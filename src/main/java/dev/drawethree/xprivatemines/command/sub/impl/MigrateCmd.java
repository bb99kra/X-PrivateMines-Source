package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.manager.PrivateMinesManagerImpl;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class MigrateCmd extends PrivateMineSubCommand {
   public MigrateCmd(PrivateMineCommand command) {
      super(command, "migrate");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (args.size() == 1 && args.get(0).equalsIgnoreCase("confirm")) {
         PlayerUtils.sendMessage(sender, "§eStarting migration...");
         Bukkit.getScheduler().runTaskAsynchronously(this.getCommand().getPlugin(), () -> this.migrate(sender));
         return true;
      } else {
         PlayerUtils.sendMessage(sender, "§cThis will convert all private mine files inside /mines folder into one file.");
         PlayerUtils.sendMessage(sender, "§cType: §e/pmine migrate confirm");
         return true;
      }
   }

   private void migrate(CommandSender sender) {
      File oldDir = PrivateMinesManagerImpl.PRIVATE_MINES_DIRECTORY;
      if (oldDir.exists() && oldDir.isDirectory()) {
         File[] files = oldDir.listFiles((dir, name) -> name.endsWith(".yml"));
         if (files != null && files.length != 0) {
            long start = System.currentTimeMillis();
            ConfigurationSection minesSection = this.getCommand().getPlugin().getMinesConfig().getYamlConfig().getConfigurationSection("mines");
            int success = 0;
            int failed = 0;

            for (File file : files) {
               try {
                  String name = file.getName().replace(".yml", "");
                  UUID uuid = UUID.fromString(name);
                  YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(file);
                  minesSection.set(uuid.toString(), oldConfig.getValues(true));
                  if (++success % 5 == 0) {
                     this.send(sender, "§7Migrated §e" + success + "§7 mines...");
                  }
               } catch (IllegalArgumentException var16) {
                  failed++;
                  PrivateMinesLogger.warning("Invalid UUID: " + file.getName());
               } catch (Exception var17) {
                  failed++;
                  PrivateMinesLogger.warning("Failed to migrate: " + file.getName());
                  var17.printStackTrace();
               }
            }

            this.getCommand().getPlugin().getMinesConfig().save();
            long end = System.currentTimeMillis();
            this.send(sender, "§aMigration completed!");
            this.send(sender, "§7Converted: §a" + success);
            this.send(sender, "§7Failed: §c" + failed);
            this.send(sender, "§7Time: §e" + (end - start) + "ms");
            this.send(sender, "§eYou can now safely delete the /mines folder.");
         } else {
            this.send(sender, "§cNo private mines files found.");
         }
      } else {
         this.send(sender, "§cOld /mines folder not found.");
      }
   }

   private void send(CommandSender sender, String msg) {
      Bukkit.getScheduler().runTask(this.getCommand().getPlugin(), () -> PlayerUtils.sendMessage(sender, msg));
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine migrate confirm &7~ &fConvert old mine files into a single file";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      return args.size() == 1 ? List.of("confirm") : List.of();
   }
}
