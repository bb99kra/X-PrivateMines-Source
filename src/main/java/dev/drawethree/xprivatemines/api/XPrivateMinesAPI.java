package dev.drawethree.xprivatemines.api;

import dev.drawethree.xprivatemines.api.addons.XPrivateMinesAddonInfo;
import dev.drawethree.xprivatemines.api.economy.MineEconomyProvider;
import dev.drawethree.xprivatemines.api.manager.MineTierManager;
import dev.drawethree.xprivatemines.api.manager.PrivateMinesManager;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface XPrivateMinesAPI {
   @NotNull
   MineTierManager getTierManager();

   @NotNull
   PrivateMinesManager getMinesManager();

   @NotNull
   MineEconomyProvider getEconomyProvider();

   @NotNull
   List<XPrivateMinesAddonInfo> getLoadedAddons();

   boolean enableAddon(@NotNull String var1);

   boolean disableAddon(@NotNull String var1);

   boolean loadAddonFromFile(@NotNull String var1);

   @NotNull
   static XPrivateMinesAPI getInstance() {
      return XPrivateMinesAPI.InstanceHolder.INSTANCE;
   }

   static void setInstance(@NotNull XPrivateMinesAPI instance) {
      if (XPrivateMinesAPI.InstanceHolder.INSTANCE != null) {
         throw new IllegalStateException("XPrivateMines API is already initialized!");
      } else {
         XPrivateMinesAPI.InstanceHolder.INSTANCE = instance;
      }
   }

   public static class InstanceHolder {
      private static XPrivateMinesAPI INSTANCE;
   }
}
