package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public class HelpCmd extends PrivateMineSubCommand {
   public HelpCmd(PrivateMineCommand command) {
      super(command, "help", "?");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      for (PrivateMineSubCommand subCommand : this.command.getSubCommands()) {
         if (subCommand != this && subCommand.canExecute(sender)) {
            PlayerUtils.sendMessage(sender, subCommand.getUsage());
         }
      }

      return true;
   }

   @Override
   public boolean canExecute(CommandSender sender) {
      return true;
   }

   @Override
   public String getUsage() {
      return "";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return new ArrayList<>();
   }
}
