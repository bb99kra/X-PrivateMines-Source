package dev.drawethree.xprivatemines.virtual;

import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public final class VirtualPalette {
   public static final int AIR_ID = 0;
   public static final int MAX_ENTRIES = 255;
   private final List<MineBlock> entries;
   private final long[] cumulativeWeights;
   private final long totalWeight;

   private VirtualPalette(List<MineBlock> entries, long[] cumulativeWeights, long totalWeight) {
      this.entries = entries;
      this.cumulativeWeights = cumulativeWeights;
      this.totalWeight = totalWeight;
   }

   public static VirtualPalette ofSingle(MineBlock block) {
      return ofWeights(Map.of(block, 1));
   }

   public static VirtualPalette ofWeights(Map<MineBlock, Integer> weights) {
      Map<MineBlock, Integer> filtered = new LinkedHashMap<>();

      for (Entry<MineBlock, Integer> entry : weights.entrySet()) {
         if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
            filtered.put(entry.getKey(), entry.getValue());
         }
      }

      if (filtered.isEmpty()) {
         throw new IllegalArgumentException("Palette requires at least one block with a positive weight");
      } else if (filtered.size() > 255) {
         throw new IllegalArgumentException("Palette supports at most 255 blocks, got " + filtered.size());
      } else {
         List<MineBlock> entries = new ArrayList<>(filtered.size());
         long[] cumulative = new long[filtered.size()];
         long running = 0L;
         int i = 0;

         for (Entry<MineBlock, Integer> entryx : filtered.entrySet()) {
            entries.add(entryx.getKey());
            running += entryx.getValue().intValue();
            cumulative[i++] = running;
         }

         return new VirtualPalette(List.copyOf(entries), cumulative, running);
      }
   }

   public int size() {
      return this.entries.size();
   }

   public MineBlock block(int paletteId) {
      return this.entries.get(paletteId - 1);
   }

   public List<MineBlock> blocks() {
      return this.entries;
   }

   public int sample() {
      if (this.entries.size() == 1) {
         return 1;
      } else {
         long roll = ThreadLocalRandom.current().nextLong(this.totalWeight);

         for (int i = 0; i < this.cumulativeWeights.length; i++) {
            if (roll < this.cumulativeWeights[i]) {
               return i + 1;
            }
         }

         return this.cumulativeWeights.length;
      }
   }
}
