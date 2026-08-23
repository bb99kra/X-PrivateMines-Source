package dev.drawethree.xprivatemines.api;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.addons.AddonManager;
import dev.drawethree.xprivatemines.addons.XPrivateMinesAddonMetadata;
import dev.drawethree.xprivatemines.api.addons.XPrivateMinesAddonInfo;
import dev.drawethree.xprivatemines.api.economy.MineEconomyProvider;
import dev.drawethree.xprivatemines.api.manager.MineTierManager;
import dev.drawethree.xprivatemines.api.manager.PrivateMinesManager;
import java.io.File;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class XPrivateMinesAPIImpl implements XPrivateMinesAPI {
   private final XPrivateMines plugin;

   public XPrivateMinesAPIImpl(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   @NotNull
   @Override
   public MineTierManager getTierManager() {
      return this.plugin.getMineTierManager();
   }

   @NotNull
   @Override
   public PrivateMinesManager getMinesManager() {
      return this.plugin.getMinesManager();
   }

   @NotNull
   @Override
   public MineEconomyProvider getEconomyProvider() {
      return this.plugin.getEconomyManager();
   }

   @NotNull
   @Override
   public List<XPrivateMinesAddonInfo> getLoadedAddons() {
      AddonManager mgr = this.plugin.getAddonManager();
      return mgr.getAllAddons()
         .stream()
         .map(
            a -> {
               XPrivateMinesAddonMetadata meta = mgr.getMetadata(a);
               return new XPrivateMinesAddonInfo(
                  meta.getName(), meta.getVersion(), meta.getAuthor(), meta.getDescription(), meta.getMinRequiredVersion(), mgr.isAddonEnabled(a)
               );
            }
         )
         .toList();
   }

   @Override
   public boolean enableAddon(@NotNull String name) {
      AddonManager mgr = this.plugin.getAddonManager();
      return mgr.getAllAddons().stream().filter(a -> mgr.getMetadata(a).getName().equalsIgnoreCase(name)).findFirst().map(a -> {
         mgr.enableAddon(a);
         return true;
      }).orElse(false);
   }

   @Override
   public boolean disableAddon(@NotNull String name) {
      AddonManager mgr = this.plugin.getAddonManager();
      return mgr.getAllAddons().stream().filter(a -> mgr.getMetadata(a).getName().equalsIgnoreCase(name)).findFirst().map(a -> {
         mgr.disableAddon(a);
         return true;
      }).orElse(false);
   }

   @Override
   public boolean loadAddonFromFile(@NotNull String filename) {
      AddonManager mgr = this.plugin.getAddonManager();
      File addonsDir = new File(this.plugin.getDataFolder(), "addons");
      return mgr.loadAddon(new File(addonsDir, filename));
   }
}
