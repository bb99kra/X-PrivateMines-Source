package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCmd extends PrivateMineSubCommand {
   public ClaimCmd(PrivateMineCommand command) {
      super(command, "claim");
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
         } else if (mine.getUnclaimedMoney() <= 0.0) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-unclaimed-money"));
            return true;
         } else {
            this.getCommand().getPlugin().getEconomyManager().deposit(p, mine.getUnclaimedMoney());
            PlayerUtils.sendMessage(
               p,
               this.getMessageConfig()
                  .getMessage("mine-claimed")
                  .replace("%money%", this.getCommand().getPlugin().getEconomyManager().format(p, mine.getUnclaimedMoney()))
            );
            mine.setUnclaimedMoney(0.0);
            return true;
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine claim &7~ &fClaim earned money from mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return new ArrayList<>();
   }
}
