package dev.drawethree.xprivatemines.utils.gui;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.lucko.helper.menu.Item;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SimpleSlot implements Slot {
   private final XPrivateMinesGui gui;
   private final int id;
   protected final Map<ClickType, Set<Consumer<InventoryClickEvent>>> handlers;

   public SimpleSlot(@Nonnull XPrivateMinesGui gui, int id) {
      this.gui = gui;
      this.id = id;
      this.handlers = Collections.synchronizedMap(new EnumMap<>(ClickType.class));
   }

   public void handle(@Nonnull InventoryClickEvent event) {
      Set<Consumer<InventoryClickEvent>> handlers = this.handlers.get(event.getClick());
      if (handlers != null) {
         for (Consumer<InventoryClickEvent> handler : handlers) {
            try {
               handler.accept(event);
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }
   }

   @Nonnull
   @Override
   public XPrivateMinesGui gui() {
      return this.gui;
   }

   @Override
   public int getId() {
      return this.id;
   }

   @Override
   public Slot applyFromItem(Item item) {
      Objects.requireNonNull(item, "item");
      this.setItem(item.getItemStack());
      this.clearBindings();
      this.bindAllConsumers(item.getHandlers().entrySet());
      return this;
   }

   @Nullable
   @Override
   public ItemStack getItem() {
      return this.gui.getHandle().getItem(this.id);
   }

   @Override
   public boolean hasItem() {
      return this.getItem() != null;
   }

   @Nonnull
   @Override
   public Slot setItem(@Nonnull ItemStack item) {
      Objects.requireNonNull(item, "item");
      this.gui.getHandle().setItem(this.id, item);
      return this;
   }

   @Override
   public Slot clear() {
      this.clearItem();
      this.clearBindings();
      return this;
   }

   @Nonnull
   @Override
   public Slot clearItem() {
      this.gui.getHandle().clear(this.id);
      return this;
   }

   @Nonnull
   @Override
   public Slot clearBindings() {
      this.handlers.clear();
      return this;
   }

   @Nonnull
   @Override
   public Slot clearBindings(ClickType type) {
      this.handlers.remove(type);
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull ClickType type, @Nonnull Consumer<InventoryClickEvent> handler) {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(handler, "handler");
      this.handlers.computeIfAbsent(type, t -> ConcurrentHashMap.newKeySet()).add(handler);
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull ClickType type, @Nonnull Runnable handler) {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(handler, "handler");
      this.handlers.computeIfAbsent(type, t -> ConcurrentHashMap.newKeySet()).add(Item.transformRunnable(handler));
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull Consumer<InventoryClickEvent> handler, @Nonnull ClickType... types) {
      for (ClickType type : types) {
         this.bind(type, handler);
      }

      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull Runnable handler, @Nonnull ClickType... types) {
      for (ClickType type : types) {
         this.bind(type, handler);
      }

      return this;
   }

   @Nonnull
   @Override
   public <T extends Runnable> Slot bindAllRunnables(@Nonnull Iterable<Entry<ClickType, T>> handlers) {
      Objects.requireNonNull(handlers, "handlers");

      for (Entry<ClickType, T> handler : handlers) {
         this.bind(handler.getKey(), handler.getValue());
      }

      return this;
   }

   @Nonnull
   @Override
   public <T extends Consumer<InventoryClickEvent>> Slot bindAllConsumers(@Nonnull Iterable<Entry<ClickType, T>> handlers) {
      Objects.requireNonNull(handlers, "handlers");

      for (Entry<ClickType, T> handler : handlers) {
         this.bind(handler.getKey(), handler.getValue());
      }

      return this;
   }
}
