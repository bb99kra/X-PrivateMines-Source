package dev.drawethree.xprivatemines.config;

import dev.drawethree.xprivatemines.XPrivateMines;
import org.bukkit.configuration.file.YamlConfiguration;

public class MinesConfig {
   private final XPrivateMines plugin;
   private final FileManager.Config config;

   public MinesConfig(XPrivateMines plugin) {
      this.plugin = plugin;
      this.config = this.plugin.getFileManager().getConfig("mines.yml").reload();
   }

   private FileManager.Config getConfig() {
      return this.config;
   }

   public YamlConfiguration getYamlConfig() {
      return this.config.get();
   }

   public void load() {
   }

   public void reload() {
   }

   public void save() {
      this.getConfig().save();
   }
}
