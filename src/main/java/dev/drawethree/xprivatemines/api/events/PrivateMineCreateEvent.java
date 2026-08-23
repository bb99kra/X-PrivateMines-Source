package dev.drawethree.xprivatemines.api.events;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PrivateMineCreateEvent extends Event {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private final PrivateMine mine;

   public PrivateMineCreateEvent(PrivateMine mine) {
      this.mine = mine;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS_LIST;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS_LIST;
   }

   public PrivateMine getMine() {
      return this.mine;
   }
}
