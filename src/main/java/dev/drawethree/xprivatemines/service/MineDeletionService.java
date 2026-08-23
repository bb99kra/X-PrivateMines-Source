package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMineDeleteEvent;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.registry.PrivateMineRegistry;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineDeletionService {
   private final XPrivateMines plugin;
   private final PrivateMineRegistry mineRegistry;
   private final PlayerTeleportService teleportService;
   private final RegionService regionService;
   private final MineResetService resetService;
   private final MineRefillService refillService;

   public MineDeletionService(
      XPrivateMines plugin,
      PrivateMineRegistry mineRegistry,
      PlayerTeleportService teleportService,
      RegionService regionService,
      MineResetService resetService,
      MineRefillService refillService
   ) {
      this.plugin = plugin;
      this.mineRegistry = mineRegistry;
      this.teleportService = teleportService;
      this.regionService = regionService;
      this.resetService = resetService;
      this.refillService = refillService;
   }

   public void deleteMine(CommandSender sender, PrivateMineImpl mine) {
      PrivateMineDeleteEvent event = new PrivateMineDeleteEvent(mine);
      Events.callSync(event);
      XPrivateMines.getInstance().debug("Called PrivateMineDeleteEvent event");
      if (event.isCancelled()) {
         XPrivateMines.getInstance().debug("PrivateMineDeleteEvent was cancelled");
      } else {
         if (mine.getRegion() != null) {
            for (Player p : mine.getPlayersInMine()) {
               this.teleportService.teleportToSpawn(p);
            }
         }

         OfflinePlayer owner = mine.getOfflineOwner();
         this.regionService.clearRegionAsync(mine.getMineImpl().getRegion());
         this.resetService.resetToDefaultSettings(mine);
         this.regionService.recreateMineRegion(mine);
         this.refillService.refill(mine);
         if (owner != null) {
            this.mineRegistry.removePlayerMine(owner.getUniqueId());
         }

         PlayerUtils.sendMessage(sender, this.plugin.getMessageConfig().getMessage("delete-admin").replace("%player%", owner != null ? owner.getName() : "N/A"));
      }
   }
}
