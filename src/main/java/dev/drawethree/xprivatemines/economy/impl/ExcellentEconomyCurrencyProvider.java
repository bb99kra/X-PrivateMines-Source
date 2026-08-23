package dev.drawethree.xprivatemines.economy.impl;

import dev.drawethree.xprivatemines.economy.CurrencyProvider;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.api.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationResult;

public final class ExcellentEconomyCurrencyProvider implements CurrencyProvider {
   private final ExcellentEconomyAPI api;
   private final ExcellentCurrency currency;

   public ExcellentEconomyCurrencyProvider(ExcellentEconomyAPI api, ExcellentCurrency currency) {
      this.api = api;
      this.currency = currency;
   }

   @Override
   public boolean withdraw(Player player, BigDecimal amount) {
      double amt = amount.doubleValue();
      if (player.isOnline()) {
         return this.api.withdraw(player, this.currency, amt, this.createContext());
      } else {
         try {
            return ((OperationResult)this.api.withdrawAsync(player.getUniqueId(), this.currency, amt, this.createContext()).get()).bool();
         } catch (ExecutionException | InterruptedException var6) {
            throw new RuntimeException(var6);
         }
      }
   }

   @Override
   public boolean deposit(Player player, BigDecimal amount) {
      double amt = amount.doubleValue();
      if (player.isOnline()) {
         return this.api.deposit(player, this.currency, amt, this.createContext());
      } else {
         try {
            return ((OperationResult)this.api.depositAsync(player.getUniqueId(), this.currency, amt, this.createContext()).get()).bool();
         } catch (ExecutionException | InterruptedException var6) {
            throw new RuntimeException(var6);
         }
      }
   }

   @Override
   public BigDecimal getBalance(Player player) {
      if (player == null) {
         return BigDecimal.ZERO;
      } else if (player.isOnline()) {
         return BigDecimal.valueOf(this.api.getBalance(player, this.currency));
      } else {
         try {
            return BigDecimal.valueOf((Double)this.api.getBalanceAsync(player.getUniqueId(), this.currency).get());
         } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   @Override
   public BigDecimal getBalance(OfflinePlayer player) {
      if (player.isOnline()) {
         return this.getBalance(player.getPlayer());
      } else {
         try {
            return BigDecimal.valueOf((Double)this.api.getBalanceAsync(player.getUniqueId(), this.currency).get());
         } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   @Override
   public boolean has(Player player, BigDecimal amount) {
      return this.getBalance(player).compareTo(amount) >= 0;
   }

   @Override
   public String format(Player player, BigDecimal amount) {
      return this.currency.format(amount.doubleValue());
   }

   @Override
   public String getCurrencyName() {
      String name = this.currency.getName();
      return name != null && !name.isBlank() ? name : "money";
   }

   private OperationContext createContext() {
      return OperationContext.custom("X-PrivateMines")
         .silentFor(new NotificationTarget[]{NotificationTarget.USER, NotificationTarget.EXECUTOR, NotificationTarget.CONSOLE_LOGGER});
   }
}
