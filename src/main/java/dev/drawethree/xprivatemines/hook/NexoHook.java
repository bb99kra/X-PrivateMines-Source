package dev.drawethree.xprivatemines.hook;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import me.lucko.helper.Events;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NexoHook implements CustomBlockHook {
   public static final String PREFIX = "nexo:";
   private final XPrivateMines plugin;
   private volatile boolean loaded;

   public NexoHook(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   @Override
   public String pluginName() {
      return "Nexo";
   }

   @Override
   public String prefix() {
      return "nexo:";
   }

   @Override
   public boolean isLoaded() {
      return this.isEnabled() && this.loaded;
   }

   @Override
   public void register() {
      if (this.isEnabled()) {
         Events.subscribe(NexoItemsLoadedEvent.class, EventPriority.MONITOR).handler(e -> {
            this.loaded = true;
            this.plugin.refreshCustomBlockConfigs("Nexo");
         }).bindWith(this.plugin);
         PrivateMinesLogger.info("Hooked into Nexo.");
      }
   }

   @Override
   public boolean isCustomBlock(String configId) {
      if (this.isLoaded() && configId != null) {
         try {
            return NexoBlocks.isCustomBlock(this.bareId(configId));
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
            return NexoBlocks.blockData(this.bareId(configId));
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
            ItemBuilder builder = NexoItems.itemFromId(this.bareId(configId));
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
