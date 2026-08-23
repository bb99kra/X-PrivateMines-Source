package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMineExpandEvent;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.schematic.SchematicSettingsImpl;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import me.lucko.helper.Events;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineExpandService {
   private final XPrivateMines plugin;
   private final MineRefillService resetService;
   private final RegionService regionService;

   public MineExpandService(XPrivateMines plugin, MineRefillService resetService, RegionService regionService) {
      this.plugin = plugin;
      this.resetService = resetService;
      this.regionService = regionService;
   }

   public boolean forceExpand(CommandSender sender, PrivateMineImpl mine, int expandAmount) {
      if (expandAmount == -1 && this.isMaxExpand(mine)) {
         PlayerUtils.sendMessage(sender, this.plugin.getMessageConfig().getMessage("mine-expand-failed-max-level"));
         return false;
      } else {
         int newExpand;
         if (expandAmount == -1) {
            newExpand = mine.getExpandLevel() + 1;
         } else {
            newExpand = expandAmount;
         }

         if (newExpand > mine.getSchematic().getSettings().getMaxExpand()) {
            newExpand = mine.getSchematic().getSettings().getMaxExpand();
         }

         PrivateMineExpandEvent event = new PrivateMineExpandEvent(mine, newExpand);
         Events.callSync(event);
         XPrivateMines.getInstance().debug("Called PrivateMineExpandEvent event");
         if (event.isCancelled()) {
            XPrivateMines.getInstance().debug("PrivateMineExpandEvent was cancelled");
            return false;
         } else {
            mine.setExpandLevel(newExpand);
            boolean bedrockWalls = mine.getSchematic().getSettings() instanceof SchematicSettingsImpl s && s.isBedrockWalls();
            this.regionService.clearRegionAsync(mine.getMineImpl().getRegion(), bedrockWalls).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
               this.regionService.recreateMineRegion(mine);
               this.resetService.refill(mine);
            }));
            SoundUtils.playUpgrade(mine.getOfflineOwner().getPlayer());
            PlayerUtils.sendTitle(mine.getOfflineOwner().getPlayer(), this.plugin.getMessageConfig().getTitle("mine-expanded"));
            PlayerUtils.sendMessage(mine.getOfflineOwner().getPlayer(), this.plugin.getMessageConfig().getMessage("mine-expand-success"));
            return true;
         }
      }
   }

   public boolean expandMine(PrivateMineImpl mine, Player p) {
      if (this.isMaxExpand(mine)) {
         PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("mine-expand-failed-max-level"));
         SoundUtils.playError(p);
         return false;
      } else if (!this.canExpand(mine, p)) {
         PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("no-money"));
         SoundUtils.playError(p);
         return false;
      } else {
         this.plugin.getEconomyManager().withdraw(p, mine.getSchematic().getSettings().getExpandCost());
         mine.setExpandLevel(mine.getExpandLevel() + 1);
         this.regionService.recreateMineRegion(mine);
         this.resetService.refill(mine);
         SoundUtils.playUpgrade(p);
         PlayerUtils.sendTitle(p, this.plugin.getMessageConfig().getTitle("mine-expanded"));
         PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("mine-expand-success"));
         return true;
      }
   }

   private boolean canExpand(PrivateMineImpl mine, Player p) {
      return this.plugin.getEconomyManager().getBalance(p) >= mine.getSchematic().getSettings().getExpandCost();
   }

   public boolean isMaxExpand(PrivateMineImpl mine) {
      return mine.getExpandLevel() >= mine.getSchematic().getSettings().getMaxExpand();
   }
}
