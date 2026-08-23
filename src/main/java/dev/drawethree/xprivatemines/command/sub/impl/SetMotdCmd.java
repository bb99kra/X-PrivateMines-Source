package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMotdCmd extends PrivateMineSubCommand {
   public SetMotdCmd(PrivateMineCommand command) {
      super(command, "setmotd");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else if (args.isEmpty()) {
            String current = mine.getMineMotd();
            String display = current != null ? current : "&7(none)";
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("your-mine-motd").replace("%motd%", display));
            return true;
         } else if (args.get(0).equalsIgnoreCase("clear")) {
            mine.setMineMotd(null);
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-motd-cleared"));
            return true;
         } else {
            String motd = String.join(" ", args);
            int max = this.getConfig().getMaxMineMotd();
            if (motd.length() > max) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-motd-too-long").replace("%max%", String.valueOf(max)));
               return true;
            } else {
               mine.setMineMotd(motd);
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-motd-set"));
               return true;
            }
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine setmotd [message|clear] &7~ &fSet a message shown to players entering your mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? List.of("clear") : List.of();
   }
}
