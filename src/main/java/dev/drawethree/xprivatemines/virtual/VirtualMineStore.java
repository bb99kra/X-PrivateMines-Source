package dev.drawethree.xprivatemines.virtual;

import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class VirtualMineStore {
   private final PrivateMineImpl mine;
   private final int minX;
   private final int minY;
   private final int minZ;
   private final int sizeX;
   private final int sizeY;
   private final int sizeZ;
   private final long volume;
   private volatile VirtualPalette palette;
   private volatile byte[] blocks;
   private volatile int generation;
   private static final int PALETTE_IDS = 256;
   private final AtomicLong remaining = new AtomicLong();
   private long[] paletteCounts;

   VirtualMineStore(PrivateMineImpl mine, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.mine = mine;
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.sizeX = maxX - minX + 1;
      this.sizeY = maxY - minY + 1;
      this.sizeZ = maxZ - minZ + 1;
      this.volume = (long)this.sizeX * this.sizeY * this.sizeZ;
      this.remaining.set(this.volume);
   }

   public PrivateMineImpl getMine() {
      return this.mine;
   }

   public synchronized void fill(VirtualPalette palette) {
      this.palette = palette;
      this.blocks = null;
      this.paletteCounts = null;
      this.generation++;
      this.remaining.set(this.volume);
   }

   public synchronized void evict() {
      if (this.blocks != null) {
         this.blocks = null;
         this.paletteCounts = null;
         this.generation++;
         this.remaining.set(this.volume);
      }
   }

   public int get(int x, int y, int z) {
      if (!this.contains(x, y, z)) {
         return 0;
      } else {
         byte[] data = this.materialized();
         return data == null ? 0 : data[this.index(x, y, z)] & 0xFF;
      }
   }

   public synchronized int tryBreak(int x, int y, int z) {
      if (!this.contains(x, y, z)) {
         return 0;
      } else {
         byte[] data = this.materialized();
         if (data == null) {
            return 0;
         } else {
            int idx = this.index(x, y, z);
            int id = data[idx] & 255;
            if (id == 0) {
               return 0;
            } else {
               data[idx] = 0;
               this.remaining.decrementAndGet();
               if (this.paletteCounts != null) {
                  this.paletteCounts[id]--;
               }

               return id;
            }
         }
      }
   }

   public synchronized void restore(int x, int y, int z, int paletteId) {
      if (paletteId != 0 && this.contains(x, y, z)) {
         byte[] data = this.materialized();
         if (data != null) {
            int idx = this.index(x, y, z);
            if ((data[idx] & 255) == 0) {
               data[idx] = (byte)paletteId;
               this.remaining.incrementAndGet();
               if (this.paletteCounts != null) {
                  this.paletteCounts[paletteId]++;
               }
            }
         }
      }
   }

   public synchronized Map<Integer, Long> collectRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean remove) {
      if (this.remaining.get() == 0L) {
         return Collections.emptyMap();
      } else {
         byte[] data = this.materialized();
         if (data == null) {
            return Collections.emptyMap();
         } else {
            int x0 = Math.max(minX, this.minX);
            int x1 = Math.min(maxX, this.getMaxX());
            int y0 = Math.max(minY, this.minY);
            int y1 = Math.min(maxY, this.getMaxY());
            int z0 = Math.max(minZ, this.minZ);
            int z1 = Math.min(maxZ, this.getMaxZ());
            if (x0 <= x1 && y0 <= y1 && z0 <= z1) {
               boolean coversWholeStore = x0 == this.minX
                  && y0 == this.minY
                  && z0 == this.minZ
                  && x1 == this.getMaxX()
                  && y1 == this.getMaxY()
                  && z1 == this.getMaxZ();
               if (coversWholeStore && this.paletteCounts != null) {
                  Map<Integer, Long> counts = new HashMap<>();

                  for (int id = 1; id < this.paletteCounts.length; id++) {
                     if (this.paletteCounts[id] > 0L) {
                        counts.put(id, this.paletteCounts[id]);
                     }
                  }

                  if (remove && !counts.isEmpty()) {
                     Arrays.fill(data, (byte)0);
                     Arrays.fill(this.paletteCounts, 0L);
                     this.remaining.set(0L);
                  }

                  return counts;
               } else {
                  Map<Integer, Long> counts = new HashMap<>();
                  long removed = 0L;

                  for (int y = y0; y <= y1; y++) {
                     for (int z = z0; z <= z1; z++) {
                        int rowBase = ((y - this.minY) * this.sizeZ + (z - this.minZ)) * this.sizeX - this.minX;

                        for (int x = x0; x <= x1; x++) {
                           int idx = rowBase + x;
                           int idxx = data[idx] & 255;
                           if (idxx != 0) {
                              if (remove) {
                                 data[idx] = 0;
                              }

                              counts.merge(idxx, 1L, Long::sum);
                              removed++;
                           }
                        }
                     }
                  }

                  if (remove && removed > 0L) {
                     this.remaining.addAndGet(-removed);
                  }

                  return counts;
               }
            } else {
               return Collections.emptyMap();
            }
         }
      }
   }

   public long remaining() {
      return this.remaining.get();
   }

   public long volume() {
      return this.volume;
   }

   public int generation() {
      return this.generation;
   }

   public VirtualPalette getPalette() {
      return this.palette;
   }

   public boolean isMaterialized() {
      return this.blocks != null;
   }

   public void ensureMaterialized() {
      this.materialized();
   }

   public boolean contains(int x, int y, int z) {
      return x >= this.minX && x < this.minX + this.sizeX && y >= this.minY && y < this.minY + this.sizeY && z >= this.minZ && z < this.minZ + this.sizeZ;
   }

   public boolean intersectsChunk(int chunkX, int chunkZ) {
      int chunkMinX = chunkX << 4;
      int chunkMinZ = chunkZ << 4;
      return chunkMinX + 15 >= this.minX && chunkMinX < this.minX + this.sizeX && chunkMinZ + 15 >= this.minZ && chunkMinZ < this.minZ + this.sizeZ;
   }

   public int getMinX() {
      return this.minX;
   }

   public int getMinY() {
      return this.minY;
   }

   public int getMinZ() {
      return this.minZ;
   }

   public int getMaxX() {
      return this.minX + this.sizeX - 1;
   }

   public int getMaxY() {
      return this.minY + this.sizeY - 1;
   }

   public int getMaxZ() {
      return this.minZ + this.sizeZ - 1;
   }

   public MineBlock blockOf(int paletteId) {
      VirtualPalette current = this.palette;
      return current != null && paletteId > 0 && paletteId <= current.size() ? current.block(paletteId) : null;
   }

   public void forEachInSection(int sectionX, int sectionY, int sectionZ, VirtualMineStore.PositionVisitor visitor) {
      byte[] data = this.materialized();
      if (data != null) {
         int fromX = Math.max(this.minX, sectionX << 4);
         int toX = Math.min(this.getMaxX(), (sectionX << 4) + 15);
         int fromY = Math.max(this.minY, sectionY << 4);
         int toY = Math.min(this.getMaxY(), (sectionY << 4) + 15);
         int fromZ = Math.max(this.minZ, sectionZ << 4);
         int toZ = Math.min(this.getMaxZ(), (sectionZ << 4) + 15);

         for (int y = fromY; y <= toY; y++) {
            for (int z = fromZ; z <= toZ; z++) {
               int rowBase = ((y - this.minY) * this.sizeZ + (z - this.minZ)) * this.sizeX - this.minX;

               for (int x = fromX; x <= toX; x++) {
                  visitor.visit(x, y, z, data[rowBase + x] & 255);
               }
            }
         }
      }
   }

   private int index(int x, int y, int z) {
      return ((y - this.minY) * this.sizeZ + (z - this.minZ)) * this.sizeX + (x - this.minX);
   }

   private byte[] materialized() {
      byte[] data = this.blocks;
      if (data != null) {
         return data;
      } else {
         VirtualPalette current = this.palette;
         if (current == null) {
            return null;
         } else {
            synchronized (this) {
               if (this.blocks != null) {
                  return this.blocks;
               } else if (this.volume > 2147483647L) {
                  throw new IllegalStateException("Mining region too large for a virtual store: " + this.volume + " blocks");
               } else {
                  byte[] fresh = new byte[(int)this.volume];
                  long[] counts = new long[256];
                  if (current.size() == 1) {
                     Arrays.fill(fresh, (byte)1);
                     counts[1] = this.volume;
                  } else {
                     for (int i = 0; i < fresh.length; i++) {
                        int id = current.sample();
                        fresh[i] = (byte)id;
                        counts[id]++;
                     }
                  }

                  this.remaining.set(this.volume);
                  this.paletteCounts = counts;
                  this.blocks = fresh;
                  return fresh;
               }
            }
         }
      }
   }

   @FunctionalInterface
   public interface PositionVisitor {
      void visit(int var1, int var2, int var3, int var4);
   }
}
