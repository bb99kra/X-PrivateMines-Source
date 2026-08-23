package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import java.util.stream.Collectors;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCmd extends PrivateMineSubCommand {
   public KickCmd(PrivateMineCommand command) {
      super(command, "kick");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.size() != 1) {
         return false;
      } else if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else {
            Player target = Players.getNullable(args.get(0));
            if (target == null) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("player-not-online"));
               return true;
            } else if (target.getUniqueId().equals(p.getUniqueId())) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("cant-kick-yourself"));
               return true;
            } else if (!mine.isInPrivateMine(target.getLocation())) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("player-not-in-mine"));
               return true;
            } else {
               this.getMinesManager().kickPlayer(target);
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("player-kicked").replace("%player%", target.getName()));
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
      return "&e⚠ [&6XPrivateMines&e] &c/pmine kick [player] &7~ &fKicks a player from your private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? Players.all().stream().<String>map(Player::getName).collect(Collectors.toList()) : List.of();
   }
}
