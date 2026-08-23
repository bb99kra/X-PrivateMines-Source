package dev.drawethree.xprivatemines.api.model;

import org.codemc.worldguardwrapper.region.IWrappedRegion;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;

public interface Mine {
   IWrappedRegion getRegion();

   void setRegion(IWrappedRegion var1);

   Material getSelectedMaterial();

   void setSelectedMaterial(Material var1);

   long getTotalBlockCount();

   void setTotalBlockCount(long var1);

   long getEstimatedRemainingBlocks();

   void setEstimatedRemainingBlocks(long var1);

   void decrementRemainingBlockCount();

   void handleBlockBreak(List<Block> var1);
}
