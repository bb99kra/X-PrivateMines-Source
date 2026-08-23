package dev.drawethree.xprivatemines.utils.gui;

import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.lucko.helper.menu.Item;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DummySlot implements Slot {
   private final XPrivateMinesGui gui;
   private final int id;

   public DummySlot(@Nonnull XPrivateMinesGui gui, int id) {
      this.gui = gui;
      this.id = id;
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
      return this;
   }

   @Nullable
   @Override
   public ItemStack getItem() {
      return null;
   }

   @Override
   public boolean hasItem() {
      return false;
   }

   @Nonnull
   @Override
   public Slot setItem(@Nonnull ItemStack item) {
      return this;
   }

   @Override
   public Slot clear() {
      return this;
   }

   @Nonnull
   @Override
   public Slot clearItem() {
      return this;
   }

   @Nonnull
   @Override
   public Slot clearBindings() {
      return this;
   }

   @Nonnull
   @Override
   public Slot clearBindings(ClickType type) {
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull ClickType type, @Nonnull Consumer<InventoryClickEvent> handler) {
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull ClickType type, @Nonnull Runnable handler) {
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull Consumer<InventoryClickEvent> handler, @Nonnull ClickType... types) {
      return this;
   }

   @Nonnull
   @Override
   public Slot bind(@Nonnull Runnable handler, @Nonnull ClickType... types) {
      return this;
   }

   @Nonnull
   @Override
   public <T extends Runnable> Slot bindAllRunnables(@Nonnull Iterable<Entry<ClickType, T>> handlers) {
      return this;
   }

   @Nonnull
   @Override
   public <T extends Consumer<InventoryClickEvent>> Slot bindAllConsumers(@Nonnull Iterable<Entry<ClickType, T>> handlers) {
      return this;
   }
}
