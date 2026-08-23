package dev.drawethree.xprivatemines.config;

import dev.drawethree.xprivatemines.XPrivateMines;
import org.bukkit.configuration.file.YamlConfiguration;

public class SchematicSettingsConfig {
   private final XPrivateMines plugin;
   private final FileManager.Config config;

   public SchematicSettingsConfig(XPrivateMines plugin) {
      this.plugin = plugin;
      this.config = this.plugin.getFileManager().getConfig("schematic-settings.yml").copyDefaults(true).save();
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

   public void set(String path, Object value) {
      this.config.set(path, value);
   }

   public void save() {
      this.config.save();
   }

   public boolean hasSchematic(String name) {
      return this.getYamlConfig().isConfigurationSection("schematic-settings." + name.toLowerCase());
   }
}
