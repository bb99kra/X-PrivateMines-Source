package dev.drawethree.xprivatemines.gui.config;

import java.util.List;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;

public class GUIBlockChangeConfig {
   private final String title;
   private final int rows;
   private final List<String> selectedLore;
   private final List<String> unlockedLore;
   private final List<String> lockedLore;

   public GUIBlockChangeConfig(ConfigurationSection section) {
      this.title = section.getString("title", "&e&lPrivate Mine &7- &fBlock Selection");
      this.rows = section.getInt("rows", 5);
      this.selectedLore = section.getStringList("selected-lore");
      this.unlockedLore = section.getStringList("unlocked-lore");
      this.lockedLore = section.getStringList("locked-lore");
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
   public List<String> getSelectedLore() {
      return this.selectedLore;
   }

   @Generated
   public List<String> getUnlockedLore() {
      return this.unlockedLore;
   }

   @Generated
   public List<String> getLockedLore() {
      return this.lockedLore;
   }
}
