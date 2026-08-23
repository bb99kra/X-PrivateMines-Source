package dev.drawethree.xprivatemines.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntSupplier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class BreakLedger {
   private static final int SHRINK_THRESHOLD = 8192;
   private final IntSupplier tickSupplier;
   private Set<BreakLedger.Position> counted = new HashSet<>();
   private int tick = Integer.MIN_VALUE;

   public BreakLedger() {
      this(() -> Bukkit.getServer().getCurrentTick());
   }

   BreakLedger(IntSupplier tickSupplier) {
      this.tickSupplier = tickSupplier;
   }

   public List<Block> takeUncounted(Collection<Block> blocks) {
      this.rollOverIfNewTick();
      List<Block> fresh = new ArrayList<>(blocks.size());

      for (Block block : blocks) {
         if (block != null && this.counted.add(BreakLedger.Position.of(block))) {
            fresh.add(block);
         }
      }

      return fresh;
   }

   public boolean isCounted(Block block) {
      this.rollOverIfNewTick();
      return block != null && this.counted.contains(BreakLedger.Position.of(block));
   }

   private void rollOverIfNewTick() {
      int now = this.tickSupplier.getAsInt();
      if (now != this.tick) {
         this.tick = now;
         if (this.counted.size() > 8192) {
            this.counted = new HashSet<>();
         } else {
            this.counted.clear();
         }
      }
   }

   private record Position(UUID world, int x, int y, int z) {
      static BreakLedger.Position of(Block block) {
         World world = block.getWorld();
         return new BreakLedger.Position(world == null ? null : world.getUID(), block.getX(), block.getY(), block.getZ());
      }
   }
}
