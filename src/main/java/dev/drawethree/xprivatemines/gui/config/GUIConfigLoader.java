package dev.drawethree.xprivatemines.gui.config;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class GUIConfigLoader {
   private final XPrivateMines plugin;

   public GUIConfigLoader(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public GUIConfigLoader.GUIItemConfig getItem(String path) {
      YamlConfiguration config = this.plugin.getGuiConfig();
      String material = config.getString(path + ".material", "STONE");
      String name = config.getString(path + ".name", "&fUndefined");
      List<String> lore = config.getStringList(path + ".lore");
      int slot = config.getInt(path + ".slot");
      boolean enabled = config.getBoolean(path + ".enabled", true);
      return new GUIConfigLoader.GUIItemConfig(material, name, lore, slot, enabled);
   }

   public ItemStack buildFromConfig(GUIConfigLoader.GUIItemConfig config, Map<String, String> placeholders) {
      MineBlock block = MineBlock.parse(config.material());
      ItemStack base = block != null ? block.toIcon() : XMaterial.STONE.parseItem();
      ItemStackBuilder builder = ItemStackBuilder.of(base);
      String name = config.name();
      List<String> lore = new ArrayList<>();

      for (String line : config.lore()) {
         lore.add(this.replacePlaceholders(line, placeholders));
      }

      builder.name(this.replacePlaceholders(name, placeholders)).lore(lore);
      return builder.build();
   }

   private String replacePlaceholders(String input, Map<String, String> placeholders) {
      for (Entry<String, String> entry : placeholders.entrySet()) {
         input = input.replace("%" + entry.getKey() + "%", entry.getValue());
      }

      return input;
   }

   public int getRows(String path) {
      return this.plugin.getGuiConfig().getInt(path + ".rows", 3);
   }

   public String getTitle(String path) {
      return this.plugin.getGuiConfig().getString(path + ".title", "&fDefault GUI");
   }

   public GUIBlockChangeConfig getBlockChangeGuiConfig() {
      ConfigurationSection section = this.plugin.getGuiConfig().getConfigurationSection("block-change-gui");
      if (section == null) {
         PrivateMinesLogger.warning("Missing 'block-change-gui' section in guis.yml!");
         return null;
      } else {
         return new GUIBlockChangeConfig(section);
      }
   }

   public GUIBannedPlayersConfig getBannedPlayersGuiConfig() {
      ConfigurationSection section = this.plugin.getGuiConfig().getConfigurationSection("private-mine-banned-players-gui");
      if (section == null) {
         PrivateMinesLogger.warning("Missing 'private-mine-banned-players-gui' section in guis.yml!");
         return null;
      } else {
         return new GUIBannedPlayersConfig(section);
      }
   }

   public record GUIItemConfig(String material, String name, List<String> lore, int slot, boolean enabled) {
   }
}
