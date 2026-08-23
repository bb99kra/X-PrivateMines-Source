package dev.drawethree.xprivatemines.api.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface MineEconomyProvider {
   boolean withdraw(Player var1, double var2);

   boolean deposit(Player var1, double var2);

   double getBalance(Player var1);

   default double getBalance(OfflinePlayer player) {
      Player online = player.getPlayer();
      return online != null ? this.getBalance(online) : 0.0;
   }

   boolean has(Player var1, double var2);

   String format(Player var1, double var2);
}
