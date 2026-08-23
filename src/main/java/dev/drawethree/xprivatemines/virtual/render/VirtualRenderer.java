package dev.drawethree.xprivatemines.virtual.render;

import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import org.bukkit.entity.Player;

public interface VirtualRenderer {
   void resyncBlock(Player var1, VirtualMineStore var2, int var3, int var4, int var5);

   void acknowledgeDig(Player var1, int var2);

   void broadcastBlock(VirtualMineStore var1, int var2, int var3, int var4);

   void broadcastAir(VirtualMineStore var1, int var2, int var3, int var4);

   void broadcastBreakEffect(VirtualMineStore var1, int var2, int var3, int var4, MineBlock var5, Player var6);

   void resendRegion(VirtualMineStore var1);

   default void clearArea(VirtualMineStore viewerStore, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
   }

   default void discardPendingUpdates(VirtualMineStore store) {
   }

   void warmUp(Iterable<MineBlock> var1);
}
