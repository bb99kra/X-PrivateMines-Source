package dev.drawethree.xprivatemines.gui.config;

import java.util.List;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;

public class GUIBannedPlayersConfig {
   private final String title;
   private final int rows;
   private final int banButtonSlot;
   private final String banButtonMaterial;
   private final String banButtonName;
   private final boolean banButtonEnabled;
   private final List<String> banButtonLore;
   private final String bannedPlayerName;
   private final List<String> bannedPlayerLore;

   public GUIBannedPlayersConfig(ConfigurationSection section) {
      this.title = section.getString("title", "&cBanned Players");
      this.rows = section.getInt("rows", 5);
      ConfigurationSection banButton = section.getConfigurationSection("ban-button");
      this.banButtonSlot = banButton.getInt("slot", 44);
      this.banButtonMaterial = banButton.getString("material", "ANVIL");
      this.banButtonName = banButton.getString("name", "&eBan Player");
      this.banButtonLore = banButton.getStringList("lore");
      this.banButtonEnabled = banButton.getBoolean("enabled", true);
      ConfigurationSection bannedPlayer = section.getConfigurationSection("banned-player");
      this.bannedPlayerName = bannedPlayer.getString("name", "&c%player%");
      this.bannedPlayerLore = bannedPlayer.getStringList("lore");
   }

   public String getBannedPlayerName(String player) {
      return this.bannedPlayerName.replace("%player%", player);
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public int getRows() {
      return this.rows;
   }

   @Generated
   public int getBanButtonSlot() {
      return this.banButtonSlot;
   }

   @Generated
   public String getBanButtonMaterial() {
      return this.banButtonMaterial;
   }

   @Generated
   public String getBanButtonName() {
      return this.banButtonName;
   }

   @Generated
   public boolean isBanButtonEnabled() {
      return this.banButtonEnabled;
   }

   @Generated
   public List<String> getBanButtonLore() {
      return this.banButtonLore;
   }

   @Generated
   public List<String> getBannedPlayerLore() {
      return this.bannedPlayerLore;
   }
}
