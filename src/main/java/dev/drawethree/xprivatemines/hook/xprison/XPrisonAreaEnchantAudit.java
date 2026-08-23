package dev.drawethree.xprivatemines.hook.xprison;

import dev.drawethree.xprison.api.XPrisonAPI;
import dev.drawethree.xprison.api.enchants.XPrisonEnchantsAPI;
import dev.drawethree.xprison.api.enchants.area.AreaBreakContext;
import dev.drawethree.xprison.api.enchants.area.AreaBreakSettings;
import dev.drawethree.xprison.api.enchants.area.BreakEventStrategy;
import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.util.ArrayList;
import java.util.List;
import me.lucko.helper.Schedulers;

public final class XPrisonAreaEnchantAudit {
   private static final long AUDIT_DELAY_TICKS = 100L;

   private XPrisonAreaEnchantAudit() {
   }

   public static void scheduleIfNeeded(XPrivateMines plugin) {
      if (plugin.isXPrisonEnabled() && !plugin.isPacketMinesActive()) {
         try {
            Class.forName("dev.drawethree.xprison.api.enchants.area.AreaBreakContext");
         } catch (ClassNotFoundException var2) {
            return;
         }

         Schedulers.sync().runLater(() -> audit(plugin), 100L);
      }
   }

   private static void audit(XPrivateMines plugin) {
      if (plugin.isXPrisonEnabled() && !plugin.isPacketMinesActive()) {
         List<String> unobservable;
         try {
            unobservable = findUnobservableEnchants();
         } catch (LinkageError | RuntimeException var3) {
            return;
         }

         if (!unobservable.isEmpty()) {
            PrivateMinesLogger.warning("================================================================");
            PrivateMinesLogger.warning("X-Prison area enchant(s) " + String.join(", ", unobservable) + " destroy mine blocks");
            PrivateMinesLogger.warning("without announcing them (countBlocksBroken: false with an aggregate/no event");
            PrivateMinesLogger.warning("strategy). Private mines cannot count those blocks, so their automatic reset");
            PrivateMinesLogger.warning("will trigger late or not at all.");
            PrivateMinesLogger.warning("Fix: set \"countBlocksBroken\": true in the affected enchants' JSON files");
            PrivateMinesLogger.warning("(X-Prison/enchants/*.json), or switch them to \"eventStrategy\": \"PER_BLOCK\".");
            PrivateMinesLogger.warning("================================================================");
         }
      }
   }

   private static List<String> findUnobservableEnchants() {
      XPrisonEnchantsAPI enchantsApi = XPrisonAPI.getInstance().getEnchantsApi();
      List<String> names = new ArrayList<>();

      for (XPrisonEnchantment enchantment : enchantsApi.getAllEnchantments()) {
         if (enchantment instanceof AreaBreakContext area && area.shouldRemoveBlocks()) {
            AreaBreakSettings settings = area.areaSettings();
            if (settings.eventStrategy() != BreakEventStrategy.PER_BLOCK && !settings.countBlocksBroken()) {
               names.add(enchantment.getRawName());
            }
         }
      }

      return names;
   }
}
