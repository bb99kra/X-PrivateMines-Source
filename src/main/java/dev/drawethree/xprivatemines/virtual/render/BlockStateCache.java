package dev.drawethree.xprivatemines.virtual.render;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockStateCache {
   public static final int AIR_GLOBAL_ID = 0;
   private final Map<MineBlock, WrappedBlockState> cache = new ConcurrentHashMap<>();

   public WrappedBlockState stateFor(MineBlock block) {
      return this.cache.computeIfAbsent(block, b -> SpigotConversionUtil.fromBukkitBlockData(b.toBlockData()));
   }

   public int globalIdOf(MineBlock block) {
      return block == null ? 0 : this.stateFor(block).getGlobalId();
   }

   public void warmUp(Iterable<MineBlock> blocks) {
      for (MineBlock block : blocks) {
         this.stateFor(block);
      }
   }
}
