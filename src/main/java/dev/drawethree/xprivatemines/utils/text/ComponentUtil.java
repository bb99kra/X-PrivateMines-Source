package dev.drawethree.xprivatemines.utils.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class ComponentUtil {
   private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

   public static Component fromLegacy(String legacy) {
      if (legacy == null) {
         return Component.empty();
      } else {
         String formatted = ChatColor.translateAlternateColorCodes('&', legacy);
         return LEGACY_SERIALIZER.deserialize(formatted);
      }
   }

   public static String toLegacy(String miniMessageString) {
      MiniMessage miniMessage = MiniMessage.miniMessage();
      LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder().character('&').build();
      Component component = miniMessage.deserialize(miniMessageString);
      return legacySerializer.serialize(component);
   }
}
