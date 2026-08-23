package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import org.bukkit.command.CommandSender;

public interface MineRefillService {
   void refill(PrivateMineImpl var1);

   void refill(PrivateMineImpl var1, CommandSender var2);

   boolean shouldReset(PrivateMineImpl var1);
}
