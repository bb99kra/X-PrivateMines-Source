package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaxCmd extends PrivateMineSubCommand {
   public TaxCmd(PrivateMineCommand command) {
      super(command, "tax");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else if (args.isEmpty()) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("your-tax").replace("%tax%", String.format("%,.2f", mine.getTax())));
            return true;
         } else if (args.size() == 1) {
            try {
               double newTax = Double.parseDouble(args.get(0));
               if (newTax >= this.getConfig().getMinTax() && newTax <= this.getConfig().getMaxTax()) {
                  mine.setTax(newTax);
                  PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("tax-set").replace("%tax%", String.format("%,.2f", newTax)));
                  return true;
               } else {
                  PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("invalid-tax"));
                  return true;
               }
            } catch (Exception var8) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("invalid-tax"));
               return true;
            }
         } else {
            return false;
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine tax [tax] &7~ &fSet tax percentage for your private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? List.of("0", "5", "10", "25", "50") : List.of();
   }
}
