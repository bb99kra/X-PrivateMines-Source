package dev.drawethree.xprivatemines.listener;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.MineEnterEvent;
import dev.drawethree.xprivatemines.api.events.MineLeaveEvent;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PrivateMineRegionListener {
   private final XPrivateMines plugin;
   private final Map<UUID, PrivateMine> playersInMines = new HashMap<>();

   public PrivateMineRegionListener(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void register() {
      Events.subscribe(PlayerMoveEvent.class, EventPriority.NORMAL).filter(e -> !(e instanceof PlayerTeleportEvent)).handler(e -> {
         Location to = e.getTo();
         if (to != null && !sameBlock(e.getFrom(), to)) {
            if (!this.handleTransition(e.getPlayer(), to)) {
               e.setCancelled(true);
            }
         }
      }).bindWith(this.plugin);
      Events.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL).handler(e -> {
         Location to = e.getTo();
         if (to != null) {
            if (!this.handleTransition(e.getPlayer(), to)) {
               e.setCancelled(true);
            }
         }
      }).bindWith(this.plugin);
      Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR).handler(e -> this.handleLeave(e.getPlayer())).bindWith(this.plugin);
      Events.subscribe(PlayerKickEvent.class, EventPriority.MONITOR).handler(e -> this.handleLeave(e.getPlayer())).bindWith(this.plugin);
   }

   private boolean handleTransition(Player player, Location to) {
      PrivateMine currentMine = this.playersInMines.get(player.getUniqueId());
      PrivateMine targetMine = this.plugin.getMinesManager().getPrivateMineAtLocation(to);
      if (currentMine == targetMine) {
         return true;
      } else {
         if (currentMine != null) {
            Events.callSync(new MineLeaveEvent(player, currentMine));
            this.playersInMines.remove(player.getUniqueId());
         }

         if (targetMine != null) {
            MineEnterEvent enterEvent = new MineEnterEvent(player, targetMine);
            Events.callSync(enterEvent);
            if (enterEvent.isCancelled()) {
               return false;
            }

            this.playersInMines.put(player.getUniqueId(), targetMine);
         }

         return true;
      }
   }

   private void handleLeave(Player player) {
      PrivateMine mine = this.playersInMines.remove(player.getUniqueId());
      if (mine != null) {
         Events.callSync(new MineLeaveEvent(player, mine));
      }
   }

   private static boolean sameBlock(Location a, Location b) {
      return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
   }
}
