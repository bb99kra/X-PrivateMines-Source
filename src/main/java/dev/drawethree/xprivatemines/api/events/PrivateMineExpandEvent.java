package dev.drawethree.xprivatemines.api.events;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PrivateMineExpandEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private boolean cancelled;
   private final PrivateMine mine;
   private int expandLevel;

   public PrivateMineExpandEvent(PrivateMine mine, int expandLevel) {
      this.mine = mine;
      this.expandLevel = expandLevel;
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

   public int getExpandLevel() {
      return this.expandLevel;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void setExpandLevel(int expandLevel) {
      this.expandLevel = expandLevel;
   }
}
