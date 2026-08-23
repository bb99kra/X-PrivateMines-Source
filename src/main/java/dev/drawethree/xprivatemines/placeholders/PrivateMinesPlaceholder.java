package dev.drawethree.xprivatemines.placeholders;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.manager.PrivateMinesManagerImpl;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.PlaceholderUtils;
import dev.drawethree.xprivatemines.utils.text.NumberFormatter;
import dev.drawethree.xprivatemines.utils.text.ProgressBar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PrivateMinesPlaceholder extends PlaceholderExpansion {
   private final XPrivateMines plugin;
   private final Map<String, List<PrivateMine>> leaderboardCache = new ConcurrentHashMap<>();

   public PrivateMinesPlaceholder(XPrivateMines plugin) {
      this.plugin = plugin;
      this.scheduleLeaderboardRebuild();
   }

   private void scheduleLeaderboardRebuild() {
      this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this::rebuildLeaderboardCache, 600L, 600L);
   }

   private void rebuildLeaderboardCache() {
      if (this.plugin.getMinesManager().isMinesReady()) {
         List<MineTier> allTiers = this.plugin.getMineTierManager().getAllTiers();
         Map<String, Comparator<PrivateMine>> comparators = Map.of(
            "tier",
            Comparator.<PrivateMine>comparingInt(m -> m.getTier() != null ? allTiers.indexOf(m.getTier()) : -1).reversed(),
            "size",
            Comparator.comparingInt(PrivateMine::getMineSize).reversed(),
            "tax",
            Comparator.comparingDouble(PrivateMine::getTax).reversed(),
            "fee",
            Comparator.comparingDouble(PrivateMine::getEntryFee).reversed()
         );
         Collection<PrivateMine> all = this.plugin.getMinesManager().getAll();
         this.plugin
            .getPrivateMinesConfig()
            .getLeaderboardCategories()
            .forEach(
               (key, cfg) -> {
                  if (cfg.enabled()) {
                     Comparator<PrivateMine> cmp = comparators.getOrDefault(
                        key, Comparator.<PrivateMine>comparingInt(m -> m.getTier() != null ? allTiers.indexOf(m.getTier()) : -1).reversed()
                     );
                     List<PrivateMine> sorted = all.stream().filter(m -> m.getOwner() != null).sorted(cmp).collect(Collectors.toList());
                     this.leaderboardCache.put(key, sorted);
                  }
               }
            );
      }
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String getAuthor() {
      return this.plugin.getDescription().getAuthors().toString();
   }

   public String getIdentifier() {
      return "xprivatemines";
   }

   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      if (player == null) {
         return null;
      } else {
         PrivateMinesManagerImpl manager = this.plugin.getMinesManager();
         PrivateMineImpl mine = this.plugin.getMinesManager().getPrivateMineInternal(player);
         if ("has".equalsIgnoreCase(identifier)) {
            return String.valueOf(mine != null);
         } else if (!identifier.toLowerCase().startsWith("top_")) {
            if (mine == null) {
               return "";
            } else {
               String var15 = identifier.toLowerCase();
               switch (var15) {
                  case "owner":
                     return mine.getOfflineOwner().getName();
                  case "tier":
                     return mine.getTier().getName();
                  case "is_max_tier":
                     return String.valueOf(manager.isMaxTier(mine));
                  case "is_max_expand":
                     return String.valueOf(manager.isMaxExpand(mine));
                  case "is_open":
                     return String.valueOf(mine.isOpen());
                  case "rankup_cost":
                     return String.format("%,.2f", manager.getNextUpgradeCost(mine));
                  case "rankup_cost_formatted":
                     return this.plugin.getEconomyManager().format(player, manager.getNextUpgradeCost(mine));
                  case "rankup_progress":
                     if (manager.isMaxTier(mine)) {
                        return ProgressBar.getProgressBar(20, null, 1.0, 1.0);
                     }

                     return ProgressBar.getProgressBar(20, null, this.plugin.getEconomyManager().getBalance(player), manager.getNextUpgradeCost(mine));
                  case "rankup_progress_percentage":
                     double balance = this.plugin.getEconomyManager().getBalance(player);
                     double cost = manager.getNextUpgradeCost(mine);
                     if (cost == 0.0) {
                        return "&a&lMAX";
                     }

                     double percent = balance / cost * 100.0;
                     return String.format("%,.2f%%", Math.min(percent, 100.0));
                  case "reset_percentage":
                     return String.format("%.2f%%", mine.getResetPercentage());
                  case "size":
                     return mine.getMineSize() + "x" + mine.getMineSize();
                  case "tax":
                     return String.format("%.2f", mine.getTax());
                  case "expand_level":
                     return String.valueOf(mine.getExpandLevel());
                  case "expand_cost":
                     return this.plugin.getEconomyManager().format(player, mine.getSchematic().getSettings().getExpandCost());
                  case "expand_cost_formatted":
                     return NumberFormatter.format(mine.getSchematic().getSettings().getExpandCost());
                  case "open":
                     return mine.isOpen()
                        ? PlaceholderUtils.getTranslation(PlaceholderUtils.PlaceholderType.IS_OPEN_YES)
                        : PlaceholderUtils.getTranslation(PlaceholderUtils.PlaceholderType.IS_OPEN_NO);
                  case "entry_fee":
                     return this.plugin.getEconomyManager().format(player, mine.getEntryFee());
                  case "unclaimed_money":
                     return this.plugin.getEconomyManager().format(player, mine.getUnclaimedMoney());
                  case "reset_progress":
                     long total = mine.getMineImpl().getTotalBlockCount();
                     if (total <= 0L) {
                        return "100.00%";
                     }

                     double remaining = (double)mine.getMineImpl().getEstimatedRemainingBlocks() / total * 100.0;
                     return String.format("%.2f%%", Math.min(remaining, 100.0));
                  case "name":
                     return mine.getDisplayName();
                  case "motd":
                     return mine.getMineMotd() != null ? mine.getMineMotd() : "";
                  default:
                     return null;
               }
            }
         } else {
            String[] parts = identifier.split("_", 4);
            if (parts.length == 4) {
               String category = parts[1];
               String field = parts[3];

               int rank;
               try {
                  rank = Integer.parseInt(parts[2]);
               } catch (NumberFormatException var14) {
                  return null;
               }

               List<PrivateMine> cached = this.leaderboardCache.get(category);
               if (cached != null && rank >= 1 && rank <= cached.size()) {
                  PrivateMine entry = cached.get(rank - 1);
                  OfflinePlayer entryOwner = entry.getOfflineOwner();
                  String var12 = field.toLowerCase();

                  return switch (var12) {
                     case "name" -> entry.getDisplayName();
                     case "owner" -> entryOwner != null && entryOwner.getName() != null ? entryOwner.getName() : "Unknown";
                     case "value" -> this.resolveLeaderboardValue(category, entry, player);
                     default -> null;
                  };
               } else {
                  return "";
               }
            } else {
               return null;
            }
         }
      }
   }

   private String resolveLeaderboardValue(String category, PrivateMine mine, Player viewer) {
      return switch (category) {
         case "tier" -> mine.getTier() != null ? mine.getTier().getName() : "N/A";
         case "size" -> mine.getMineSize() + "x" + mine.getMineSize();
         case "tax" -> String.format("%,.2f%%", mine.getTax());
         case "fee" -> this.plugin.getEconomyManager().format(viewer, mine.getEntryFee());
         default -> "";
      };
   }
}
