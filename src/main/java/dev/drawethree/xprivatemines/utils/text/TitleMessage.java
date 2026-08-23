package dev.drawethree.xprivatemines.utils.text;

import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.entity.Player;

public final class TitleMessage {
   private final String name;
   private final boolean enabled;
   private final String title;
   private final String subtitle;
   private final int fadeIn;
   private final int stay;
   private final int fadeOut;

   public TitleMessage(String name, boolean enabled, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
      this.name = name;
      this.enabled = enabled;
      this.title = title;
      this.subtitle = subtitle;
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
   }

   public void send(Player player, Map<String, String> placeholders) {
      if (this.enabled) {
         String finalTitle = this.replacePlaceholders(this.title, placeholders);
         String finalSubtitle = this.replacePlaceholders(this.subtitle, placeholders);
         player.sendTitle(finalTitle, finalSubtitle, this.fadeIn, this.stay, this.fadeOut);
      }
   }

   private String replacePlaceholders(String message, Map<String, String> placeholders) {
      String result = message;

      for (Entry<String, String> entry : placeholders.entrySet()) {
         result = result.replace("%" + entry.getKey() + "%", entry.getValue());
      }

      return result;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public String getSubtitle() {
      return this.subtitle;
   }

   @Generated
   public int getFadeIn() {
      return this.fadeIn;
   }

   @Generated
   public int getStay() {
      return this.stay;
   }

   @Generated
   public int getFadeOut() {
      return this.fadeOut;
   }
}
