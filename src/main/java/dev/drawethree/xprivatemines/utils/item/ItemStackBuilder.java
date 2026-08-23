package dev.drawethree.xprivatemines.utils.item;

import com.cryptomorin.xseries.XItemFlag;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.compat.MinecraftVersion;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.utils.text.ComponentUtil;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Item.Builder;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

@NonnullByDefault
public final class ItemStackBuilder {
   private static final ItemFlag[] ALL_FLAGS = new ItemFlag[]{
      ItemFlag.HIDE_ENCHANTS,
      ItemFlag.HIDE_ATTRIBUTES,
      ItemFlag.HIDE_UNBREAKABLE,
      XItemFlag.HIDE_ADDITIONAL_TOOLTIP.get(),
      ItemFlag.HIDE_DESTROYS,
      ItemFlag.HIDE_PLACED_ON
   };
   private final ItemStack itemStack;

   public static ItemStackBuilder of(Material material) {
      return new ItemStackBuilder(new ItemStack(material)).hideAttributes();
   }

   public static ItemStackBuilder of(ItemStack itemStack) {
      return new ItemStackBuilder(itemStack).hideAttributes();
   }

   private ItemStackBuilder(ItemStack itemStack) {
      this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
   }

   public ItemStackBuilder transform(Consumer<ItemStack> is) {
      is.accept(this.itemStack);
      return this;
   }

   public ItemStackBuilder transformMeta(Consumer<ItemMeta> meta) {
      ItemMeta m = this.itemStack.getItemMeta();
      if (m != null) {
         meta.accept(m);
         this.itemStack.setItemMeta(m);
      }

      return this;
   }

   public ItemStackBuilder name(String name) {
      return XPrivateMines.isUseMiniMessage()
         ? this.transformMeta(meta -> meta.displayName(TextUtils.toItemComponent(name)))
         : this.transformMeta(meta -> meta.setDisplayName(ComponentUtil.toLegacy(name)));
   }

   public ItemStackBuilder type(Material material) {
      return this.transform(itemStack -> itemStack.setType(material));
   }

   public ItemStackBuilder lore(String line) {
      return XPrivateMines.isUseMiniMessage() ? this.transformMeta(meta -> {
         List<Component> lore = (List<Component>)(meta.lore() == null ? new ArrayList<>() : meta.lore());
         lore.add(TextUtils.toItemComponent(line));
         meta.lore(lore);
      }) : this.transformMeta(meta -> {
         List<String> lore = (List<String>)(meta.getLore() == null ? new ArrayList<>() : meta.getLore());
         lore.add(ComponentUtil.toLegacy(line));
         meta.setLore(lore);
      });
   }

   public ItemStackBuilder lore(String... lines) {
      return XPrivateMines.isUseMiniMessage() ? this.transformMeta(meta -> {
         List<Component> lore = (List<Component>)(meta.lore() == null ? new ArrayList<>() : meta.lore());

         for (String line : lines) {
            lore.add(TextUtils.toItemComponent(line));
         }

         meta.lore(lore);
      }) : this.transformMeta(meta -> {
         List<String> lore = (List<String>)(meta.getLore() == null ? new ArrayList<>() : meta.getLore());

         for (String line : lines) {
            lore.add(ComponentUtil.toLegacy(line));
         }

         meta.setLore(lore);
      });
   }

   public ItemStackBuilder lore(Iterable<String> lines) {
      return XPrivateMines.isUseMiniMessage() ? this.transformMeta(meta -> {
         List<Component> lore = (List<Component>)(meta.lore() == null ? new ArrayList<>() : meta.lore());

         for (String line : lines) {
            lore.add(TextUtils.toItemComponent(line));
         }

         meta.lore(lore);
      }) : this.transformMeta(meta -> {
         List<String> lore = (List<String>)(meta.getLore() == null ? new ArrayList<>() : meta.getLore());

         for (String line : lines) {
            lore.add(ComponentUtil.toLegacy(line));
         }

         meta.setLore(lore);
      });
   }

   public ItemStackBuilder clearLore() {
      return this.transformMeta(meta -> meta.lore(new ArrayList()));
   }

   public ItemStackBuilder durability(int durability) {
      return this.transform(itemStack -> itemStack.setDurability((short)durability));
   }

   public ItemStackBuilder customModelData(int data) {
      if (!MinecraftVersion.atLeast(MinecraftVersion.V.v1_14)) {
         PrivateMinesLogger.warning("Unable to set custom model data as your server version is not >= 1.14!");
         return this;
      } else {
         return this.transformMeta(meta -> meta.setCustomModelData(data));
      }
   }

   public ItemStackBuilder data(int data) {
      return this.durability(data);
   }

   public ItemStackBuilder amount(int amount) {
      return this.transform(itemStack -> itemStack.setAmount(amount));
   }

   public ItemStackBuilder enchant(Enchantment enchantment, int level) {
      return this.transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, level));
   }

   public ItemStackBuilder enchant(Enchantment enchantment) {
      return this.transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, 1));
   }

   public ItemStackBuilder clearEnchantments() {
      return this.transform(itemStack -> itemStack.getEnchantments().keySet().forEach(itemStack::removeEnchantment));
   }

   public ItemStackBuilder flag(ItemFlag... flags) {
      return this.transformMeta(meta -> meta.addItemFlags(flags));
   }

   public ItemStackBuilder unflag(ItemFlag... flags) {
      return this.transformMeta(meta -> meta.removeItemFlags(flags));
   }

   public ItemStackBuilder hideAttributes() {
      return this.flag(ALL_FLAGS);
   }

   public ItemStackBuilder showAttributes() {
      return this.unflag(ALL_FLAGS);
   }

   public ItemStackBuilder color(Color color) {
      return this.transform(itemStack -> {
         Material type = itemStack.getType();
         if (type == Material.LEATHER_BOOTS || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_HELMET || type == Material.LEATHER_LEGGINGS) {
            LeatherArmorMeta meta = (LeatherArmorMeta)itemStack.getItemMeta();
            meta.setColor(color);
            itemStack.setItemMeta(meta);
         }
      });
   }

   public ItemStackBuilder breakable(boolean flag) {
      return this.transformMeta(meta -> meta.setUnbreakable(!flag));
   }

   public ItemStackBuilder apply(Consumer<ItemStackBuilder> consumer) {
      consumer.accept(this);
      return this;
   }

   public ItemStack build() {
      return this.itemStack;
   }

   public Builder buildItem() {
      return Item.builder(this.build());
   }

   public Item build(@Nullable Runnable handler) {
      return this.buildItem().bind(handler, new ClickType[]{ClickType.RIGHT, ClickType.LEFT}).build();
   }

   public Item build(ClickType type, @Nullable Runnable handler) {
      return this.buildItem().bind(type, handler).build();
   }

   public Item build(@Nullable Runnable rightClick, @Nullable Runnable leftClick) {
      return this.buildItem().bind(ClickType.RIGHT, rightClick).bind(ClickType.LEFT, leftClick).build();
   }

   public Item buildFromMap(Map<ClickType, Runnable> handlers) {
      return this.buildItem().bindAllRunnables(handlers.entrySet()).build();
   }

   public Item buildConsumer(@Nullable Consumer<InventoryClickEvent> handler) {
      return this.buildItem().bind(handler, new ClickType[]{ClickType.RIGHT, ClickType.LEFT}).build();
   }

   public Item buildConsumer(ClickType type, @Nullable Consumer<InventoryClickEvent> handler) {
      return this.buildItem().bind(type, handler).build();
   }

   public Item buildConsumer(@Nullable Consumer<InventoryClickEvent> rightClick, @Nullable Consumer<InventoryClickEvent> leftClick) {
      return this.buildItem().bind(ClickType.RIGHT, rightClick).bind(ClickType.LEFT, leftClick).build();
   }

   public Item buildFromConsumerMap(Map<ClickType, Consumer<InventoryClickEvent>> handlers) {
      return this.buildItem().bindAllConsumers(handlers.entrySet()).build();
   }
}
