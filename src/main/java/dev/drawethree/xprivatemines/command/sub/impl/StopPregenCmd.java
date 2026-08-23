package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.List;
import org.bukkit.command.CommandSender;

public class StopPregenCmd extends PrivateMineSubCommand {
   public StopPregenCmd(PrivateMineCommand command) {
      super(command, "pregenstop");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (!this.getMinesManager().isPregenRunning()) {
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&c⚠ No pre-generation is currently running."));
         return true;
      } else {
         this.getMinesManager().stopPregen();
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&a✔ Pre-generation process has been stopped."));
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine pregenstop &7~ &fStops the pre-generation of private mines.";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return List.of();
   }
}
