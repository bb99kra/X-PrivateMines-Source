package dev.drawethree.xprivatemines.utils.gui;

import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.lucko.helper.menu.Item;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Slot {
   @Nonnull
   XPrivateMinesGui gui();

   int getId();

   Slot applyFromItem(Item var1);

   @Nullable
   ItemStack getItem();

   boolean hasItem();

   @Nonnull
   Slot setItem(@Nonnull ItemStack var1);

   Slot clear();

   @Nonnull
   Slot clearItem();

   @Nonnull
   Slot clearBindings();

   @Nonnull
   Slot clearBindings(ClickType var1);

   @Nonnull
   Slot bind(@Nonnull ClickType var1, @Nonnull Consumer<InventoryClickEvent> var2);

   @Nonnull
   Slot bind(@Nonnull ClickType var1, @Nonnull Runnable var2);

   @Nonnull
   Slot bind(@Nonnull Consumer<InventoryClickEvent> var1, @Nonnull ClickType... var2);

   @Nonnull
   Slot bind(@Nonnull Runnable var1, @Nonnull ClickType... var2);

   @Nonnull
   <T extends Runnable> Slot bindAllRunnables(@Nonnull Iterable<Entry<ClickType, T>> var1);

   @Nonnull
   <T extends Consumer<InventoryClickEvent>> Slot bindAllConsumers(@Nonnull Iterable<Entry<ClickType, T>> var1);
}
