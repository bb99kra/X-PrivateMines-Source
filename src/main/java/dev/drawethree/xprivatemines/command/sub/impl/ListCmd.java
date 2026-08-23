package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCmd extends PrivateMineSubCommand {
   public ListCmd(PrivateMineCommand command) {
      super(command, "list");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (!args.isEmpty()) {
         return false;
      } else if (sender instanceof Player p) {
         this.getMinesManager().showPrivateMineList(p);
         return true;
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine list &7~ &fOpens Private Mines GUI for visiting";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return new ArrayList<>();
   }
}
