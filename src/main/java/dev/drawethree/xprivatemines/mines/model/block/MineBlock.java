package dev.drawethree.xprivatemines.mines.model.block;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.hook.CustomBlockHook;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public final class MineBlock {
   private final String raw;
   private final XMaterial vanilla;
   private final String customId;
   private final CustomBlockHook customHook;
   private BlockData cachedBlockData;
   private ItemStack cachedIcon;
   private boolean warnedUnresolved;

   private MineBlock(String raw, XMaterial vanilla, String customId, CustomBlockHook customHook) {
      this.raw = raw;
      this.vanilla = vanilla;
      this.customId = customId;
      this.customHook = customHook;
   }

   public static MineBlock parse(String raw) {
      if (raw != null && !raw.isBlank()) {
         String trimmed = raw.trim();
         Optional<XMaterial> mat = XMaterial.matchXMaterial(trimmed);
         if (mat.isPresent()) {
            return new MineBlock(trimmed, mat.get(), null, null);
         } else {
            CustomBlockHook hook = XPrivateMines.getInstance().getCustomBlockHooks().owner(trimmed);
            return new MineBlock(trimmed, null, trimmed, hook);
         }
      } else {
         return null;
      }
   }

   public static MineBlock of(XMaterial material) {
      return material == null ? null : new MineBlock(material.name(), material, null, null);
   }

   public boolean isCustom() {
      return this.customId != null;
   }

   public CustomBlockHook getHook() {
      return this.customHook;
   }

   public String getCustomId() {
      return this.customId;
   }

   public XMaterial getXMaterial() {
      return this.vanilla;
   }

   public BlockData toBlockData() {
      if (this.cachedBlockData != null) {
         return this.cachedBlockData;
      } else if (!this.isCustom()) {
         Material m = this.vanilla.parseMaterial();
         this.cachedBlockData = (m == null ? Material.STONE : m).createBlockData();
         return this.cachedBlockData;
      } else {
         BlockData data = this.customHook.getBaseBlockData(this.customId);
         if (data != null) {
            this.cachedBlockData = data;
            return data;
         } else {
            this.warnUnresolved();
            return Material.STONE.createBlockData();
         }
      }
   }

   public ItemStack toIcon() {
      if (this.cachedIcon != null) {
         return this.cachedIcon.clone();
      } else if (!this.isCustom()) {
         ItemStack item = this.vanilla.parseItem();
         this.cachedIcon = item == null ? new ItemStack(Material.STONE) : item;
         return this.cachedIcon.clone();
      } else {
         ItemStack custom = this.customHook.getItemStack(this.customId);
         if (custom != null) {
            this.cachedIcon = custom;
            return custom.clone();
         } else {
            this.warnUnresolved();
            return new ItemStack(Material.STONE);
         }
      }
   }

   public String serialize() {
      return this.raw;
   }

   public String displayName() {
      if (this.isCustom()) {
         String name = this.customHook.getDisplayName(this.customId);
         if (name != null && !name.isBlank()) {
            return name;
         } else {
            String bare = this.customHook.bareId(this.customId);
            int idx = bare.indexOf(58);
            return prettify(idx >= 0 ? bare.substring(idx + 1) : bare);
         }
      } else {
         return prettify(this.vanilla.name());
      }
   }

   public String permissionNode() {
      return this.isCustom() ? this.customId.toLowerCase().replace(':', '.') : this.vanilla.name().toLowerCase();
   }

   public boolean matches(MineBlock other) {
      return this.equals(other);
   }

   private void warnUnresolved() {
      if (!this.customHook.isEnabled() || this.customHook.isLoaded()) {
         if (!this.warnedUnresolved) {
            this.warnedUnresolved = true;
            if (this.customHook.isEnabled()) {
               PrivateMinesLogger.warning("Unknown " + this.customHook.pluginName() + " block id '" + this.customId + "'; using STONE as fallback.");
            } else {
               PrivateMinesLogger.warning(
                  "Block '" + this.customId + "' requires " + this.customHook.pluginName() + " (not installed); using STONE as fallback."
               );
            }
         }
      }
   }

   private static String prettify(String key) {
      String[] parts = key.toLowerCase().split("_");
      StringBuilder sb = new StringBuilder();

      for (String part : parts) {
         if (!part.isEmpty()) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
         }
      }

      return sb.toString().trim();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof MineBlock other) {
         return !this.isCustom() && !other.isCustom() ? this.vanilla == other.vanilla : Objects.equals(this.customId, other.customId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.isCustom() ? Objects.hashCode(this.customId) : Objects.hashCode(this.vanilla);
   }
}
