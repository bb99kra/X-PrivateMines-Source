package dev.drawethree.xprivatemines.utils.wand;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class WandUtil {
   private static final String WAND_KEY_ID = "schematic_wand";
   private static NamespacedKey key;

   private WandUtil() {
      throw new UnsupportedOperationException("Cannot instantiate");
   }

   private static NamespacedKey key() {
      if (key == null) {
         key = new NamespacedKey(XPrivateMines.getInstance(), "schematic_wand");
      }

      return key;
   }

   public static ItemStack createWand() {
      return ItemStackBuilder.of(Material.BLAZE_ROD)
         .name("&6&lSchematic Wand")
         .lore(
            "&7Define a custom mine schematic in-game.",
            "",
            "&eLeft-click &7a block  &8» &acorner 1",
            "&eRight-click &7a block &8» &acorner 2",
            "",
            "&7Then open &f/pmine schematic &7and click",
            "&7a slot to save your selection."
         )
         .transformMeta(WandUtil::tag)
         .build();
   }

   public static boolean isWand(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && meta.getPersistentDataContainer().has(key(), PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   private static void tag(ItemMeta meta) {
      meta.getPersistentDataContainer().set(key(), PersistentDataType.BYTE, (byte)1);
   }
}
