package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.config.PrivateMinesConfig;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCmd extends PrivateMineSubCommand {
   public TopCmd(PrivateMineCommand command) {
      super(command, "top");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (sender instanceof Player p) {
         Map categories = this.getConfig().getLeaderboardCategories();
         String category;
         if (args.isEmpty()) {
            category = (String)categories.keySet().stream().findFirst().orElse(null);
         } else {
            category = args.get(0).toLowerCase();
         }

         if (category != null && categories.containsKey(category) && ((PrivateMinesConfig.LeaderboardCategoryConfig)categories.get(category)).enabled()) {
            this.getMinesManager().showLeaderboard(p, category);
            return true;
         } else {
            String available = ((java.util.Map<String, dev.drawethree.xprivatemines.config.PrivateMinesConfig.LeaderboardCategoryConfig>) categories).entrySet()
               .stream()
               .filter(e -> e.getValue().enabled())
               .map(java.util.Map.Entry::getKey)
               .collect(Collectors.joining(", "));
            PlayerUtils.sendMessage(p, this.getMessageConfig().getMessage("leaderboard-invalid-category").replace("%categories%", available));
            return true;
         }
      } else {
         PlayerUtils.sendMessage(sender, "Player only command.");
         return true;
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine top [category] &7~ &fView mine leaderboards";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return (List<String>)(args.size() == 1
         ? this.getConfig().getLeaderboardCategories().entrySet().stream().filter(e -> e.getValue().enabled()).map(Entry::getKey).collect(Collectors.toList())
         : new ArrayList<>());
   }
}
