package dev.drawethree.xprivatemines.mines.model.settings;

import lombok.Generated;

public class EconomySettings {
   private double entryFee;
   private double tax;
   private double unclaimedMoney;

   public EconomySettings(double entryFee, double tax, double unclaimedMoney) {
      this.entryFee = entryFee;
      this.tax = tax;
      this.unclaimedMoney = unclaimedMoney;
   }

   @Generated
   public double getEntryFee() {
      return this.entryFee;
   }

   @Generated
   public double getTax() {
      return this.tax;
   }

   @Generated
   public double getUnclaimedMoney() {
      return this.unclaimedMoney;
   }

   @Generated
   public void setEntryFee(double entryFee) {
      this.entryFee = entryFee;
   }

   @Generated
   public void setTax(double tax) {
      this.tax = tax;
   }

   @Generated
   public void setUnclaimedMoney(double unclaimedMoney) {
      this.unclaimedMoney = unclaimedMoney;
   }
}
