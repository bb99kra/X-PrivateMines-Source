package dev.drawethree.xprivatemines.utils;

import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.EnumMap;
import org.bukkit.configuration.file.FileConfiguration;

public class PlaceholderUtils {
   private static final EnumMap<PlaceholderUtils.PlaceholderType, String> TRANSLATIONS = new EnumMap<>(PlaceholderUtils.PlaceholderType.class);

   public static void init(FileConfiguration config) {
      loadTranslation(config, PlaceholderUtils.PlaceholderType.IS_OPEN_YES, "placeholders.xprivatemines_is_open.true");
      loadTranslation(config, PlaceholderUtils.PlaceholderType.IS_OPEN_NO, "placeholders.xprivatemines_is_open.false");
      loadTranslation(config, PlaceholderUtils.PlaceholderType.HAS_YES, "placeholders.xprivatemines_has.true");
      loadTranslation(config, PlaceholderUtils.PlaceholderType.HAS_NO, "placeholders.xprivatemines_has.false");
   }

   private static void loadTranslation(FileConfiguration config, PlaceholderUtils.PlaceholderType type, String path) {
      String translation = config.getString(path);
      TRANSLATIONS.put(type, TextUtils.applyColor(translation));
   }

   public static String getTranslation(PlaceholderUtils.PlaceholderType type) {
      return TRANSLATIONS.get(type);
   }

   public static enum PlaceholderType {
      IS_OPEN_YES,
      IS_OPEN_NO,
      HAS_YES,
      HAS_NO;
   }
}
