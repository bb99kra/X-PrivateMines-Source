package dev.drawethree.xprivatemines.command.sub.impl;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSetBlockCmd extends PrivateMineSubCommand {
   public ForceSetBlockCmd(PrivateMineCommand command) {
      super(command, "forcesetblock");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.isEmpty()) {
         return false;
      } else {
         OfflinePlayer target = Players.getOfflineNullable(args.get(0));
         if (target == null) {
            PlayerUtils.sendMessage(sender, "&cUnknown player.");
            return true;
         } else {
            String blockId = args.size() == 2 ? args.get(1) : null;
            PrivateMine mine = this.getMinesManager().getPrivateMine(target);
            if (mine == null) {
               PlayerUtils.sendMessage(sender, "&cThat player does not have private mine");
               return true;
            } else {
               if (this.getMinesManager().setBlock(mine, blockId)) {
                  PlayerUtils.sendMessage(
                     sender,
                     "&aSuccessfully set mine block to " + (blockId == null ? "tier default" : blockId) + " for " + target.getName() + "'s private mine."
                  );
               } else {
                  PlayerUtils.sendMessage(sender, "&cInvalid block '" + blockId + "'. Use a vanilla material or a valid ItemsAdder id.");
               }

               return true;
            }
         }
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine forcesetblock [player] [MATERIAL] &7~ &fSet the mine block on player's private mine";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      if (args.size() == 1) {
         return Players.all().stream().<String>map(Player::getName).collect(Collectors.toList());
      } else if (args.size() == 2) {
         String typing = args.get(1);
         return !typing.isEmpty()
            ? Arrays.stream(XMaterial.values()).map(Enum::name).filter(s -> s.toLowerCase().startsWith(typing.toLowerCase())).collect(Collectors.toList())
            : Arrays.stream(XMaterial.values()).map(Enum::name).collect(Collectors.toList());
      } else {
         return List.of();
      }
   }
}
