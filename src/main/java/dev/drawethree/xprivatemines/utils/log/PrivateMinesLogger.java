package dev.drawethree.xprivatemines.utils.log;

import dev.drawethree.xprivatemines.utils.text.TextUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PrivateMinesLogger {
   private static Logger LOGGER;

   private PrivateMinesLogger() {
   }

   public static void setLogger(Logger logger) {
      if (LOGGER == null) {
         if (logger == null) {
            throw new IllegalArgumentException("Logger cannot be null.");
         } else {
            LOGGER = logger;
         }
      }
   }

   private static Logger getLogger() {
      if (LOGGER == null) {
         throw new IllegalStateException("Logger has not been initialized. Call setLogger() first.");
      } else {
         return LOGGER;
      }
   }

   public static void info(String msg) {
      getLogger().info(TextUtils.applyColor(msg));
   }

   public static void warning(String msg) {
      getLogger().warning(TextUtils.applyColor("&c" + msg));
   }

   public static void error(String msg) {
      log(Level.SEVERE, msg);
      getLogger().warning(TextUtils.applyColor("&4" + msg));
   }

   public static void log(Level level, String msg) {
      getLogger().log(level, TextUtils.applyColor(msg));
   }
}
