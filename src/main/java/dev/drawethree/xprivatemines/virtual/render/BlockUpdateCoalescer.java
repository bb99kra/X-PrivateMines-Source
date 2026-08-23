package dev.drawethree.xprivatemines.virtual.render;

import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public final class BlockUpdateCoalescer {
   static final int EFFECT_CAP = 12;
   private final BlockUpdateCoalescer.Flusher flusher;
   private final Consumer<Runnable> flushScheduler;
   private final BooleanSupplier mainThreadCheck;
   private final Map<UUID, BlockUpdateCoalescer.StoreBatch> pending = new LinkedHashMap<>();
   private boolean flushScheduled;

   public BlockUpdateCoalescer(BlockUpdateCoalescer.Flusher flusher, Consumer<Runnable> flushScheduler, BooleanSupplier mainThreadCheck) {
      this.flusher = flusher;
      this.flushScheduler = flushScheduler;
      this.mainThreadCheck = mainThreadCheck;
   }

   public void enqueueBlock(VirtualMineStore store, int x, int y, int z) {
      if (!this.mainThreadCheck.getAsBoolean()) {
         this.flushImmediately(store, x, y, z);
      } else {
         this.batchOf(store).dirtyPositions.add(packPosition(x, y, z));
         this.scheduleFlush();
      }
   }

   public void enqueueEffect(VirtualMineStore store, int x, int y, int z, MineBlock block, Player except) {
      if (this.mainThreadCheck.getAsBoolean()) {
         BlockUpdateCoalescer.StoreBatch batch = this.batchOf(store);
         if (batch.effects.size() < 12) {
            batch.effects.add(new BlockUpdateCoalescer.EffectEntry(x, y, z, block, except));
         }

         this.scheduleFlush();
      }
   }

   public void flush() {
      this.flushScheduled = false;
      if (!this.pending.isEmpty()) {
         List<BlockUpdateCoalescer.StoreBatch> batches = new ArrayList<>(this.pending.values());
         this.pending.clear();

         for (BlockUpdateCoalescer.StoreBatch batch : batches) {
            try {
               this.flusher.flush(batch);
            } catch (Throwable var5) {
               PrivateMinesLogger.warning("Packet-mines batch flush failed for mine " + batch.store.getMine().getUuid() + ": " + var5);
               var5.printStackTrace();
            }
         }
      }
   }

   public void discardPending(VirtualMineStore store) {
      this.pending.remove(store.getMine().getUuid());
   }

   private BlockUpdateCoalescer.StoreBatch batchOf(VirtualMineStore store) {
      return this.pending.computeIfAbsent(store.getMine().getUuid(), id -> new BlockUpdateCoalescer.StoreBatch(store));
   }

   private void scheduleFlush() {
      if (!this.flushScheduled) {
         this.flushScheduled = true;
         this.flushScheduler.accept(this::flush);
      }
   }

   private void flushImmediately(VirtualMineStore store, int x, int y, int z) {
      PrivateMinesLogger.warning("Packet-mines: block update enqueued off the main thread - flushing immediately (report this).");
      BlockUpdateCoalescer.StoreBatch single = new BlockUpdateCoalescer.StoreBatch(store);
      single.dirtyPositions.add(packPosition(x, y, z));
      this.flusher.flush(single);
   }

   public static Map<Long, List<Long>> groupBySection(Iterable<Long> positions) {
      Map<Long, List<Long>> bySection = new LinkedHashMap<>();

      for (long pos : positions) {
         long sectionKey = packPosition(unpackX(pos) >> 4, unpackY(pos) >> 4, unpackZ(pos) >> 4);
         bySection.computeIfAbsent(sectionKey, key -> new ArrayList<>()).add(pos);
      }

      return bySection;
   }

   public static long packPosition(int x, int y, int z) {
      return (long)(x & 67108863) << 38 | (long)(z & 67108863) << 12 | y & 4095;
   }

   public static int unpackX(long packed) {
      return (int)(packed >> 38);
   }

   public static int unpackZ(long packed) {
      return (int)(packed << 26 >> 38);
   }

   public static int unpackY(long packed) {
      return (int)(packed << 52 >> 52);
   }

   public record EffectEntry(int x, int y, int z, MineBlock block, Player except) {
   }

   @FunctionalInterface
   public interface Flusher {
      void flush(BlockUpdateCoalescer.StoreBatch var1);
   }

   public static final class StoreBatch {
      private final VirtualMineStore store;
      private final Set<Long> dirtyPositions = new LinkedHashSet<>();
      private final List<BlockUpdateCoalescer.EffectEntry> effects = new ArrayList<>();

      StoreBatch(VirtualMineStore store) {
         this.store = store;
      }

      public VirtualMineStore store() {
         return this.store;
      }

      public Set<Long> dirtyPositions() {
         return this.dirtyPositions;
      }

      public List<BlockUpdateCoalescer.EffectEntry> effects() {
         return this.effects;
      }
   }
}
