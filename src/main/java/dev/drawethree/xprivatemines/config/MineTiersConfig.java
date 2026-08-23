package dev.drawethree.xprivatemines.config;

import dev.drawethree.xprivatemines.XPrivateMines;
import org.bukkit.configuration.file.YamlConfiguration;

public class MineTiersConfig {
   private final XPrivateMines plugin;
   private final FileManager.Config config;

   public MineTiersConfig(XPrivateMines plugin) {
      this.plugin = plugin;
      this.config = this.plugin.getFileManager().getConfig("mine-tiers.yml").copyDefaults(true).save();
   }

   private FileManager.Config getConfig() {
      return this.config;
   }

   public YamlConfiguration getYamlConfig() {
      return this.config.get();
   }

   public void load() {
      this.getConfig().reload();
   }

   public void reload() {
      this.load();
   }
}
