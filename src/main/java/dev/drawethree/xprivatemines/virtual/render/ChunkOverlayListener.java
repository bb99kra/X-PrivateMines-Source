package dev.drawethree.xprivatemines.virtual.render;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.virtual.VirtualMineEngine;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.entity.Player;

public final class ChunkOverlayListener extends PacketListenerAbstract {
   private static final long ERROR_LOG_THROTTLE_MS = 10000L;
   private final VirtualMineEngine engine;
   private final ViewerTracker tracker;
   private final BlockStateCache stateCache;
   private final AtomicLong lastErrorLog = new AtomicLong();

   public ChunkOverlayListener(VirtualMineEngine engine, ViewerTracker tracker, BlockStateCache stateCache) {
      super(PacketListenerPriority.HIGH);
      this.engine = engine;
      this.tracker = tracker;
      this.stateCache = stateCache;
   }

   public void onPacketSend(PacketSendEvent event) {
      try {
         if (event.getPacketType() == Server.CHUNK_DATA) {
            this.handleChunkData(event);
         } else if (event.getPacketType() == Server.UNLOAD_CHUNK) {
            this.handleUnloadChunk(event);
         } else if (event.getPacketType() == Server.BLOCK_CHANGE) {
            this.handleBlockChange(event);
         } else if (event.getPacketType() == Server.MULTI_BLOCK_CHANGE) {
            this.handleMultiBlockChange(event);
         }
      } catch (Throwable var3) {
         this.logThrottled(var3);
      }
   }

   private void handleChunkData(PacketSendEvent event) {
      Player player = (Player)event.getPlayer();
      if (player != null && this.engine.isMinesWorld(player.getWorld())) {
         WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
         Column column = wrapper.getColumn();
         List<VirtualMineStore> stores = this.engine.storesInChunk(column.getX(), column.getZ());
         if (!stores.isEmpty()) {
            int worldMinY = this.engine.getWorldMinY();
            BaseChunk[] sections = column.getChunks();
            boolean modified = false;

            for (VirtualMineStore store : stores) {
               int fromSectionY = Math.max(store.getMinY(), worldMinY) >> 4;
               int toSectionY = store.getMaxY() >> 4;

               for (int sectionY = fromSectionY; sectionY <= toSectionY; sectionY++) {
                  int sectionIndex = sectionY - (worldMinY >> 4);
                  if (sectionIndex >= 0 && sectionIndex < sections.length && sections[sectionIndex] != null) {
                     BaseChunk section = sections[sectionIndex];
                     boolean[] touched = new boolean[]{false};
                     store.forEachInSection(column.getX(), sectionY, column.getZ(), (x, y, z, paletteId) -> {
                        if (paletteId != 0) {
                           section.set(x & 15, y & 15, z & 15, this.stateCache.stateFor(store.blockOf(paletteId)));
                           touched[0] = true;
                        }
                     });
                     modified |= touched[0];
                  }
               }
            }

            if (modified) {
               event.markForReEncode(true);
            }

            this.tracker.onChunkSent(player.getUniqueId(), ViewerTracker.chunkKey(column.getX(), column.getZ()), stores);
         }
      }
   }

   private void handleUnloadChunk(PacketSendEvent event) {
      Player player = (Player)event.getPlayer();
      if (player != null && this.engine.isMinesWorld(player.getWorld())) {
         WrapperPlayServerUnloadChunk wrapper = new WrapperPlayServerUnloadChunk(event);
         List<VirtualMineStore> stores = this.engine.storesInChunk(wrapper.getChunkX(), wrapper.getChunkZ());
         if (!stores.isEmpty()) {
            this.tracker.onChunkUnloaded(player.getUniqueId(), ViewerTracker.chunkKey(wrapper.getChunkX(), wrapper.getChunkZ()), stores);
         }
      }
   }

   private void handleBlockChange(PacketSendEvent event) {
      Player player = (Player)event.getPlayer();
      if (player != null && this.engine.isMinesWorld(player.getWorld())) {
         WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
         int x = wrapper.getBlockPosition().getX();
         int y = wrapper.getBlockPosition().getY();
         int z = wrapper.getBlockPosition().getZ();
         VirtualMineStore store = this.engine.storeAt(x, y, z);
         if (store != null) {
            MineBlock virtual = store.blockOf(store.get(x, y, z));
            if (virtual != null) {
               int virtualId = this.stateCache.globalIdOf(virtual);
               if (wrapper.getBlockId() != virtualId) {
                  wrapper.setBlockID(virtualId);
                  event.markForReEncode(true);
               }
            }
         }
      }
   }

   private void handleMultiBlockChange(PacketSendEvent event) {
      Player player = (Player)event.getPlayer();
      if (player != null && this.engine.isMinesWorld(player.getWorld())) {
         WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
         boolean modified = false;
         int cachedChunkX = Integer.MIN_VALUE;
         int cachedChunkZ = Integer.MIN_VALUE;
         List<VirtualMineStore> chunkStores = List.of();

         for (EncodedBlock block : wrapper.getBlocks()) {
            int chunkX = block.getX() >> 4;
            int chunkZ = block.getZ() >> 4;
            if (chunkX != cachedChunkX || chunkZ != cachedChunkZ) {
               cachedChunkX = chunkX;
               cachedChunkZ = chunkZ;
               chunkStores = this.engine.storesInChunk(chunkX, chunkZ);
            }

            if (!chunkStores.isEmpty()) {
               VirtualMineStore store = storeContaining(chunkStores, block.getX(), block.getY(), block.getZ());
               if (store != null) {
                  MineBlock virtual = store.blockOf(store.get(block.getX(), block.getY(), block.getZ()));
                  if (virtual != null) {
                     int virtualId = this.stateCache.globalIdOf(virtual);
                     if (block.getBlockId() != virtualId) {
                        block.setBlockId(virtualId);
                        modified = true;
                     }
                  }
               }
            }
         }

         if (modified) {
            event.markForReEncode(true);
         }
      }
   }

   private static VirtualMineStore storeContaining(List<VirtualMineStore> stores, int x, int y, int z) {
      for (VirtualMineStore store : stores) {
         if (store.contains(x, y, z)) {
            return store;
         }
      }

      return null;
   }

   private void logThrottled(Throwable t) {
      long now = System.currentTimeMillis();
      long last = this.lastErrorLog.get();
      if (now - last >= 10000L && this.lastErrorLog.compareAndSet(last, now)) {
         PrivateMinesLogger.warning("Packet-mines chunk overlay failed (packet passed through unmodified): " + t);
         t.printStackTrace();
      }
   }
}
