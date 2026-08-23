package dev.drawethree.xprivatemines.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.codemc.worldguardwrapper.flag.WrappedState;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMinePostResetEvent;
import dev.drawethree.xprivatemines.api.events.PrivateMinePreResetEvent;
import dev.drawethree.xprivatemines.mines.model.MineTierImpl;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.schematic.SchematicSettingsImpl;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.region.RegionUtils;
import dev.drawethree.xprivatemines.virtual.VirtualMineEngine;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import dev.drawethree.xprivatemines.virtual.VirtualPalette;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PacketMineResetService implements MineRefillService {
   private static final String LAST_MODE_KEY = "packet-mines-last-mode";
   private static final String MODE_PACKET = "packet";
   private static final int MIGRATION_CLEARS_PER_BATCH = 5;
   private static final long MIGRATION_BATCH_DELAY_TICKS = 10L;
   private static final int REALWORLD_PREPS_PER_BATCH = 4;
   private static final long REALWORLD_BATCH_DELAY_TICKS = 5L;
   private final XPrivateMines plugin;
   private final VirtualMineEngine engine;
   private final MineResetService legacyResetService;
   private final Queue<Runnable> realWorldPrepQueue = new ConcurrentLinkedQueue<>();
   private final AtomicBoolean realWorldDrainerActive = new AtomicBoolean(false);
   private final Map<UUID, VirtualMineStore> wallsPlacedFor = new ConcurrentHashMap<>();
   private final Map<UUID, VirtualMineStore> interiorClearedFor = new ConcurrentHashMap<>();
   private final Map<UUID, VirtualMineStore> enchantFlagEnsuredFor = new ConcurrentHashMap<>();
   private static final String ENCHANTS_WG_FLAG = "upc-enchants";

   public PacketMineResetService(XPrivateMines plugin, VirtualMineEngine engine, MineResetService legacyResetService) {
      this.plugin = plugin;
      this.engine = engine;
      this.legacyResetService = legacyResetService;
   }

   @Override
   public void refill(PrivateMineImpl mine) {
      this.refill(mine, null);
   }

   @Override
   public void refill(PrivateMineImpl mine, CommandSender sender) {
      if (this.plugin.getPrivateMinesConfig().isPacketTeleportOnReset()) {
         Schedulers.sync().run(() -> mine.getPlayersInMine().forEach(mine::teleportToReset));
      }

      Schedulers.async().run(() -> {
         PrivateMinePreResetEvent event = new PrivateMinePreResetEvent(mine);
         Events.callSync(event);
         this.plugin.debug("Called PrivateMinePreResetEvent event");
         if (event.isCancelled()) {
            this.plugin.debug("PrivateMinePreResetEvent was cancelled");
         } else {
            VirtualMineStore store = this.engine.getOrCreateStore(mine);
            if (store == null) {
               PrivateMinesLogger.warning("Cannot packet-refill mine " + mine.getUuid() + ": no mining region.");
            } else {
               VirtualPalette palette = this.buildPalette(mine);
               if (palette == null) {
                  PrivateMinesLogger.warning("Cannot packet-refill mine " + mine.getUuid() + ": empty block composition.");
               } else {
                  this.engine.warmUp(palette);
                  store.fill(palette);
                  if (this.engine.getTracker().hasViewers(store)) {
                     store.ensureMaterialized();
                  }

                  this.enqueueRealWorldPrepIfNeeded(mine, store);
                  Schedulers.sync().run(() -> {
                     this.ensureEnchantFlagIfNeeded(mine, store);
                     this.engine.getRenderer().discardPendingUpdates(store);
                     this.engine.clearPreviousFootprint(store);
                     this.engine.getRenderer().resendRegion(store);
                     this.carveAirPocketsAroundPlayers(store);
                     if (sender != null) {
                        PlayerUtils.sendMessage(sender, this.plugin.getMessageConfig().getMessage("mine-refill"));
                     }

                     PrivateMinePostResetEvent postResetEvent = new PrivateMinePostResetEvent(mine);
                     Events.callSync(postResetEvent);
                     this.plugin.debug("Called PrivateMinePostResetEvent event");
                  });
               }
            }
         }
      });
   }

   @Override
   public boolean shouldReset(PrivateMineImpl mine) {
      return this.legacyResetService.shouldReset(mine);
   }

   public void onMinesLoaded(Collection<PrivateMineImpl> mines, RegionService regionService) {
      this.seedInitialFills(mines);
      String lastMode = this.plugin.getConfig().getString("packet-mines-last-mode", "");
      if (!"packet".equals(lastMode)) {
         PrivateMinesLogger.info("Packet-mines: previous boot used FAWE mode - clearing leftover real blocks in " + mines.size() + " mining region(s)...");
         List<PrivateMineImpl> queue = new ArrayList<>(mines);
         this.clearNextBatch(queue, regionService, () -> {
            this.plugin.reloadConfig();
            this.plugin.getConfig().set("packet-mines-last-mode", "packet");
            this.plugin.saveConfig();
            PrivateMinesLogger.info("Packet-mines: migration clear complete.");
         });
      }
   }

   private void clearNextBatch(List<PrivateMineImpl> queue, RegionService regionService, Runnable onDone) {
      if (queue.isEmpty()) {
         Schedulers.sync().run(onDone);
      } else {
         int batch = Math.min(5, queue.size());

         for (int i = 0; i < batch; i++) {
            PrivateMineImpl mine = queue.remove(queue.size() - 1);
            if (mine.getMineImpl() != null && mine.getMineImpl().getRegion() != null) {
               regionService.clearRegionAsync(mine.getMineImpl().getRegion());
            }
         }

         Schedulers.sync().runLater(() -> this.clearNextBatch(queue, regionService, onDone), 10L);
      }
   }

   private void seedInitialFills(Collection<PrivateMineImpl> mines) {
      for (PrivateMineImpl mine : mines) {
         VirtualMineStore store = this.engine.getOrCreateStore(mine);
         if (store != null && store.getPalette() == null) {
            VirtualPalette palette = this.buildPalette(mine);
            if (palette == null) {
               PrivateMinesLogger.warning("Mine " + mine.getUuid() + " has an empty block composition; it will render empty until fixed.");
            } else {
               this.engine.warmUp(palette);
               store.fill(palette);
               this.ensureEnchantFlagIfNeeded(mine, store);
            }
         }
      }
   }

   private VirtualPalette buildPalette(PrivateMineImpl mine) {
      try {
         if (mine.getMineImpl().getSelectedBlock() != null) {
            return VirtualPalette.ofSingle(mine.getMineImpl().getSelectedBlock());
         } else {
            MineTierImpl tier = (MineTierImpl)mine.getTier();
            return VirtualPalette.ofWeights(tier.getMineBlockWeights());
         }
      } catch (IllegalArgumentException var3) {
         return null;
      }
   }

   private void carveAirPocketsAroundPlayers(VirtualMineStore store) {
      for (UUID viewerId : this.engine.getTracker().viewersOf(store)) {
         Player player = Bukkit.getPlayer(viewerId);
         if (player != null) {
            Location feet = player.getLocation();
            int x = feet.getBlockX();
            int y = feet.getBlockY();
            int z = feet.getBlockZ();
            if (store.contains(x, y, z) || store.contains(x, y + 1, z)) {
               if (store.tryBreak(x, y, z) != 0) {
                  this.engine.getRenderer().broadcastAir(store, x, y, z);
               }

               if (store.tryBreak(x, y + 1, z) != 0) {
                  this.engine.getRenderer().broadcastAir(store, x, y + 1, z);
               }
            }
         }
      }
   }

   private void ensureEnchantFlagIfNeeded(PrivateMineImpl mine, VirtualMineStore store) {
      if (this.engine.isBridgeActive()) {
         if (this.enchantFlagEnsuredFor.get(mine.getUuid()) != store) {
            this.enchantFlagEnsuredFor.put(mine.getUuid(), store);

            try {
               if (mine.getMineImpl() != null
                  && mine.getMineImpl().getRegion() != null
                  && RegionUtils.ensureStateFlag(mine.getMineImpl().getRegion(), "upc-enchants", WrappedState.ALLOW)) {
                  PrivateMinesLogger.info(
                     "Packet-mines: applied missing 'upc-enchants: ALLOW' to mine region of " + mine.getUuid() + " (required for X-Prison enchants/autosell)."
                  );
               }
            } catch (Exception var4) {
               PrivateMinesLogger.warning("Packet-mines: could not verify 'upc-enchants' flag for mine " + mine.getUuid() + ": " + var4);
            }
         }
      }
   }

   private void enqueueRealWorldPrepIfNeeded(PrivateMineImpl mine, VirtualMineStore store) {
      boolean needClear = this.plugin.getPrivateMinesConfig().isPacketClearRealInterior() && this.interiorClearedFor.get(mine.getUuid()) != store;
      boolean needWalls = mine.getSchematic().getSettings() instanceof SchematicSettingsImpl s
         && s.isBedrockWalls()
         && this.wallsPlacedFor.get(mine.getUuid()) != store;
      if (needClear || needWalls) {
         if (needClear) {
            this.interiorClearedFor.put(mine.getUuid(), store);
         }

         if (needWalls) {
            this.wallsPlacedFor.put(mine.getUuid(), store);
         }

         this.realWorldPrepQueue.add(() -> this.runRealWorldPrep(mine, store, needClear, needWalls));
         this.ensureRealWorldDrainer();
      }
   }

   private void ensureRealWorldDrainer() {
      if (this.realWorldDrainerActive.compareAndSet(false, true)) {
         Schedulers.async().run(this::drainRealWorldPrep);
      }
   }

   private void drainRealWorldPrep() {
      Runnable task;
      for (int processed = 0; processed < 4 && (task = this.realWorldPrepQueue.poll()) != null; processed++) {
         try {
            task.run();
         } catch (Exception var4) {
            PrivateMinesLogger.warning("Packet-mines: real-world prep task failed: " + var4);
            var4.printStackTrace();
         }
      }

      if (!this.realWorldPrepQueue.isEmpty()) {
         Schedulers.async().runLater(this::drainRealWorldPrep, 5L);
      } else {
         this.realWorldDrainerActive.set(false);
         if (!this.realWorldPrepQueue.isEmpty() && this.realWorldDrainerActive.compareAndSet(false, true)) {
            Schedulers.async().runLater(this::drainRealWorldPrep, 5L);
         }
      }
   }

   private void runRealWorldPrep(PrivateMineImpl mine, VirtualMineStore store, boolean clearInterior, boolean placeWalls) {
      try {
         World bukkitWorld = this.plugin.getPrivateMinesConfig().getMinesWorld();
         com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
         BlockVector3 min = BlockVector3.at(store.getMinX(), store.getMinY(), store.getMinZ());
         BlockVector3 max = BlockVector3.at(store.getMaxX(), store.getMaxY(), store.getMaxZ());
         EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();

         try {
            if (clearInterior) {
               editSession.setBlocks(new CuboidRegion(weWorld, min, max), BlockTypes.AIR.getDefaultState());
            }

            if (placeWalls) {
               MineResetService.placeBedrockWalls(editSession, weWorld, min, max);
            }
         } catch (Throwable var13) {
            if (editSession != null) {
               try {
                  editSession.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (editSession != null) {
            editSession.close();
         }
      } catch (Exception var14) {
         PrivateMinesLogger.warning("Failed to prepare real world for mine " + mine.getUuid() + ": " + var14);
         var14.printStackTrace();
      }
   }
}
