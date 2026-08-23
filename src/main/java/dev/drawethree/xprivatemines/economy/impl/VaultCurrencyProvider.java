package dev.drawethree.xprivatemines.economy.impl;

import dev.drawethree.xprivatemines.economy.CurrencyProvider;
import java.math.BigDecimal;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class VaultCurrencyProvider implements CurrencyProvider {
   private final Economy vault;

   public VaultCurrencyProvider(Economy vault) {
      this.vault = vault;
   }

   @Override
   public boolean withdraw(Player player, BigDecimal amount) {
      return this.vault.withdrawPlayer(player, amount.doubleValue()).transactionSuccess();
   }

   @Override
   public boolean deposit(Player player, BigDecimal amount) {
      return this.vault.depositPlayer(player, amount.doubleValue()).transactionSuccess();
   }

   @Override
   public BigDecimal getBalance(Player player) {
      return BigDecimal.valueOf(this.vault.getBalance(player));
   }

   @Override
   public BigDecimal getBalance(OfflinePlayer player) {
      return BigDecimal.valueOf(this.vault.getBalance(player));
   }

   @Override
   public boolean has(Player player, BigDecimal amount) {
      return this.vault.has(player, amount.doubleValue());
   }

   @Override
   public String format(Player player, BigDecimal amount) {
      return this.vault.format(amount.doubleValue());
   }

   @Override
   public String getCurrencyName() {
      String plural = this.vault.currencyNamePlural();
      if (plural != null && !plural.isBlank()) {
         return plural;
      } else {
         String singular = this.vault.currencyNameSingular();
         return singular != null && !singular.isBlank() ? singular : "money";
      }
   }
}
