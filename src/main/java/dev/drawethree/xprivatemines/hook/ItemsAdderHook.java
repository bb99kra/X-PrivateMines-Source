package dev.drawethree.xprivatemines.hook;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import me.lucko.helper.Events;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderHook implements CustomBlockHook {
   private final XPrivateMines plugin;
   private volatile boolean loaded;

   public ItemsAdderHook(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   @Override
   public String pluginName() {
      return "ItemsAdder";
   }

   @Override
   public String prefix() {
      return "";
   }

   @Override
   public boolean ownsId(String configId) {
      return true;
   }

   @Override
   public boolean isLoaded() {
      return this.isEnabled() && this.loaded;
   }

   @Override
   public void register() {
      if (this.isEnabled()) {
         Events.subscribe(ItemsAdderLoadDataEvent.class, EventPriority.MONITOR).handler(e -> {
            this.loaded = true;
            this.plugin.refreshCustomBlockConfigs("ItemsAdder");
         }).bindWith(this.plugin);
         PrivateMinesLogger.info("Hooked into ItemsAdder.");
      }
   }

   @Override
   public boolean isCustomBlock(String id) {
      if (this.isLoaded() && id != null) {
         try {
            return CustomBlock.getInstance(id) != null;
         } catch (Throwable var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public BlockData getBaseBlockData(String id) {
      if (this.isLoaded() && id != null) {
         try {
            CustomBlock block = CustomBlock.getInstance(id);
            return block == null ? null : block.getBaseBlockData();
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public ItemStack getItemStack(String id) {
      if (this.isLoaded() && id != null) {
         try {
            CustomBlock block = CustomBlock.getInstance(id);
            return block == null ? null : block.getItemStack();
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public String getDisplayName(String id) {
      if (this.isLoaded() && id != null) {
         try {
            CustomBlock block = CustomBlock.getInstance(id);
            return block == null ? null : block.getDisplayName();
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }
}
