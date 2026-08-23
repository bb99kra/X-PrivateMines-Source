package dev.drawethree.xprivatemines.addons;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.XPrivateMinesAPI;
import dev.drawethree.xprivatemines.api.addons.XPrivateMinesAddonContext;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class XPrivateMinesAddonContextImpl implements XPrivateMinesAddonContext {
   private final XPrivateMinesAPI api;
   private final File dataFolder;
   private final Logger logger;
   private final XPrivateMinesAddonMetadata metadata;
   private final XPrivateMines plugin;

   public XPrivateMinesAddonContextImpl(XPrivateMinesAPI api, File dataFolder, XPrivateMinesAddonMetadata metadata, XPrivateMines plugin) {
      this.api = api;
      this.metadata = metadata;
      this.plugin = plugin;
      this.dataFolder = dataFolder;
      this.logger = Logger.getLogger("X-PrivateMines-Addon/" + metadata.getName());
      if (!dataFolder.exists()) {
         dataFolder.mkdirs();
      }
   }

   @Override
   public XPrivateMinesAPI getAPI() {
      return this.api;
   }

   @Override
   public File getDataFolder() {
      return this.dataFolder;
   }

   @Override
   public Logger getLogger() {
      return this.logger;
   }

   @Override
   public String getAddonName() {
      return this.metadata.getName();
   }

   @Override
   public String getAddonVersion() {
      return this.metadata.getVersion();
   }

   @Override
   public String getAddonAuthor() {
      return this.metadata.getAuthor();
   }

   @Override
   public void registerEvents(Listener listener) {
      Bukkit.getPluginManager().registerEvents(listener, this.plugin);
   }
}
