package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCmd extends PrivateMineSubCommand {
   public CreateCmd(PrivateMineCommand command) {
      super(command, "create", "new");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.size() != 1) {
         return false;
      } else if (sender instanceof Player p) {
         PrivateMine mine = this.getMinesManager().getPrivateMine(p);
         if (mine != null) {
            PlayerUtils.sendMessage(sender, this.getMessageConfig().getMessage("mine-exists"));
            if (this.getConfig().isTeleportExistingOnCreate()) {
               mine.teleport(p);
            }

            return true;
         } else {
            MinesSchematic schematic = this.getMinesManager().getSchematic(args.get(0));
            if (schematic == null) {
               PlayerUtils.sendMessage(sender, this.getMessageConfig().getMessage("mine-invalid-schematic").replace("%name%", args.get(0)));
               return true;
            } else if (!p.hasPermission(schematic.getSettings().getPermission())) {
               PlayerUtils.sendMessage(sender, this.getMessageConfig().getMessage("no-perm"));
               return true;
            } else {
               this.getMinesManager().createPrivateMine(p, schematic);
               PlayerUtils.sendMessage(sender, this.getMessageConfig().getMessage("mine-generation-started"));
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
      return "&e⚠ [&6XPrivateMines&e] &c/pmine create [schematic_name] &7~ &fCreate new private mine with given schematic";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? this.getMinesManager().getAllSchematicNames() : List.of();
   }
}
