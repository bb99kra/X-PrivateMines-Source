package dev.drawethree.xprivatemines.mines.model;

import org.codemc.worldguardwrapper.region.IWrappedRegion;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.Mine;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.region.RegionUtils;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class MineImpl implements Mine {
   private IWrappedRegion region;
   private MineBlock selectedBlock;
   private long totalBlockCount;
   private long estimatedRemainingBlocks;
   private volatile VirtualMineStore virtualStore;

   MineImpl() {
   }

   MineImpl(PrivateMineImpl mine, ConfigurationSection config) {
      this.loadFromConfig(mine, config);
   }

   private void loadFromConfig(PrivateMineImpl mine, ConfigurationSection config) {
      try {
         this.selectedBlock = MineBlock.parse(config.getString("mine-material"));
         this.region = (IWrappedRegion)XPrivateMines.getInstance()
            .getWorldGuardWrapper()
            .getRegion(XPrivateMines.getInstance().getPrivateMinesConfig().getMinesWorld(), mine.getUuid().toString() + "_mine")
            .orElse(null);
         if (this.region != null) {
            this.totalBlockCount = RegionUtils.getTotalBlockCountInRegion(this.region);
         }

         this.estimatedRemainingBlocks = this.totalBlockCount;
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   @Override
   public Material getSelectedMaterial() {
      return this.selectedBlock != null && !this.selectedBlock.isCustom() ? this.selectedBlock.getXMaterial().parseMaterial() : null;
   }

   @Override
   public void setSelectedMaterial(Material material) {
      this.selectedBlock = material == null ? null : MineBlock.parse(material.name());
   }

   @Override
   public synchronized void decrementRemainingBlockCount() {
      if (this.virtualStore == null) {
         this.estimatedRemainingBlocks = Math.max(0L, this.estimatedRemainingBlocks - 1L);
      }
   }

   @Override
   public synchronized void handleBlockBreak(List<Block> blocks) {
      if (this.virtualStore == null) {
         this.estimatedRemainingBlocks = Math.max(0L, this.estimatedRemainingBlocks - blocks.size());
      }
   }

   @Override
   public long getEstimatedRemainingBlocks() {
      VirtualMineStore store = this.virtualStore;
      return store != null ? store.remaining() : this.estimatedRemainingBlocks;
   }

   @Override
   public void setEstimatedRemainingBlocks(long estimatedRemainingBlocks) {
      if (this.virtualStore == null) {
         this.estimatedRemainingBlocks = estimatedRemainingBlocks;
      }
   }

   @Generated
   @Override
   public void setRegion(IWrappedRegion region) {
      this.region = region;
   }

   @Generated
   public void setSelectedBlock(MineBlock selectedBlock) {
      this.selectedBlock = selectedBlock;
   }

   @Generated
   @Override
   public void setTotalBlockCount(long totalBlockCount) {
      this.totalBlockCount = totalBlockCount;
   }

   @Generated
   public void setVirtualStore(VirtualMineStore virtualStore) {
      this.virtualStore = virtualStore;
   }

   @Generated
   @Override
   public IWrappedRegion getRegion() {
      return this.region;
   }

   @Generated
   public MineBlock getSelectedBlock() {
      return this.selectedBlock;
   }

   @Generated
   @Override
   public long getTotalBlockCount() {
      return this.totalBlockCount;
   }

   @Generated
   public VirtualMineStore getVirtualStore() {
      return this.virtualStore;
   }
}
