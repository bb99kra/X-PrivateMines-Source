package dev.drawethree.xprivatemines.config;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageConfig {
   private static final String CURRENCY_PLACEHOLDER = "%currency%";
   private final XPrivateMines plugin;
   private final FileManager.Config config;
   private Map<String, String> messages;
   private Map<String, TitleMessage> titles;

   public MessageConfig(XPrivateMines plugin) {
      this.plugin = plugin;
      this.config = this.plugin.getFileManager().getConfig("messages.yml").copyDefaults(true).save();
   }

   private void loadMessages() {
      this.messages = new HashMap<>();
      YamlConfiguration configuration = this.getYamlConfig();

      for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
         this.messages.put(key.toLowerCase(), TextUtils.applyColor(configuration.getString("messages." + key)));
      }
   }

   private void loadTitles() {
      this.titles = new HashMap<>();
      ConfigurationSection titlesSection = this.getYamlConfig().getConfigurationSection("titles");
      if (titlesSection == null) {
         PrivateMinesLogger.warning("No 'titles' section found in config.yml!");
      } else {
         for (String key : titlesSection.getKeys(false)) {
            ConfigurationSection section = titlesSection.getConfigurationSection(key);
            if (section != null) {
               boolean enabled = section.getBoolean("enabled", false);
               String title = section.getString("title", "");
               String subtitle = section.getString("subtitle", "");
               int fadeIn = section.getInt("fade-in", 10);
               int stay = section.getInt("stay", 60);
               int fadeOut = section.getInt("fade-out", 15);
               TitleMessage message = new TitleMessage(key, enabled, title, subtitle, fadeIn, stay, fadeOut);
               this.titles.put(key.toLowerCase(), message);
            }
         }
      }
   }

   public String getMessage(String key) {
      return this.applyCurrency(this.messages.getOrDefault(key.toLowerCase(), "No message with key '" + key + "' found"));
   }

   private String applyCurrency(String message) {
      if (message != null && message.contains("%currency%")) {
         String currency = this.plugin.getEconomyManager() == null ? "money" : this.plugin.getEconomyManager().getCurrencyName();
         return message.replace("%currency%", currency);
      } else {
         return message;
      }
   }

   private FileManager.Config getConfig() {
      return this.config;
   }

   public YamlConfiguration getYamlConfig() {
      return this.config.get();
   }

   public void load() {
      this.getConfig().reload();
      this.loadMessages();
      this.loadTitles();
   }

   public void reload() {
      this.load();
   }

   public TitleMessage getTitle(String name) {
      return this.titles.get(name.toLowerCase());
   }
}
