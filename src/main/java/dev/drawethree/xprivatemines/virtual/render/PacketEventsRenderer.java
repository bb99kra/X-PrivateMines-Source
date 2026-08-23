package dev.drawethree.xprivatemines.virtual.render;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PacketEventsRenderer implements VirtualRenderer {
   private static final int EFFECT_BLOCK_BREAK = 2001;
   private final ViewerTracker tracker;
   private final BlockStateCache stateCache;
   private final BlockUpdateCoalescer coalescer;

   public PacketEventsRenderer(ViewerTracker tracker, BlockStateCache stateCache) {
      this.tracker = tracker;
      this.stateCache = stateCache;
      this.coalescer = new BlockUpdateCoalescer(this::flushBatch, task -> Schedulers.sync().run(task), Bukkit::isPrimaryThread);
   }

   PacketEventsRenderer(ViewerTracker tracker, BlockStateCache stateCache, BlockUpdateCoalescer coalescer) {
      this.tracker = tracker;
      this.stateCache = stateCache;
      this.coalescer = coalescer;
   }

   @Override
   public void resyncBlock(Player player, VirtualMineStore store, int x, int y, int z) {
      this.send(player, new WrapperPlayServerBlockChange(new Vector3i(x, y, z), this.globalIdAt(store, x, y, z)));
   }

   @Override
   public void acknowledgeDig(Player player, int sequence) {
      if (sequence > 0) {
         this.send(player, new WrapperPlayServerAcknowledgeBlockChanges(sequence));
      }
   }

   @Override
   public void broadcastBlock(VirtualMineStore store, int x, int y, int z) {
      this.coalescer.enqueueBlock(store, x, y, z);
   }

   @Override
   public void broadcastAir(VirtualMineStore store, int x, int y, int z) {
      this.coalescer.enqueueBlock(store, x, y, z);
   }

   @Override
   public void discardPendingUpdates(VirtualMineStore store) {
      this.coalescer.discardPending(store);
   }

   @Override
   public void broadcastBreakEffect(VirtualMineStore store, int x, int y, int z, MineBlock block, Player except) {
      this.coalescer.enqueueEffect(store, x, y, z, block, except);
   }

   @Override
   public void resendRegion(VirtualMineStore store) {
      List<Player> viewers = this.resolveViewers(store);
      if (!viewers.isEmpty()) {
         int fromSectionX = store.getMinX() >> 4;
         int toSectionX = store.getMaxX() >> 4;
         int fromSectionY = store.getMinY() >> 4;
         int toSectionY = store.getMaxY() >> 4;
         int fromSectionZ = store.getMinZ() >> 4;
         int toSectionZ = store.getMaxZ() >> 4;

         for (int sx = fromSectionX; sx <= toSectionX; sx++) {
            for (int sy = fromSectionY; sy <= toSectionY; sy++) {
               for (int sz = fromSectionZ; sz <= toSectionZ; sz++) {
                  List<EncodedBlock> encoded = new ArrayList<>();
                  store.forEachInSection(
                     sx, sy, sz, (x, y, z, paletteId) -> encoded.add(new EncodedBlock(paletteId == 0 ? 0 : this.globalIdOf(store.blockOf(paletteId)), x, y, z))
                  );
                  if (!encoded.isEmpty()) {
                     WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(
                        new Vector3i(sx, sy, sz), true, encoded.toArray(new EncodedBlock[0])
                     );

                     for (Player viewer : viewers) {
                        this.send(viewer, packet);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void clearArea(VirtualMineStore viewerStore, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      List<Player> viewers = this.resolveViewers(viewerStore);
      if (!viewers.isEmpty()) {
         for (int sx = minX >> 4; sx <= maxX >> 4; sx++) {
            for (int sy = minY >> 4; sy <= maxY >> 4; sy++) {
               for (int sz = minZ >> 4; sz <= maxZ >> 4; sz++) {
                  int fromX = Math.max(minX, sx << 4);
                  int toX = Math.min(maxX, (sx << 4) + 15);
                  int fromY = Math.max(minY, sy << 4);
                  int toY = Math.min(maxY, (sy << 4) + 15);
                  int fromZ = Math.max(minZ, sz << 4);
                  int toZ = Math.min(maxZ, (sz << 4) + 15);
                  List<EncodedBlock> encoded = new ArrayList<>();

                  for (int x = fromX; x <= toX; x++) {
                     for (int y = fromY; y <= toY; y++) {
                        for (int z = fromZ; z <= toZ; z++) {
                           encoded.add(new EncodedBlock(0, x, y, z));
                        }
                     }
                  }

                  if (!encoded.isEmpty()) {
                     WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(
                        new Vector3i(sx, sy, sz), true, encoded.toArray(new EncodedBlock[0])
                     );

                     for (Player viewer : viewers) {
                        this.send(viewer, packet);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void warmUp(Iterable<MineBlock> blocks) {
      this.stateCache.warmUp(blocks);
   }

   private void flushBatch(BlockUpdateCoalescer.StoreBatch batch) {
      VirtualMineStore store = batch.store();
      List<Player> viewers = this.resolveViewers(store);
      if (!viewers.isEmpty() && !batch.dirtyPositions().isEmpty()) {
         if (batch.dirtyPositions().size() == 1) {
            long pos = batch.dirtyPositions().iterator().next();
            int x = BlockUpdateCoalescer.unpackX(pos);
            int y = BlockUpdateCoalescer.unpackY(pos);
            int z = BlockUpdateCoalescer.unpackZ(pos);
            WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(new Vector3i(x, y, z), this.globalIdAt(store, x, y, z));

            for (Player viewer : viewers) {
               this.send(viewer, packet);
            }
         } else {
            this.sendGroupedBySection(store, batch.dirtyPositions(), viewers);
         }
      }

      for (BlockUpdateCoalescer.EffectEntry effect : batch.effects()) {
         PacketWrapper<?> packet = new WrapperPlayServerEffect(2001, new Vector3i(effect.x(), effect.y(), effect.z()), this.globalIdOf(effect.block()), false);

         for (Player viewer : viewers) {
            if (effect.except() == null || !viewer.getUniqueId().equals(effect.except().getUniqueId())) {
               this.send(viewer, packet);
            }
         }
      }
   }

   private void sendGroupedBySection(VirtualMineStore store, Set<Long> dirty, List<Player> viewers) {
      Map<Long, List<Long>> bySection = BlockUpdateCoalescer.groupBySection(dirty);

      for (Entry<Long, List<Long>> section : bySection.entrySet()) {
         List<Long> positions = section.getValue();
         EncodedBlock[] encoded = new EncodedBlock[positions.size()];

         for (int i = 0; i < positions.size(); i++) {
            long pos = positions.get(i);
            int x = BlockUpdateCoalescer.unpackX(pos);
            int y = BlockUpdateCoalescer.unpackY(pos);
            int z = BlockUpdateCoalescer.unpackZ(pos);
            encoded[i] = new EncodedBlock(this.globalIdAt(store, x, y, z), x, y, z);
         }

         long sectionKey = section.getKey();
         WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(
            new Vector3i(BlockUpdateCoalescer.unpackX(sectionKey), BlockUpdateCoalescer.unpackY(sectionKey), BlockUpdateCoalescer.unpackZ(sectionKey)),
            true,
            encoded
         );

         for (Player viewer : viewers) {
            this.send(viewer, packet);
         }
      }
   }

   private List<Player> resolveViewers(VirtualMineStore store) {
      List<UUID> viewerIds = this.tracker.snapshotViewers(store);
      if (viewerIds.isEmpty()) {
         return List.of();
      } else {
         List<Player> viewers = new ArrayList<>(viewerIds.size());

         for (UUID id : viewerIds) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
               viewers.add(player);
            }
         }

         return viewers;
      }
   }

   private int globalIdAt(VirtualMineStore store, int x, int y, int z) {
      return this.stateCache.globalIdOf(store.blockOf(store.get(x, y, z)));
   }

   private int globalIdOf(MineBlock block) {
      return this.stateCache.globalIdOf(block);
   }

   private void send(Player player, PacketWrapper<?> packet) {
      PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
   }
}
