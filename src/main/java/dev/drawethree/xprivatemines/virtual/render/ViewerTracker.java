package dev.drawethree.xprivatemines.virtual.render;

import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ViewerTracker {
   private final Map<UUID, Map<UUID, Set<Long>>> loadedChunks = new ConcurrentHashMap<>();
   private final Map<UUID, Set<UUID>> viewers = new ConcurrentHashMap<>();

   public void onChunkSent(UUID playerId, long chunkKey, Collection<VirtualMineStore> stores) {
      if (!stores.isEmpty()) {
         Map<UUID, Set<Long>> perStore = this.loadedChunks.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>());

         for (VirtualMineStore store : stores) {
            UUID mineId = store.getMine().getUuid();
            perStore.computeIfAbsent(mineId, id -> ConcurrentHashMap.newKeySet()).add(chunkKey);
            this.viewers.computeIfAbsent(mineId, id -> ConcurrentHashMap.newKeySet()).add(playerId);
         }
      }
   }

   public void onChunkUnloaded(UUID playerId, long chunkKey, Collection<VirtualMineStore> stores) {
      if (!stores.isEmpty()) {
         Map<UUID, Set<Long>> perStore = this.loadedChunks.get(playerId);
         if (perStore != null) {
            for (VirtualMineStore store : stores) {
               UUID mineId = store.getMine().getUuid();
               Set<Long> chunks = perStore.get(mineId);
               if (chunks != null) {
                  chunks.remove(chunkKey);
                  if (chunks.isEmpty()) {
                     perStore.remove(mineId, chunks);
                     Set<UUID> set = this.viewers.get(mineId);
                     if (set != null) {
                        set.remove(playerId);
                     }
                  }
               }
            }
         }
      }
   }

   public void onPlayerRemoved(UUID playerId) {
      Map<UUID, Set<Long>> perStore = this.loadedChunks.remove(playerId);
      if (perStore != null) {
         for (UUID mineId : perStore.keySet()) {
            Set<UUID> set = this.viewers.get(mineId);
            if (set != null) {
               set.remove(playerId);
            }
         }
      }
   }

   public void onStoreRemoved(VirtualMineStore store) {
      UUID mineId = store.getMine().getUuid();
      this.viewers.remove(mineId);

      for (Map<UUID, Set<Long>> perStore : this.loadedChunks.values()) {
         perStore.remove(mineId);
      }
   }

   public Set<UUID> viewersOf(VirtualMineStore store) {
      Set<UUID> set = this.viewers.get(store.getMine().getUuid());
      return set != null ? set : Set.of();
   }

   public boolean hasViewers(VirtualMineStore store) {
      Set<UUID> set = this.viewers.get(store.getMine().getUuid());
      return set != null && !set.isEmpty();
   }

   public void clear() {
      this.loadedChunks.clear();
      this.viewers.clear();
   }

   public List<UUID> snapshotViewers(VirtualMineStore store) {
      return List.copyOf(this.viewersOf(store));
   }

   public static long chunkKey(int chunkX, int chunkZ) {
      return (long)chunkX << 32 | chunkZ & 4294967295L;
   }
}
