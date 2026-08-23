package dev.drawethree.xprivatemines.task;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.io.IOException;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

public class PrivateMineAutoSaveTask extends BukkitRunnable {
   private final XPrivateMines plugin;

   public PrivateMineAutoSaveTask(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void run() {
      List<PrivateMineImpl> toSave = this.plugin.getMinesManager().getAllInternal().stream().filter(PrivateMineImpl::isDirty).toList();
      if (!toSave.isEmpty()) {
         PrivateMinesLogger.info("⏳ Auto-saving " + toSave.size() + " modified private mine(s)...");

         for (PrivateMineImpl mine : toSave) {
            try {
               mine.saveToConfig();
               mine.setDirty(false);
            } catch (IOException var5) {
               PrivateMinesLogger.warning("⚠ Failed to save mine for " + mine.getOwner() + ": " + var5.getMessage());
               var5.printStackTrace();
            }
         }

         PrivateMinesLogger.info("✅ Done auto-saving modified private mines.");
      }
   }

   public void start() {
      this.runTaskTimerAsynchronously(this.plugin, 0L, this.plugin.getPrivateMinesConfig().getAutoSaveInterval() * 20L);
   }
}
