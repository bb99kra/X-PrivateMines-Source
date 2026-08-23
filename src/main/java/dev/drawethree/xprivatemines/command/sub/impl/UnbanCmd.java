package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import java.util.stream.Collectors;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanCmd extends PrivateMineSubCommand {
   public UnbanCmd(PrivateMineCommand command) {
      super(command, "unban");
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
            OfflinePlayer target = Players.getOfflineNullable(args.get(0));
            if (target.getUniqueId().equals(p.getUniqueId())) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("cant-unban-yourself"));
               return true;
            } else if (!mine.isBanned(target)) {
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("player-not-banned"));
               return true;
            } else {
               this.getMinesManager().unbanPlayer(mine, target);
               PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("player-unbanned").replace("%player%", target.getName()));
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
      return "&e⚠ [&6XPrivateMines&e] &c/pmine unban [player] &7~ &fUnban a player for your private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? Players.all().stream().<String>map(Player::getName).collect(Collectors.toList()) : List.of();
   }
}
