package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetNameCmd extends PrivateMineSubCommand {
   public SetNameCmd(PrivateMineCommand command) {
      super(command, "setname");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else if (args.isEmpty()) {
            String current = mine.getMineName();
            String display = current != null ? current : "&7(none)";
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("your-mine-name").replace("%name%", display));
            return true;
         } else if (args.get(0).equalsIgnoreCase("clear")) {
            mine.setMineName(null);
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-name-cleared"));
            return true;
         } else {
            String name = String.join(" ", args);
            int max = this.getConfig().getMaxMineName();
            if (name.length() > max) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-name-too-long").replace("%max%", String.valueOf(max)));
               return true;
            } else {
               mine.setMineName(name);
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("mine-name-set").replace("%name%", name));
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
      return "&e⚠ [&6XPrivateMines&e] &c/pmine setname [name|clear] &7~ &fSet a custom display name for your mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? List.of("clear") : List.of();
   }
}
