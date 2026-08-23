package dev.drawethree.xprivatemines.gui.confirmation;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ConfirmationGui extends Gui {
   private static final ItemStack YES_ITEM = ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem())
      .name("&a&lYES")
      .lore("&7Click to confirm this action.")
      .build();
   private static final ItemStack NO_ITEM = ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c&lNO").lore("&7Click to cancel.").build();

   public ConfirmationGui(Player player, String title) {
      super(player, 3, title);
   }

   public void redraw() {
      this.clearItems();

      for (int i = 0; i < 27; i++) {
         if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
            this.setItem(i, ItemStackBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).buildItem().build());
         }
      }

      this.setItem(10, this.getInfoItem());
      this.setItem(12, this.getConfirmationItem(true));
      this.setItem(14, this.getConfirmationItem(false));
   }

   private Item getConfirmationItem(boolean confirm) {
      ItemStack baseItem = confirm ? YES_ITEM : NO_ITEM;
      return ItemStackBuilder.of(baseItem).build(() -> this.confirm(confirm));
   }

   private Item getInfoItem() {
      return ItemStackBuilder.of(Material.BOOK)
         .name("&6&lConfirmation")
         .lore("", "&c&lWARNING!", "&7This action is irreversible.", "&7Are you sure you want to proceed?", "")
         .buildItem()
         .build();
   }

   public abstract void confirm(boolean var1);
}
