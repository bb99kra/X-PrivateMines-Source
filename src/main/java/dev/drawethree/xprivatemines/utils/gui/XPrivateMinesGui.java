package dev.drawethree.xprivatemines.utils.gui;

import com.google.common.base.Preconditions;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.menu.Item;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.metadata.MetadataMap;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.reflect.MinecraftVersions;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;

@NonnullByDefault
public abstract class XPrivateMinesGui implements TerminableConsumer {
   public static final MetadataKey<XPrivateMinesGui> OPEN_GUI_KEY = MetadataKey.create("open-gui", XPrivateMinesGui.class);
   private final Player player;
   private final Inventory inventory;
   private final String initialTitle;
   private final Map<Integer, SimpleSlot> slots;
   private boolean firstDraw = true;
   @Nullable
   private Function<Player, XPrivateMinesGui> fallbackGui = null;
   private final CompositeTerminable compositeTerminable = CompositeTerminable.create();
   private boolean valid = false;
   private boolean invalidated = false;

   public static int getMenuSize(int count) {
      Preconditions.checkArgument(count >= 0, "count < 0");
      return getMenuSize(count, 9);
   }

   public static int getMenuSize(int count, int itemsPerLine) {
      Preconditions.checkArgument(itemsPerLine >= 1, "itemsPerLine < 1");
      return count / itemsPerLine + (count % itemsPerLine != 0 ? 1 : 0);
   }

   public XPrivateMinesGui(Player player, int lines, String title) {
      this.player = Objects.requireNonNull(player, "player");
      this.initialTitle = Text.colorize(Objects.requireNonNull(title, "title"));
      if (XPrivateMines.isUseMiniMessage()) {
         this.inventory = Bukkit.createInventory(player, lines * 9, TextUtils.toComponent(this.initialTitle));
      } else {
         this.inventory = Bukkit.createInventory(player, lines * 9, this.initialTitle);
      }

      this.slots = new HashMap<>();
   }

   public abstract void redraw();

   public Player getPlayer() {
      return this.player;
   }

   public Inventory getHandle() {
      return this.inventory;
   }

   public String getInitialTitle() {
      return this.initialTitle;
   }

   @Nullable
   public Function<Player, XPrivateMinesGui> getFallbackGui() {
      return this.fallbackGui;
   }

   public void setFallbackGui(@Nullable Function<Player, XPrivateMinesGui> fallbackGui) {
      this.fallbackGui = fallbackGui;
   }

   @Nonnull
   public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
      return (T)this.compositeTerminable.bind(terminable);
   }

   public boolean isFirstDraw() {
      return this.firstDraw;
   }

   public Slot getSlot(int slot) {
      if (slot >= 0 && slot < this.inventory.getSize()) {
         return (Slot)(this.invalidated ? new DummySlot(this, slot) : this.slots.computeIfAbsent(slot, i -> new SimpleSlot(this, i)));
      } else {
         throw new IllegalArgumentException("Invalid slot id: " + slot);
      }
   }

   public void setItem(int slot, Item item) {
      this.getSlot(slot).applyFromItem(item);
   }

   public void setItems(Item item, int... slots) {
      Objects.requireNonNull(item, "item");

      for (int slot : slots) {
         this.setItem(slot, item);
      }
   }

   public void setItems(Iterable<Integer> slots, Item item) {
      Objects.requireNonNull(item, "item");
      Objects.requireNonNull(slots, "slots");

      for (int slot : slots) {
         this.setItem(slot, item);
      }
   }

   public int getFirstEmpty() {
      int ret = this.inventory.firstEmpty();
      if (ret < 0) {
         throw new IndexOutOfBoundsException("no empty slots");
      } else {
         return ret;
      }
   }

   public Optional<Slot> getFirstEmptySlot() {
      int ret = this.inventory.firstEmpty();
      return ret < 0 ? Optional.empty() : Optional.of(this.getSlot(ret));
   }

   public void addItem(Item item) {
      Objects.requireNonNull(item, "item");
      this.getFirstEmptySlot().ifPresent(s -> s.applyFromItem(item));
   }

   public void addItems(Iterable<Item> items) {
      Objects.requireNonNull(items, "items");

      for (Item item : items) {
         this.addItem(item);
      }
   }

   public void fillWith(Item item) {
      Objects.requireNonNull(item, "item");

      for (int i = 0; i < this.inventory.getSize(); i++) {
         this.setItem(i, item);
      }
   }

   public void removeItem(int slot) {
      this.getSlot(slot).clear();
   }

   public void removeItems(int... slots) {
      for (int slot : slots) {
         this.removeItem(slot);
      }
   }

   public void removeItems(Iterable<Integer> slots) {
      Objects.requireNonNull(slots, "slots");

      for (int slot : slots) {
         this.removeItem(slot);
      }
   }

   public void clearItems() {
      this.inventory.clear();
      this.slots.values().forEach(Slot::clearBindings);
   }

   public void open() {
      if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_16)) {
         Schedulers.sync().runLater(() -> {
            if (this.player.isOnline()) {
               this.handleOpen();
            }
         }, 1L);
      } else {
         this.handleOpen();
      }
   }

   private void handleOpen() {
      if (this.valid) {
         throw new IllegalStateException("Gui is already opened.");
      } else {
         this.firstDraw = true;
         this.invalidated = false;

         try {
            this.redraw();
         } catch (Exception var2) {
            var2.printStackTrace();
            this.invalidate();
            return;
         }

         this.firstDraw = false;
         this.startListening();
         this.player.openInventory(this.inventory);
         Metadata.provideForPlayer(this.player).put(OPEN_GUI_KEY, this);
         this.valid = true;
      }
   }

   public void close() {
      this.player.closeInventory();
   }

   private void closeIfStillOnScreen() {
      if (this.player.isOnline()) {
         if (this.inventory.equals(this.player.getOpenInventory().getTopInventory())) {
            this.player.closeInventory();
         }
      }
   }

   private void invalidate() {
      this.valid = false;
      this.invalidated = true;
      MetadataMap metadataMap = Metadata.provideForPlayer(this.player);
      XPrivateMinesGui existing = (XPrivateMinesGui)metadataMap.getOrNull(OPEN_GUI_KEY);
      if (existing == this) {
         metadataMap.remove(OPEN_GUI_KEY);
      }

      this.compositeTerminable.closeAndReportException();
      this.clearItems();
   }

   public boolean isValid() {
      return this.valid;
   }

   private void startListening() {
      Events.merge(Player.class)
         .bindEvent(PlayerDeathEvent.class, PlayerDeathEvent::getEntity)
         .bindEvent(PlayerQuitEvent.class, PlayerEvent::getPlayer)
         .bindEvent(PlayerChangedWorldEvent.class, PlayerEvent::getPlayer)
         .bindEvent(PlayerTeleportEvent.class, PlayerEvent::getPlayer)
         .filter(p -> p.equals(this.player))
         .filter(p -> this.isValid())
         .handler(p -> {
            this.invalidate();
            this.closeIfStillOnScreen();
         })
         .bindWith(this);
      Events.subscribe(InventoryDragEvent.class)
         .filter(e -> e.getInventory().getHolder() != null)
         .filter(e -> e.getInventory().getHolder().equals(this.player))
         .handler(e -> {
            e.setCancelled(true);
            if (!this.isValid()) {
               this.close();
            }
         })
         .bindWith(this);
      Events.subscribe(InventoryClickEvent.class)
         .filter(e -> e.getInventory().getHolder() != null)
         .filter(e -> e.getInventory().getHolder().equals(this.player))
         .handler(e -> {
            e.setCancelled(true);
            if (!this.isValid()) {
               this.close();
            } else if (e.getInventory().equals(this.inventory)) {
               int slotId = e.getRawSlot();
               if (slotId == e.getSlot()) {
                  SimpleSlot slot = this.slots.get(slotId);
                  if (slot != null) {
                     slot.handle(e);
                  }
               }
            }
         })
         .bindWith(this);
      Events.subscribe(InventoryOpenEvent.class)
         .filter(e -> e.getPlayer().equals(this.player))
         .filter(e -> !e.getInventory().equals(this.inventory))
         .filter(e -> this.isValid())
         .handler(e -> this.invalidate())
         .bindWith(this);
      Events.subscribe(InventoryCloseEvent.class).filter(e -> e.getPlayer().equals(this.player)).filter(e -> this.isValid()).handler(e -> {
         this.invalidate();
         if (e.getInventory().equals(this.inventory)) {
            Function<Player, XPrivateMinesGui> fallback = this.fallbackGui;
            if (fallback != null) {
               Schedulers.sync().runLater(() -> {
                  if (this.player.isOnline()) {
                     XPrivateMinesGui fallbackGui = fallback.apply(this.player);
                     if (fallbackGui == null) {
                        throw new IllegalStateException("Fallback function " + fallback + " returned null");
                     }

                     if (fallbackGui.valid) {
                        throw new IllegalStateException("Fallback function " + fallback + " produced a GUI " + fallbackGui + " which is already open");
                     }

                     fallbackGui.open();
                  }
               }, 1L);
            }
         }
      }).bindWith(this);
   }
}
