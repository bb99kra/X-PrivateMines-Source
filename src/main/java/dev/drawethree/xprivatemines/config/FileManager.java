package dev.drawethree.xprivatemines.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FileManager {
   private final JavaPlugin plugin;
   private final HashMap<String, FileManager.Config> configs = new HashMap<>();

   public FileManager(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public FileManager.Config getConfig(String name) {
      if (!this.configs.containsKey(name)) {
         this.configs.put(name, new FileManager.Config(name));
      }

      return this.configs.get(name);
   }

   public FileManager.Config saveConfig(String name) {
      return this.getConfig(name).save();
   }

   public FileManager.Config reloadConfig(String name) {
      return this.getConfig(name).reload();
   }

   public class Config {
      private final String name;
      private File file;
      private YamlConfiguration config;

      public Config(String name) {
         this.name = name;
      }

      public FileManager.Config save() {
         if (this.config != null && this.file != null) {
            try {
               if (this.config.getConfigurationSection("").getKeys(true).size() != 0) {
                  this.config.save(this.file);
               }
            } catch (IOException var2) {
               var2.printStackTrace();
            }

            return this;
         } else {
            return this;
         }
      }

      public YamlConfiguration get() {
         if (this.config == null) {
            this.reload();
         }

         return this.config;
      }

      public FileManager.Config saveDefaultConfig() {
         this.file = new File(FileManager.this.plugin.getDataFolder(), this.name);
         FileManager.this.plugin.saveResource(this.name, false);
         return this;
      }

      public FileManager.Config reload() {
         if (this.file == null) {
            this.file = new File(FileManager.this.plugin.getDataFolder(), this.name);
         }

         this.config = YamlConfiguration.loadConfiguration(this.file);

         try {
            Reader defConfigStream = new InputStreamReader(FileManager.this.plugin.getResource(this.name), StandardCharsets.UTF_8);
            if (defConfigStream != null) {
               YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
               this.config.setDefaults(defConfig);
            }
         } catch (NullPointerException var3) {
         }

         return this;
      }

      public FileManager.Config copyDefaults(boolean force) {
         this.get().options().copyDefaults(force);
         return this;
      }

      public FileManager.Config set(String key, Object value) {
         this.get().set(key, value);
         return this;
      }

      public Object get(String key) {
         return this.get().get(key);
      }
   }
}
