package dev.drawethree.xprivatemines.api.events;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PrivateMineUnbanPlayerEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private boolean cancelled;
   private final PrivateMine mine;
   private final OfflinePlayer player;

   public PrivateMineUnbanPlayerEvent(PrivateMine mine, OfflinePlayer player) {
      this.mine = mine;
      this.player = player;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS_LIST;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS_LIST;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public PrivateMine getMine() {
      return this.mine;
   }

   public OfflinePlayer getPlayer() {
      return this.player;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
