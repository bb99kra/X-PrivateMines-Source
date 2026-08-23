package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;

public class StatusCmd extends PrivateMineSubCommand {
   public StatusCmd(PrivateMineCommand command) {
      super(command, "status");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      Collection<PrivateMine> mines = this.getMinesManager().getAll();
      long total = mines.size();
      long owned = mines.stream().filter(pm -> pm.getOwner() != null).count();
      long unowned = total - owned;
      PlayerUtils.sendMessage(sender, "§e[XPrivateMines] Status:");
      PlayerUtils.sendMessage(sender, "§7Total Private Mines: §f" + total);
      PlayerUtils.sendMessage(sender, "§7Owned Mines: §a" + owned);
      PlayerUtils.sendMessage(sender, "§7Unowned Mines: §c" + unowned);
      return true;
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine status &7~ &fShows the count of private mines.";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return List.of();
   }
}
