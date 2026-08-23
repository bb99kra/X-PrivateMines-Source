package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.List;
import java.util.stream.Collectors;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceCreateCmd extends PrivateMineSubCommand {
   public ForceCreateCmd(PrivateMineCommand command) {
      super(command, "forcecreate", "forcenew");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.size() != 2) {
         return false;
      } else {
         OfflinePlayer target = Players.getOfflineNullable(args.get(0));
         if (target == null) {
            PlayerUtils.sendMessage(sender, "&cUnknown player.");
            return true;
         } else {
            PrivateMine mine = this.getMinesManager().getPrivateMine(target);
            if (mine != null) {
               PlayerUtils.sendMessage(sender, "&cThat player already has a private mine");
               return true;
            } else {
               MinesSchematic schematic = this.getMinesManager().getSchematic(args.get(1));
               if (schematic == null) {
                  PlayerUtils.sendMessage(sender, this.getMessageConfig().getMessage("mine-invalid-schematic").replace("%name%", args.get(1)));
                  return true;
               } else {
                  this.getMinesManager().createPrivateMine(target, schematic).whenComplete((ignored, throwable) -> {
                     if (throwable != null) {
                        PlayerUtils.sendMessage(sender, TextUtils.applyColor("&cAn error occurred while creating player mine. Check console."));
                        throwable.printStackTrace();
                     } else {
                        PlayerUtils.sendMessage(sender, TextUtils.applyColor("&aSuccessfully created private mine for " + target.getName() + "."));
                     }
                  });
                  return true;
               }
            }
         }
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine forcecreate [player] [schematic_name] &7~ &fCreates a private mine for player";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      if (args.size() == 1) {
         return Players.all().stream().<String>map(Player::getName).collect(Collectors.toList());
      } else {
         return args.size() == 2 ? this.getMinesManager().getAllSchematicNames() : List.of();
      }
   }
}
