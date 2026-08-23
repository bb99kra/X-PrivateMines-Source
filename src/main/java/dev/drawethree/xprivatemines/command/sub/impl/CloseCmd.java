package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseCmd extends PrivateMineSubCommand {
   public CloseCmd(PrivateMineCommand command) {
      super(command, "close");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (!args.isEmpty()) {
         return false;
      } else if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else if (!mine.isOpen()) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-already-closed"));
            return true;
         } else {
            this.getMinesManager().getAccessService().closePrivateMine(mine);
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-close"));
            return true;
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine close &7~ &fClose your mine to public";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return new ArrayList<>();
   }
}
