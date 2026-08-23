package dev.drawethree.xprivatemines.api.events;

import dev.drawethree.xprivatemines.api.model.PrivateMine;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MineBlockBreakEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private final Player player;
   private final PrivateMine mine;
   private final Block block;
   private boolean cancelled;

   public MineBlockBreakEvent(Player player, PrivateMine mine, Block block) {
      this.player = player;
      this.mine = mine;
      this.block = block;
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

   public Block getBlock() {
      return this.block;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
