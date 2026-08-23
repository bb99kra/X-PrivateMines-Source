package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.gui.confirmation.DeleteMineGui;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.List;
import java.util.stream.Collectors;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceDeleteCmd extends PrivateMineSubCommand {
   public ForceDeleteCmd(PrivateMineCommand command) {
      super(command, "forcedelete", "forceremove");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.size() != 1) {
         return false;
      } else {
         OfflinePlayer target = Players.getOfflineNullable(args.get(0));
         if (target == null) {
            PlayerUtils.sendMessage(sender, TextUtils.applyColor("&cPlayer not found."));
            return true;
         } else {
            PrivateMine mine = this.getMinesManager().getPrivateMine(target);
            if (mine == null) {
               PlayerUtils.sendMessage(sender, TextUtils.applyColor("&cThat player does not have a mine!"));
               return true;
            } else {
               if (sender instanceof Player) {
                  new DeleteMineGui((Player)sender, mine).open();
               } else {
                  this.getMinesManager().deleteMine(sender, mine);
               }

               return true;
            }
         }
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine forcedelete [player] &7~ &fDelete a player's private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? Players.all().stream().<String>map(Player::getName).collect(Collectors.toList()) : List.of();
   }
}
