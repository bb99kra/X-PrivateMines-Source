package dev.drawethree.xprivatemines.registry;

import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class PrivateMineRegistry {
   private final Map<UUID, PrivateMineImpl> minesByPlayer = new HashMap<>();
   private final Map<UUID, PrivateMineImpl> minesById = new HashMap<>();
   private final List<PrivateMineImpl> allMines = new ArrayList<>();
   private final Map<Long, List<PrivateMineImpl>> minesByChunk = new ConcurrentHashMap<>();

   public void registerMines(List<PrivateMineImpl> mines) {
      for (PrivateMineImpl mine : mines) {
         this.registerMine(mine);
      }
   }

   public void registerMine(PrivateMineImpl mine) {
      this.allMines.add(mine);
      this.minesById.put(mine.getUuid(), mine);
      if (mine.getOwner() != null) {
         this.minesByPlayer.put(mine.getOwner(), mine);
      }
   }

   public List<PrivateMineImpl> getAll() {
      return Collections.unmodifiableList(this.allMines);
   }

   public PrivateMineImpl getByPlayer(OfflinePlayer player) {
      return this.getMineByPlayer(player.getUniqueId());
   }

   public PrivateMineImpl getMineByPlayer(UUID uuid) {
      return this.minesByPlayer.get(uuid);
   }

   public PrivateMineImpl getMineById(UUID mineUuid) {
      return this.minesById.get(mineUuid);
   }

   public void clear() {
      this.minesByPlayer.clear();
      this.minesById.clear();
      this.allMines.clear();
      this.minesByChunk.clear();
   }

   public void indexMine(PrivateMineImpl mine) {
      IWrappedRegion region = mine.getRegion();
      if (region != null) {
         ICuboidSelection selection = (ICuboidSelection)region.getSelection();
         Location min = selection.getMinimumPoint();
         Location max = selection.getMaximumPoint();

         for (int cx = min.getBlockX() >> 4; cx <= max.getBlockX() >> 4; cx++) {
            for (int cz = min.getBlockZ() >> 4; cz <= max.getBlockZ() >> 4; cz++) {
               List<PrivateMineImpl> list = this.minesByChunk.computeIfAbsent(chunkKey(cx, cz), k -> new CopyOnWriteArrayList<>());
               if (!list.contains(mine)) {
                  list.add(mine);
               }
            }
         }
      }
   }

   public PrivateMineImpl getMineAtLocation(Location location) {
      if (location != null && location.getWorld() != null) {
         List<PrivateMineImpl> candidates = this.minesByChunk.get(chunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4));
         if (candidates == null) {
            return null;
         } else {
            for (PrivateMineImpl mine : candidates) {
               if (mine.isInPrivateMine(location)) {
                  return mine;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static long chunkKey(int chunkX, int chunkZ) {
      return (long)chunkX << 32 | chunkZ & 4294967295L;
   }

   public void removePlayerMine(UUID uniqueId) {
      this.minesByPlayer.remove(uniqueId);
   }

   public Optional<PrivateMineImpl> getAvailableMine(MinesSchematic schematic) {
      return this.allMines.stream().filter(m -> m.getOwner() == null && m.getSchematic().getName().equalsIgnoreCase(schematic.getName())).findFirst();
   }

   public void assignMineToOwner(PrivateMineImpl mine, OfflinePlayer owner) {
      mine.setOwner(owner.getUniqueId());
      this.minesByPlayer.put(owner.getUniqueId(), mine);
   }
}
