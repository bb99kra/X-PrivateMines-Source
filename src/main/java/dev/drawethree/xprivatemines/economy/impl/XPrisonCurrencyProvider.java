package dev.drawethree.xprivatemines.economy.impl;

import dev.drawethree.xprison.api.XPrisonAPI;
import dev.drawethree.xprison.api.currency.XPrisonCurrencyAPI;
import dev.drawethree.xprison.api.currency.enums.LostCause;
import dev.drawethree.xprison.api.currency.enums.ReceiveCause;
import dev.drawethree.xprison.api.currency.enums.TransactionStatus;
import dev.drawethree.xprison.api.currency.model.XPrisonCurrency;
import dev.drawethree.xprivatemines.economy.CurrencyProvider;
import java.math.BigDecimal;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class XPrisonCurrencyProvider implements CurrencyProvider {
   private final XPrisonCurrency currency;

   public XPrisonCurrencyProvider(XPrisonCurrency currency) {
      this.currency = currency;
   }

   private XPrisonCurrencyAPI api() {
      return XPrisonAPI.getInstance().getCurrencyApi();
   }

   @Override
   public boolean withdraw(Player player, BigDecimal amount) {
      TransactionStatus status = this.api().tryRemoveBalance(player, this.currency.getName(), amount, LostCause.RANKUP);
      return status == TransactionStatus.SUCCESS;
   }

   @Override
   public boolean deposit(Player player, BigDecimal amount) {
      return this.api().addBalance(player, this.currency.getName(), amount, ReceiveCause.GIVE);
   }

   @Override
   public BigDecimal getBalance(Player player) {
      return this.api().getBalanceExact(player, this.currency.getName());
   }

   @Override
   public boolean has(Player player, BigDecimal amount) {
      return this.api().has(player, this.currency.getName(), amount);
   }

   @Override
   public String format(Player player, BigDecimal amount) {
      return this.currency.format(amount);
   }

   @Override
   public BigDecimal getBalance(OfflinePlayer player) {
      return this.api().getBalanceExact(player, this.currency.getName());
   }

   @Override
   public String getCurrencyName() {
      String displayName = this.currency.getDisplayName();
      return displayName != null && !displayName.isBlank() ? displayName : this.currency.getName();
   }
}
