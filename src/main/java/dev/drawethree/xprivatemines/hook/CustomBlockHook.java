package dev.drawethree.xprivatemines.hook;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public interface CustomBlockHook {
   String pluginName();

   String prefix();

   default boolean isEnabled() {
      return Bukkit.getPluginManager().isPluginEnabled(this.pluginName());
   }

   boolean isLoaded();

   default boolean ownsId(String configId) {
      String p = this.prefix();
      return configId != null && !p.isEmpty() && configId.startsWith(p);
   }

   boolean isCustomBlock(String var1);

   BlockData getBaseBlockData(String var1);

   ItemStack getItemStack(String var1);

   String getDisplayName(String var1);

   void register();

   default String bareId(String configId) {
      String p = this.prefix();
      return !p.isEmpty() && configId != null && configId.startsWith(p) ? configId.substring(p.length()) : configId;
   }
}
