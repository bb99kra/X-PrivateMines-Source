package dev.drawethree.xprivatemines.mines.model.settings;

import dev.drawethree.xprivatemines.api.model.MineTier;
import lombok.Generated;

public class MineSettings {
   private int expandLevel;
   private int resetPercentage;
   private MineTier tier;

   public MineSettings(int expandLevel, int resetPercentage, MineTier tier) {
      this.expandLevel = expandLevel;
      this.resetPercentage = resetPercentage;
      this.tier = tier;
   }

   @Generated
   public int getExpandLevel() {
      return this.expandLevel;
   }

   @Generated
   public int getResetPercentage() {
      return this.resetPercentage;
   }

   @Generated
   public MineTier getTier() {
      return this.tier;
   }

   @Generated
   public void setExpandLevel(int expandLevel) {
      this.expandLevel = expandLevel;
   }

   @Generated
   public void setResetPercentage(int resetPercentage) {
      this.resetPercentage = resetPercentage;
   }

   @Generated
   public void setTier(MineTier tier) {
      this.tier = tier;
   }
}
