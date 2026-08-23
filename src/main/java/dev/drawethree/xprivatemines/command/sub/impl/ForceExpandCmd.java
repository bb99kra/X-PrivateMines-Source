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

public class ForceExpandCmd extends PrivateMineSubCommand {
   public ForceExpandCmd(PrivateMineCommand command) {
      super(command, "forceexpand");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.isEmpty()) {
         return false;
      } else {
         int expandLevel = -1;
         OfflinePlayer target = Players.getOfflineNullable(args.get(0));
         if (target == null) {
            PlayerUtils.sendMessage(sender, "&cUnknown player.");
            return true;
         } else {
            if (args.size() == 2) {
               try {
                  expandLevel = Integer.parseInt(args.get(1));
               } catch (Exception var6) {
                  PlayerUtils.sendMessage(sender, "&cInvalid number.");
                  return true;
               }
            }

            PrivateMine mine = this.getMinesManager().getPrivateMine(target);
            if (mine == null) {
               PlayerUtils.sendMessage(sender, "&cThat player does not have private mine");
               return true;
            } else {
               if (this.getMinesManager().forceExpand(sender, mine, expandLevel)) {
                  PlayerUtils.sendMessage(sender, "&aSuccessfully expanded " + target.getName() + "'s private mine.");
               }

               return true;
            }
         }
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine forceexpand [player] <expand_level> &7~ &fSet the expand level on player private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? Players.all().stream().<String>map(Player::getName).collect(Collectors.toList()) : List.of();
   }
}
