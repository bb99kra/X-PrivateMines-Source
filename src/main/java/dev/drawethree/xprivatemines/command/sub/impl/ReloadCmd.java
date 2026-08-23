package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ReloadCmd extends PrivateMineSubCommand {
   public ReloadCmd(PrivateMineCommand command) {
      super(command, "reload");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      this.getCommand().getPlugin().reload();
      PlayerUtils.sendMessage(
         sender, "&d\ud83d\udd04 [&6XPrivateMines&d] Plugin reloaded! &aEverything is back in business. Happy mining, legend! \ud83d\udc8e⛏"
      );
      return true;
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine reload &7~ &fReloads cached plugin configurations";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return new ArrayList<>();
   }
}
