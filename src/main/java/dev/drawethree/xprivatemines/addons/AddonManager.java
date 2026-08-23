package dev.drawethree.xprivatemines.addons;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.XPrivateMinesAPI;
import dev.drawethree.xprivatemines.api.addons.XPrivateMinesAddon;
import dev.drawethree.xprivatemines.api.addons.XPrivateMinesAddonContext;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class AddonManager {
   private final XPrivateMines plugin;
   private final File addonsFolder;
   private final List<XPrivateMinesAddon> loadedAddons = new ArrayList<>();
   private final List<XPrivateMinesAddon> enabledAddons = new ArrayList<>();
   private final Map<XPrivateMinesAddon, XPrivateMinesAddonMetadata> metadataMap = new HashMap<>();
   private final Map<XPrivateMinesAddon, URLClassLoader> classLoaders = new HashMap<>();

   public AddonManager(XPrivateMines plugin) {
      this.plugin = plugin;
      this.addonsFolder = new File(plugin.getDataFolder(), "addons");
      if (!this.addonsFolder.exists()) {
         this.addonsFolder.mkdirs();
      }
   }

   public void loadAddons() {
      File[] files = this.addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
      if (files != null) {
         for (File file : files) {
            this.loadAddon(file);
         }

         PrivateMinesLogger.info("Loaded " + this.loadedAddons.size() + " addons.");
      }
   }

   public boolean loadAddon(File file) {
      String filename = file.getName();
      boolean alreadyLoaded = this.metadataMap.keySet().stream().anyMatch(a -> {
         URLClassLoader cl = this.classLoaders.get(a);
         return cl != null && cl.getURLs().length > 0 && cl.getURLs()[0].getFile().endsWith(filename);
      });
      if (alreadyLoaded) {
         PrivateMinesLogger.warning("Addon " + filename + " is already loaded. Skipping.");
         return false;
      } else {
         try {
            boolean var12;
            try (JarFile jarFile = new JarFile(file)) {
               Attributes attrs = jarFile.getManifest().getMainAttributes();
               XPrivateMinesAddonMetadata metadata = this.readMetadata(attrs);
               String mainClass = attrs.getValue("X-PrivateMines-Addon-Class");
               if (mainClass == null) {
                  PrivateMinesLogger.warning("Skipping " + file.getName() + ": no X-PrivateMines-Addon-Class attribute in manifest.");
                  return false;
               }

               this.checkVersionCompatibility(metadata, file.getName());
               URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
               Class<?> clazz = classLoader.loadClass(mainClass);
               if (!(clazz.getDeclaredConstructor().newInstance() instanceof XPrivateMinesAddon addon)) {
                  PrivateMinesLogger.warning("Skipping " + file.getName() + ": " + mainClass + " does not implement XPrivateMinesAddon.");
                  classLoader.close();
                  return false;
               }

               this.loadedAddons.add(addon);
               this.metadataMap.put(addon, metadata);
               this.classLoaders.put(addon, classLoader);
               this.enableAddon(addon);
               PrivateMinesLogger.info("Loaded addon: " + metadata.getName() + " v" + metadata.getVersion() + " by " + metadata.getAuthor());
               var12 = true;
            }

            return var12;
         } catch (Exception var15) {
            PrivateMinesLogger.error("Failed to load addon: " + file.getName());
            var15.printStackTrace();
            return false;
         }
      }
   }

   private void checkVersionCompatibility(XPrivateMinesAddonMetadata metadata, String fileName) {
      String minVersion = metadata.getMinRequiredVersion();
      if (minVersion != null && !minVersion.isEmpty()) {
         String pluginVersion = this.plugin.getDescription().getVersion();
         if (!this.isVersionSufficient(pluginVersion, minVersion)) {
            PrivateMinesLogger.warning(
               "Addon " + fileName + " requires X-PrivateMines " + minVersion + " or higher (running " + pluginVersion + "). It may not work correctly."
            );
         }
      }
   }

   private boolean isVersionSufficient(String actual, String required) {
      try {
         String[] a = actual.split("-")[0].split("\\.");
         String[] r = required.split("-")[0].split("\\.");
         int len = Math.max(a.length, r.length);

         for (int i = 0; i < len; i++) {
            int av = i < a.length ? Integer.parseInt(a[i]) : 0;
            int rv = i < r.length ? Integer.parseInt(r[i]) : 0;
            if (av != rv) {
               return av > rv;
            }
         }

         return true;
      } catch (NumberFormatException var9) {
         return true;
      }
   }

   private XPrivateMinesAddonMetadata readMetadata(Attributes attrs) {
      return new XPrivateMinesAddonMetadata(
         attrs.getValue("X-PrivateMines-Addon-Name"),
         attrs.getValue("X-PrivateMines-Addon-Description"),
         attrs.getValue("X-PrivateMines-Addon-Version"),
         attrs.getValue("X-PrivateMines-Addon-Author"),
         attrs.getValue("X-PrivateMines-Min-Version")
      );
   }

   public void unloadAddons() {
      for (XPrivateMinesAddon addon : new ArrayList<>(this.loadedAddons)) {
         try {
            addon.onDisable();
         } catch (Exception var4) {
            PrivateMinesLogger.warning("Error while disabling addon: " + this.getMetadata(addon).getName());
            var4.printStackTrace();
         }

         this.closeClassLoader(addon);
      }

      this.loadedAddons.clear();
      this.enabledAddons.clear();
      this.metadataMap.clear();
      this.classLoaders.clear();
   }

   private void closeClassLoader(XPrivateMinesAddon addon) {
      URLClassLoader cl = this.classLoaders.get(addon);
      if (cl != null) {
         try {
            cl.close();
         } catch (IOException var4) {
            PrivateMinesLogger.warning("Failed to close classloader for addon: " + this.getMetadata(addon).getName());
         }
      }
   }

   public void enableAddon(XPrivateMinesAddon addon) {
      if (!this.isAddonEnabled(addon)) {
         try {
            XPrivateMinesAddonMetadata metadata = this.getMetadata(addon);
            String folderName = !metadata.getName().isEmpty() ? metadata.getName() : addon.getClass().getSimpleName();
            File dataFolder = new File(this.addonsFolder, folderName);
            XPrivateMinesAddonContext context = new XPrivateMinesAddonContextImpl(XPrivateMinesAPI.getInstance(), dataFolder, metadata, this.plugin);
            addon.onEnable(context);
            this.enabledAddons.add(addon);
         } catch (Exception var6) {
            PrivateMinesLogger.error("Failed to enable addon: " + this.getMetadata(addon).getName());
            var6.printStackTrace();
         }
      }
   }

   public void disableAddon(XPrivateMinesAddon addon) {
      if (this.isAddonEnabled(addon)) {
         try {
            addon.onDisable();
         } catch (Exception var3) {
            PrivateMinesLogger.warning("Error while disabling addon: " + this.getMetadata(addon).getName());
            var3.printStackTrace();
         }

         this.enabledAddons.remove(addon);
      }
   }

   public XPrivateMinesAddonMetadata getMetadata(XPrivateMinesAddon addon) {
      return this.metadataMap.getOrDefault(addon, new XPrivateMinesAddonMetadata());
   }

   public List<XPrivateMinesAddon> getAllAddons() {
      return this.loadedAddons;
   }

   public boolean isAddonEnabled(XPrivateMinesAddon addon) {
      return this.enabledAddons.contains(addon);
   }

   public void reloadAddons() {
      for (XPrivateMinesAddon addon : new ArrayList<>(this.enabledAddons)) {
         try {
            addon.onReload();
         } catch (Exception var4) {
            PrivateMinesLogger.warning("Error while reloading addon: " + this.getMetadata(addon).getName());
            var4.printStackTrace();
         }
      }
   }
}
