package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMineBanPlayerEvent;
import dev.drawethree.xprivatemines.api.events.PrivateMineCloseEvent;
import dev.drawethree.xprivatemines.api.events.PrivateMineOpenEvent;
import dev.drawethree.xprivatemines.api.events.PrivateMineUnbanPlayerEvent;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MineAccessService {
   public boolean openPrivateMine(PrivateMine mine) {
      PrivateMineOpenEvent event = new PrivateMineOpenEvent(mine);
      Events.callSync(event);
      XPrivateMines.getInstance().debug("Called PrivateMineOpenEvent event");
      mine.setOpen(true);
      return true;
   }

   public boolean closePrivateMine(PrivateMine mine) {
      PrivateMineCloseEvent event = new PrivateMineCloseEvent(mine);
      Events.callSync(event);
      XPrivateMines.getInstance().debug("Called PrivateMineCloseEvent event");
      mine.setOpen(false);

      for (Player p : Players.all()) {
         if (!mine.isOwner(p) && mine.isInPrivateMine(p.getLocation())) {
            this.kickPlayer(p);
         }
      }

      return true;
   }

   public boolean banPlayer(PrivateMineImpl mine, OfflinePlayer p) {
      if (mine.isBanned(p)) {
         return false;
      } else {
         PrivateMineBanPlayerEvent event = new PrivateMineBanPlayerEvent(mine, p);
         Events.callSync(event);
         XPrivateMines.getInstance().debug("Called PrivateMineBanPlayerEvent event");
         if (event.isCancelled()) {
            XPrivateMines.getInstance().debug("PrivateMineBanPlayerEvent was cancelled");
            return false;
         } else {
            mine.getBannedPlayersUUID().add(p.getUniqueId());
            mine.setDirty(true);
            if (p.isOnline() && mine.isInPrivateMine(p.getLocation())) {
               this.kickPlayer(p.getPlayer());
            }

            return true;
         }
      }
   }

   public boolean unbanPlayer(PrivateMineImpl mine, OfflinePlayer p) {
      if (!mine.isBanned(p)) {
         return false;
      } else {
         PrivateMineUnbanPlayerEvent event = new PrivateMineUnbanPlayerEvent(mine, p);
         Events.callSync(event);
         XPrivateMines.getInstance().debug("Called PrivateMineUnbanPlayerEvent event");
         if (event.isCancelled()) {
            XPrivateMines.getInstance().debug("PrivateMineUnbanPlayerEvent was cancelled");
            return false;
         } else {
            mine.getBannedPlayersUUID().remove(p.getUniqueId());
            mine.setDirty(true);
            return true;
         }
      }
   }

   public void kickPlayer(Player target) {
      if (!Bukkit.dispatchCommand(target, "spawn")) {
         target.teleport(target.getWorld().getSpawnLocation());
      }
   }
}
