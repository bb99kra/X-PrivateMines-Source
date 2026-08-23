package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.manager.CooldownManager;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.cooldown.CooldownType;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetCmd extends PrivateMineSubCommand {
   public ResetCmd(PrivateMineCommand command) {
      super(command, "reset");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (!args.isEmpty()) {
         return false;
      } else if (sender instanceof Player p) {
         PrivateMineImpl mine = this.getMinesManager().getPrivateMineInternal(p);
         if (mine == null) {
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("no-mine"));
            return true;
         } else if (CooldownManager.INSTANCE.hasCooldown(CooldownType.MINE_RESET, p)) {
            PlayerUtils.sendMessage(
               p,
               this.getMessageConfig()
                  .getMessage("mine-reset-cooldown")
                  .replace("%time%", String.valueOf(CooldownManager.INSTANCE.getRemainingTime(CooldownType.MINE_RESET, p)))
            );
            SoundUtils.playError(p);
            return true;
         } else {
            this.getMinesManager().refill(mine, sender);
            return true;
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine reset &7~ &fReset your private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return new ArrayList<>();
   }
}
