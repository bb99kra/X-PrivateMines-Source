package dev.drawethree.xprivatemines.virtual;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.virtual.bridge.BreakIntegration;
import dev.drawethree.xprivatemines.virtual.dig.BreakSpeedCalculator;
import dev.drawethree.xprivatemines.virtual.dig.DigCommand;
import dev.drawethree.xprivatemines.virtual.dig.DigCommandQueue;
import dev.drawethree.xprivatemines.virtual.dig.PendingDig;
import dev.drawethree.xprivatemines.virtual.render.VirtualRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class VirtualBreakDispatcher {
   private final XPrivateMines plugin;
   private final VirtualRenderer renderer;
   private final Supplier<World> minesWorld;
   private final Map<UUID, PendingDig> pendingDigs = new HashMap<>();
   private final DigCommandQueue digQueue;
   private volatile BreakIntegration integration = BreakIntegration.NONE;

   public VirtualBreakDispatcher(XPrivateMines plugin, VirtualRenderer renderer, Supplier<World> minesWorld) {
      this.plugin = plugin;
      this.renderer = renderer;
      this.minesWorld = minesWorld;
      this.digQueue = new DigCommandQueue(this::handle, plugin.getPrivateMinesConfig().getPacketMaxQueuedDigs());
   }

   public void enqueueDig(DigCommand command) {
      this.digQueue.offer(command);
   }

   public void drainDigs(int cap) {
      this.digQueue.drain(cap);
   }

   public void clearQueue() {
      this.digQueue.clear();
   }

   private void handle(DigCommand command) {
      try {
         this.handleDig(command.player(), command.store(), command.x(), command.y(), command.z(), command.action(), command.sequence());
      } catch (Throwable var3) {
         PrivateMinesLogger.warning("Virtual dig dispatch failed for " + command.player().getName() + ": " + var3);
         var3.printStackTrace();
      }
   }

   public void setIntegration(BreakIntegration integration) {
      this.integration = integration != null ? integration : BreakIntegration.NONE;
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public void handleDig(Player player, VirtualMineStore store, int x, int y, int z, DiggingAction action, int sequence) {
      if (player.isOnline() && player.getWorld() == this.minesWorld()) {
         try {
            switch (action) {
               case START_DIGGING:
                  this.handleStart(player, store, x, y, z);
                  break;
               case FINISHED_DIGGING:
                  this.handleFinish(player, store, x, y, z);
                  break;
               case CANCELLED_DIGGING:
                  this.pendingDigs.remove(player.getUniqueId());
            }
         } finally {
            this.renderer.acknowledgeDig(player, sequence);
         }
      }
   }

   public void onPlayerRemoved(UUID playerId) {
      this.pendingDigs.remove(playerId);
   }

   private void handleStart(Player player, VirtualMineStore store, int x, int y, int z) {
      if (!this.mayMine(player, store)) {
         this.deny(player, store, x, y, z);
      } else {
         int paletteId = store.get(x, y, z);
         MineBlock block = store.blockOf(paletteId);
         if (block == null) {
            this.renderer.resyncBlock(player, store, x, y, z);
         } else {
            BlockData data = block.toBlockData();
            if (!this.plugin.getPrivateMinesConfig().isPacketInstantBreak() && !BreakSpeedCalculator.isInstantBreak(player, data)) {
               int requiredTicks = BreakSpeedCalculator.requiredTicks(player, data);
               this.pendingDigs.put(player.getUniqueId(), new PendingDig(x, y, z, System.nanoTime(), requiredTicks, store.generation()));
            } else {
               this.doBreak(player, store, x, y, z);
            }
         }
      }
   }

   private void handleFinish(Player player, VirtualMineStore store, int x, int y, int z) {
      PendingDig pending = this.pendingDigs.remove(player.getUniqueId());
      if (pending != null
         && pending.matchesPosition(x, y, z)
         && pending.generation() == store.generation()
         && pending.isElapsed(this.plugin.getPrivateMinesConfig().getPacketDigLeniency())
         && this.mayMine(player, store)) {
         this.doBreak(player, store, x, y, z);
      } else {
         this.deny(player, store, x, y, z);
      }
   }

   private void doBreak(Player player, VirtualMineStore store, int x, int y, int z) {
      int paletteId = store.tryBreak(x, y, z);
      MineBlock block = store.blockOf(paletteId);
      if (block == null) {
         this.renderer.resyncBlock(player, store, x, y, z);
      } else {
         this.renderer.resyncBlock(player, store, x, y, z);
         this.renderer.broadcastAir(store, x, y, z);
         if (this.plugin.getPrivateMinesConfig().isPacketBreakEffects()) {
            this.renderer.broadcastBreakEffect(store, x, y, z, block, null);
         }

         World world = this.minesWorld();
         Block bukkitBlock = world.getBlockAt(x, y, z);
         BreakIntegration bridge = this.integration;

         boolean cancelled;
         try (AutoCloseable ignored = bridge.openBreakContext(bukkitBlock.getLocation(), block, player)) {
            BlockBreakEvent event = new BlockBreakEvent(bukkitBlock, player);
            event.setDropItems(false);
            event.setExpToDrop(0);
            Bukkit.getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
         } catch (Exception var17) {
            PrivateMinesLogger.warning("Virtual break pipeline failed at " + x + "," + y + "," + z + ": " + var17);
            var17.printStackTrace();
            cancelled = false;
         }

         if (cancelled) {
            store.restore(x, y, z, paletteId);
            this.renderer.resyncBlock(player, store, x, y, z);
            this.renderer.broadcastBlock(store, x, y, z);
         } else {
            if (!bridge.isActive() && this.plugin.getPrivateMinesConfig().isPacketVanillaDropsToInventory()) {
               this.giveDrop(player, block);
            }
         }
      }
   }

   private void giveDrop(Player player, MineBlock block) {
      ItemStack drop = block.toIcon();
      Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{drop});
      if (!leftover.isEmpty()) {
         Location at = player.getLocation().add(0.0, 0.25, 0.0);
         Vector zero = new Vector(0, 0, 0);

         for (ItemStack item : leftover.values()) {
            Item dropped = player.getWorld().dropItem(at, item);
            dropped.setVelocity(zero);
         }
      }
   }

   private boolean mayMine(Player player, VirtualMineStore store) {
      GameMode gameMode = player.getGameMode();
      if (gameMode != GameMode.SPECTATOR && gameMode != GameMode.ADVENTURE) {
         PrivateMineImpl mine = store.getMine();
         return mine.isBanned(player) ? false : mine.isOpen() || mine.isOwner(player);
      } else {
         return false;
      }
   }

   private void deny(Player player, VirtualMineStore store, int x, int y, int z) {
      this.pendingDigs.remove(player.getUniqueId());
      this.renderer.resyncBlock(player, store, x, y, z);
   }

   private World minesWorld() {
      return this.minesWorld.get();
   }
}
