package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

public class MinePregenService {
   private static final int BATCH_SIZE = 50;
   private static final long DELAY_BETWEEN_BATCHES_TICKS = 200L;
   private final XPrivateMines plugin;
   private final MineCreationService creationService;
   private volatile boolean pregenRunning = false;
   private volatile boolean stopRequested = false;
   private volatile int pregenCompleted = 0;
   private volatile int pregenTotal = 0;
   private BukkitTask scheduledBatchTask;

   public MinePregenService(XPrivateMines plugin, MineCreationService creationService) {
      this.plugin = plugin;
      this.creationService = creationService;
   }

   public void pregen(CommandSender sender, MinesSchematic schematic, int amount) {
      if (this.pregenRunning) {
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&c⚠ Pre-gen is running. Wait for it to complete or stop it first."));
      } else {
         this.stopRequested = false;
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&7Starting to pre-generate &e" + amount + " &7private mine(s)..."));
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&6⚠ Please note: This operation is RAM and CPU heavy and may cause server lag."));
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&6It involves loading chunks, fixing lightning and pasting schematic files,"));
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&6which can be intensive depending on the size and complexity of the schematic."));
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&6Mine resetting feature might be delayed because of FAWE Queues."));
         PlayerUtils.sendMessage(sender, TextUtils.applyColor("&6Avoid performing this on a live server or during peak times."));
         this.startPregen(sender, schematic, amount);
      }
   }

   public void startPregen(CommandSender sender, MinesSchematic schematic, int totalRequested) {
      this.pregenRunning = true;
      int total = Math.min(totalRequested, 1000);
      this.pregenCompleted = 0;
      this.pregenTotal = total;
      PlayerUtils.sendMessage(sender, TextUtils.applyColor("&e⛏ Starting pre-generation of &b" + total + " &emining areas in batches of &b50"));
      this.runPregenBatch(sender, schematic, total, 0);
   }

   private void runPregenBatch(CommandSender sender, MinesSchematic schematic, int total, int current) {
      if (this.stopRequested) {
         this.finishPregen(sender);
      } else {
         int end = Math.min(current + 50, total);
         this.runPregenSingle(sender, schematic, total, current, end);
      }
   }

   private void runPregenSingle(CommandSender sender, MinesSchematic schematic, int total, int current, int end) {
      if (this.stopRequested) {
         this.finishPregen(sender);
      } else if (current >= end) {
         if (end >= total) {
            PlayerUtils.sendMessage(sender, TextUtils.applyColor("&a✅ Finished pre-generating &e" + total + " &amining areas."));
            this.pregenRunning = false;
            this.stopRequested = false;
            this.pregenCompleted = 0;
            this.pregenTotal = 0;
         } else {
            PlayerUtils.sendMessage(sender, TextUtils.applyColor("&6⏳ Waiting 10 seconds before starting next batch..."));
            this.scheduledBatchTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.runPregenBatch(sender, schematic, total, end), 200L);
         }
      } else {
         int mineNumber = current + 1;
         double percent = (double)mineNumber / total * 100.0;
         this.creationService
            .createPrivateMine(null, schematic)
            .whenComplete(
               (mine, throwable) -> {
                  if (this.stopRequested) {
                     this.finishPregen(sender);
                  } else if (throwable != null) {
                     Bukkit.getScheduler()
                        .runTask(this.plugin, () -> PlayerUtils.sendMessage(sender, TextUtils.applyColor("&c✖ Failed to generate private mine #" + mineNumber)));
                     throwable.printStackTrace();
                  } else {
                     Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              this.pregenCompleted++;
                              PlayerUtils.sendMessage(
                                 sender,
                                 TextUtils.applyColor(
                                    String.format(
                                       "&a✔ Generated mine &e%d/%d &7(&a%.1f%%&7) at &e%d&7, &e%d",
                                       mineNumber,
                                       total,
                                       percent,
                                       mine.getLocationSettings().getXOffset(),
                                       mine.getLocationSettings().getZOffset()
                                    )
                                 )
                              );
                           }
                        );
                     long delayTicks = 20L;
                     Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.runPregenSingle(sender, schematic, total, current + 1, end), delayTicks);
                  }
               }
            );
      }
   }

   public boolean isRunning() {
      return this.pregenRunning;
   }

   public int getCompleted() {
      return this.pregenCompleted;
   }

   public int getTotal() {
      return this.pregenTotal;
   }

   public void stopPregen() {
      if (this.pregenRunning) {
         this.stopRequested = true;
         if (this.scheduledBatchTask != null) {
            this.scheduledBatchTask.cancel();
            this.scheduledBatchTask = null;
         }
      }
   }

   private void finishPregen(CommandSender sender) {
      this.pregenRunning = false;
      this.stopRequested = false;
      this.pregenCompleted = 0;
      this.pregenTotal = 0;
      PlayerUtils.sendMessage(sender, TextUtils.applyColor("&c⛔ Pre-generation was stopped."));
   }
}
