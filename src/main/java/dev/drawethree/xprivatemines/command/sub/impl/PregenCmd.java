package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.List;
import org.bukkit.command.CommandSender;

public class PregenCmd extends PrivateMineSubCommand {
   public PregenCmd(PrivateMineCommand command) {
      super(command, "pregen");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.size() != 2) {
         return false;
      } else {
         MinesSchematic schematic = this.getMinesManager().getSchematic(args.get(0));
         if (schematic == null) {
            PlayerUtils.sendMessage(sender, this.getMessageConfig().getMessage("mine-invalid-schematic").replace("%name%", args.get(0)));
            return true;
         } else {
            int amount;
            try {
               amount = Integer.parseInt(args.get(1));
               if (amount <= 0) {
                  PlayerUtils.sendMessage(sender, "§cInvalid amount.");
                  return true;
               }

               if (amount > 1000) {
                  PlayerUtils.sendMessage(sender, TextUtils.applyColor("&cYou can only pre-generate up to 1000 mines at a time!"));
                  return true;
               }
            } catch (NumberFormatException var6) {
               PlayerUtils.sendMessage(sender, "§c" + args.get(1) + "is not a number");
               return true;
            }

            this.getMinesManager().pregen(sender, schematic, amount);
            return true;
         }
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine pregen [schematic_name] [amount] &7~ &fPre-generate a number of private mines";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      if (args.size() == 1) {
         return this.getMinesManager().getAllSchematicNames();
      } else {
         return args.size() == 2 ? List.of("10", "50", "100", "500") : List.of();
      }
   }
}
