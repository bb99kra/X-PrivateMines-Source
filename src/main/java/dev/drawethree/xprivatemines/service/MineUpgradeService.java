package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMineUpgradeEvent;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.manager.MineTierManagerImpl;
import dev.drawethree.xprivatemines.mines.model.MineTierImpl;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.util.Optional;
import me.lucko.helper.Events;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineUpgradeService {
   private final XPrivateMines plugin;
   private final MineRefillService resetService;
   private final RegionService regionService;

   public MineUpgradeService(XPrivateMines plugin, MineRefillService resetService, RegionService regionService) {
      this.plugin = plugin;
      this.resetService = resetService;
      this.regionService = regionService;
   }

   public boolean forceUpgrade(CommandSender sender, PrivateMineImpl mine, MineTierImpl tier) {
      MineTierManagerImpl tierManager = this.plugin.getMineTierManager();
      MineTierImpl currentTier = tierManager.getTier(mine.getTier().getKey());
      MineTier newTier = (MineTier)(tier != null ? tier : tierManager.getNextTier(currentTier.getKey()).orElse(null));
      if (newTier == null) {
         PlayerUtils.sendMessage(sender, "&cPlayer is at max tier.");
         return true;
      } else {
         PrivateMineUpgradeEvent event = new PrivateMineUpgradeEvent(mine, currentTier, newTier);
         Events.callSync(event);
         if (event.isCancelled()) {
            return false;
         } else {
            mine.setTier(newTier);
            this.regionService.recreateMineRegion(mine);
            this.resetService.refill(mine);
            Player owner = mine.getOfflineOwner().getPlayer();
            if (owner != null) {
               SoundUtils.playUpgrade(owner);
               PlayerUtils.sendTitle(owner, this.plugin.getMessageConfig().getTitle("mine-upgraded"));
               PlayerUtils.sendMessage(owner, this.plugin.getMessageConfig().getMessage("mine-upgrade-success").replace("%tier%", newTier.getName()));
            }

            return true;
         }
      }
   }

   public boolean upgradeMine(PrivateMineImpl mine, Player p) {
      MineTierManagerImpl tierManager = this.plugin.getMineTierManager();
      MineTierImpl currentTier = tierManager.getTier(mine.getTier().getKey());
      Optional<MineTier> nextTierOpt = tierManager.getNextTier(currentTier.getKey());
      if (nextTierOpt.isEmpty()) {
         PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("mine-upgrade-failed-max-tier"));
         SoundUtils.playError(p);
         return false;
      } else {
         MineTier nextTier = nextTierOpt.get();
         double upgradeCost = nextTier.getUpgradeCost();
         if (!this.canUpgradeTo(p, nextTier)) {
            PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("no-money"));
            SoundUtils.playError(p);
            return false;
         } else {
            PrivateMineUpgradeEvent event = new PrivateMineUpgradeEvent(mine, currentTier, nextTier);
            Events.callSync(event);
            if (event.isCancelled()) {
               return false;
            } else {
               this.plugin.getEconomyManager().withdraw(p, upgradeCost);
               mine.setTier(nextTier);
               this.regionService.recreateMineRegion(mine);
               this.resetService.refill(mine);
               SoundUtils.playUpgrade(p);
               PlayerUtils.sendTitle(p, this.plugin.getMessageConfig().getTitle("mine-upgraded"));
               PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("mine-upgrade-success").replace("%tier%", nextTier.getName()));
               return true;
            }
         }
      }
   }

   private boolean canUpgradeTo(Player player, MineTier nextTier) {
      return this.plugin.getEconomyManager().getBalance(player) >= nextTier.getUpgradeCost();
   }

   public double getNextUpgradeCost(PrivateMineImpl mine) {
      MineTier currentTier = mine.getTier();
      MineTierManagerImpl tierManager = this.plugin.getMineTierManager();
      Optional<MineTier> nextTier = tierManager.getNextTier(currentTier.getKey());
      return nextTier.map(MineTier::getUpgradeCost).orElse(0.0);
   }

   public boolean isMaxTier(PrivateMineImpl mine) {
      MineTierManagerImpl tierManager = this.plugin.getMineTierManager();
      Optional<MineTier> nextTierOpt = tierManager.getNextTier(mine.getTier().getKey());
      return nextTierOpt.isEmpty();
   }
}
