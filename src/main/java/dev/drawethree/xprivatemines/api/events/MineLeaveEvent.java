package dev.drawethree.xprivatemines.api.events;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MineLeaveEvent extends Event {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private final Player player;
   private final PrivateMine mine;

   public MineLeaveEvent(Player player, PrivateMine mine) {
      this.player = player;
      this.mine = mine;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS_LIST;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS_LIST;
   }

   public Player getPlayer() {
      return this.player;
   }

   public PrivateMine getMine() {
      return this.mine;
   }
}
