package dev.drawethree.xprivatemines.economy.impl;

import dev.drawethree.xprivatemines.economy.CurrencyProvider;
import java.math.BigDecimal;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlayerPointsCurrencyProvider implements CurrencyProvider {
   private final PlayerPointsAPI api;

   public PlayerPointsCurrencyProvider(PlayerPointsAPI api) {
      this.api = api;
   }

   @Override
   public boolean withdraw(Player player, BigDecimal amount) {
      return this.api.take(player.getUniqueId(), amount.intValue());
   }

   @Override
   public boolean deposit(Player player, BigDecimal amount) {
      return this.api.give(player.getUniqueId(), amount.intValue());
   }

   @Override
   public BigDecimal getBalance(Player player) {
      return BigDecimal.valueOf((long)this.api.look(player.getUniqueId()));
   }

   @Override
   public BigDecimal getBalance(OfflinePlayer player) {
      return BigDecimal.valueOf((long)this.api.look(player.getUniqueId()));
   }

   @Override
   public boolean has(Player player, BigDecimal amount) {
      return this.getBalance(player).compareTo(amount) >= 0;
   }

   @Override
   public String format(Player player, BigDecimal amount) {
      return this.api.lookFormatted(player.getUniqueId());
   }

   @Override
   public String getCurrencyName() {
      return "points";
   }
}
