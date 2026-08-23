package dev.drawethree.xprivatemines.economy;

import java.math.BigDecimal;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface CurrencyProvider {
   boolean withdraw(Player var1, BigDecimal var2);

   boolean deposit(Player var1, BigDecimal var2);

   BigDecimal getBalance(Player var1);

   default BigDecimal getBalance(OfflinePlayer player) {
      Player online = player.getPlayer();
      return online != null ? this.getBalance(online) : BigDecimal.ZERO;
   }

   boolean has(Player var1, BigDecimal var2);

   String format(Player var1, BigDecimal var2);

   String getCurrencyName();
}
