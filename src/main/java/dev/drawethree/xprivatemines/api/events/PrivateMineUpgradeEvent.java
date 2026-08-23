package dev.drawethree.xprivatemines.api.events;

import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PrivateMineUpgradeEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private boolean cancelled;
   private final PrivateMine mine;
   private final MineTier oldTier;
   private MineTier newTier;

   public PrivateMineUpgradeEvent(PrivateMine mine, MineTier oldTier, MineTier newTier) {
      this.mine = mine;
      this.oldTier = oldTier;
      this.newTier = newTier;
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

   public MineTier getOldTier() {
      return this.oldTier;
   }

   public MineTier getNewTier() {
      return this.newTier;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void setNewTier(MineTier newTier) {
      this.newTier = newTier;
   }
}
