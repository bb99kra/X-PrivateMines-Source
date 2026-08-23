package dev.drawethree.xprivatemines.hook;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import me.lucko.helper.Events;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OraxenHook implements CustomBlockHook {
   public static final String PREFIX = "oraxen:";
   private final XPrivateMines plugin;
   private volatile boolean loaded;

   public OraxenHook(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   @Override
   public String pluginName() {
      return "Oraxen";
   }

   @Override
   public String prefix() {
      return "oraxen:";
   }

   @Override
   public boolean isLoaded() {
      return this.isEnabled() && this.loaded;
   }

   @Override
   public void register() {
      if (this.isEnabled()) {
         Events.subscribe(OraxenItemsLoadedEvent.class, EventPriority.MONITOR).handler(e -> {
            this.loaded = true;
            this.plugin.refreshCustomBlockConfigs("Oraxen");
         }).bindWith(this.plugin);
         PrivateMinesLogger.info("Hooked into Oraxen.");
      }
   }

   @Override
   public boolean isCustomBlock(String configId) {
      if (this.isLoaded() && configId != null) {
         try {
            return OraxenBlocks.isOraxenBlock(this.bareId(configId));
         } catch (Throwable var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public BlockData getBaseBlockData(String configId) {
      if (this.isLoaded() && configId != null) {
         try {
            return OraxenBlocks.getOraxenBlockData(this.bareId(configId));
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public ItemStack getItemStack(String configId) {
      if (this.isLoaded() && configId != null) {
         try {
            ItemBuilder builder = OraxenItems.getItemById(this.bareId(configId));
            return builder == null ? null : builder.build();
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public String getDisplayName(String configId) {
      ItemStack item = this.getItemStack(configId);
      if (item == null) {
         return null;
      } else {
         ItemMeta meta = item.getItemMeta();
         return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null;
      }
   }
}
