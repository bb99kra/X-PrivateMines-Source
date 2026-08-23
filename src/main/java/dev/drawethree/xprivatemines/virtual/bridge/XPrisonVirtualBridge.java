package dev.drawethree.xprivatemines.virtual.bridge;

import dev.drawethree.xprison.api.XPrisonAPI;
import dev.drawethree.xprison.api.blocks.MineBlock;
import dev.drawethree.xprison.api.blocks.XPrisonBlocksAPI;
import dev.drawethree.xprison.api.blocks.factory.MineBlockFactory;
import dev.drawethree.xprison.api.blocks.factory.impl.MineBlockFactoryImpl;
import dev.drawethree.xprison.api.virtualblocks.VirtualBlockProvider;
import dev.drawethree.xprison.api.virtualblocks.VirtualBlockProviders;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.virtual.VirtualMineEngine;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class XPrisonVirtualBridge implements VirtualBlockProvider, BreakIntegration {
   private final XPrivateMines plugin;
   private final VirtualMineEngine engine;
   private final Map<dev.drawethree.xprivatemines.mines.model.block.MineBlock, MineBlock> apiBlockCache = new ConcurrentHashMap<>();
   private volatile MineBlockFactory blockFactory;

   XPrisonVirtualBridge(XPrivateMines plugin, VirtualMineEngine engine) {
      this.plugin = plugin;
      this.engine = engine;
   }

   public boolean isVirtualMineArea(@NotNull Location location) {
      return this.engine.storeAt(location) != null;
   }

   @Nullable
   public MineBlock blockAt(@NotNull Location location) {
      VirtualMineStore store = this.engine.storeAt(location);
      if (store == null) {
         return null;
      } else {
         dev.drawethree.xprivatemines.mines.model.block.MineBlock block = store.blockOf(
            store.get(location.getBlockX(), location.getBlockY(), location.getBlockZ())
         );
         return block == null ? null : this.toApiBlock(block);
      }
   }

   public int breakBlocks(@Nullable Player cause, @NotNull Collection<Location> locations) {
      if (locations.isEmpty()) {
         return 0;
      } else {
         boolean effects = this.plugin.getPrivateMinesConfig().isPacketBreakEffects();
         List<Runnable> deferredRenders = Bukkit.isPrimaryThread() ? null : new ArrayList<>();
         int removed = 0;

         for (Location location : locations) {
            VirtualMineStore store = this.engine.storeAt(location);
            if (store != null) {
               int x = location.getBlockX();
               int y = location.getBlockY();
               int z = location.getBlockZ();
               int paletteId = store.tryBreak(x, y, z);
               if (paletteId != 0) {
                  removed++;
                  if (deferredRenders == null) {
                     this.renderBreak(store, x, y, z, paletteId, cause, effects);
                  } else {
                     deferredRenders.add(() -> this.renderBreak(store, x, y, z, paletteId, cause, effects));
                  }
               }
            }
         }

         if (deferredRenders != null && !deferredRenders.isEmpty()) {
            Schedulers.sync().run(() -> deferredRenders.forEach(Runnable::run));
         }

         return removed;
      }
   }

   private void renderBreak(VirtualMineStore store, int x, int y, int z, int paletteId, @Nullable Player cause, boolean effects) {
      this.engine.getRenderer().broadcastAir(store, x, y, z);
      if (effects) {
         this.engine.getRenderer().broadcastBreakEffect(store, x, y, z, store.blockOf(paletteId), cause);
      }
   }

   @NotNull
   public Map<MineBlock, Long> collectRegion(
      @Nullable Player cause, @NotNull World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean removeBlocks
   ) {
      if (!this.engine.isMinesWorld(world)) {
         return Collections.emptyMap();
      } else {
         VirtualMineStore store = this.engine.storeOverlapping(minX, minY, minZ, maxX, maxY, maxZ);
         if (store == null) {
            return Collections.emptyMap();
         } else {
            Map<Integer, Long> byPalette = store.collectRegion(minX, minY, minZ, maxX, maxY, maxZ, removeBlocks);
            if (byPalette.isEmpty()) {
               return Collections.emptyMap();
            } else {
               if (removeBlocks) {
                  if (Bukkit.isPrimaryThread()) {
                     this.engine.getRenderer().resendRegion(store);
                  } else {
                     Schedulers.sync().run(() -> this.engine.getRenderer().resendRegion(store));
                  }
               }

               Map<MineBlock, Long> result = new HashMap<>();

               for (Entry<Integer, Long> entry : byPalette.entrySet()) {
                  dev.drawethree.xprivatemines.mines.model.block.MineBlock pm = store.blockOf(entry.getKey());
                  MineBlock api = pm == null ? null : this.toApiBlock(pm);
                  if (api != null) {
                     result.merge(api, entry.getValue(), Long::sum);
                  }
               }

               return result;
            }
         }
      }
   }

   @Override
   public boolean isActive() {
      return true;
   }

   @Override
   public AutoCloseable openBreakContext(Location location, dev.drawethree.xprivatemines.mines.model.block.MineBlock block, Player player) {
      MineBlock apiBlock = this.toApiBlock(block);
            if (apiBlock == null) {
         return () -> {};
      }
      return VirtualBlockProviders.openSnapshot(Map.of(location, apiBlock));
   }

   @Nullable
   private MineBlock toApiBlock(dev.drawethree.xprivatemines.mines.model.block.MineBlock block) {
      return this.apiBlockCache.computeIfAbsent(block, pm -> this.blockFactory().fromId(pm.serialize()));
   }

   private MineBlockFactory blockFactory() {
      MineBlockFactory cached = this.blockFactory;
      if (cached != null) {
         return cached;
      } else {
         MineBlockFactory resolved;
         try {
            XPrisonBlocksAPI blocksApi = XPrisonAPI.getInstance().getBlocksApi();
            resolved = (MineBlockFactory)(blocksApi != null ? blocksApi.getMineBlockFactory() : new MineBlockFactoryImpl());
         } catch (LinkageError | RuntimeException var4) {
            resolved = new MineBlockFactoryImpl();
         }

         this.blockFactory = resolved;
         return resolved;
      }
   }
}
