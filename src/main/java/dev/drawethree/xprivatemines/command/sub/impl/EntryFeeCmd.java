package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EntryFeeCmd extends PrivateMineSubCommand {
   public EntryFeeCmd(PrivateMineCommand command) {
      super(command, "entryfee");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else if (args.isEmpty()) {
            PlayerUtils.sendMessage(
               p,
               this.getMessageConfig()
                  .getMessage("your-entry-fee")
                  .replace("%fee%", this.command.getPlugin().getEconomyManager().format(p, mine.getEntryFee()))
            );
            return true;
         } else if (args.size() == 1) {
            try {
               double newFee = Double.parseDouble(args.get(0));
               if (newFee >= this.getConfig().getMinEntryFee() && newFee <= this.getConfig().getMaxEntryFee()) {
                  mine.setEntryFee(newFee);
                  PlayerUtils.sendMessage(
                     p,
                     this.getMessageConfig().getMessage("fee-set").replace("%fee%", this.command.getPlugin().getEconomyManager().format(p, mine.getEntryFee()))
                  );
                  return true;
               } else {
                  PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("invalid-fee"));
                  return true;
               }
            } catch (Exception var8) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("invalid-fee"));
               return true;
            }
         } else {
            return true;
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine entryfee [fee] &7~ &fSet entry fee of your mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? List.of("0", "1000", "5000", "10000", "50000") : List.of();
   }
}
