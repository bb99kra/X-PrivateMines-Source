package dev.drawethree.xprivatemines.task;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.manager.PrivateMinesManagerImpl;
import org.bukkit.scheduler.BukkitRunnable;

public class PrivateMineResetTask extends BukkitRunnable {
   private final XPrivateMines plugin;
   private final PrivateMinesManagerImpl manager;

   public PrivateMineResetTask(XPrivateMines plugin, PrivateMinesManagerImpl manager) {
      this.plugin = plugin;
      this.manager = manager;
   }

   public void start() {
      this.runTaskTimerAsynchronously(this.plugin, 0L, this.plugin.getPrivateMinesConfig().getResetCheckInterval() * 20L);
   }

   public void run() {
      for (PrivateMine mine : this.manager.getAll()) {
         if (this.manager.shouldReset(mine)) {
            this.manager.refill(mine);
         }
      }
   }
}
