package dev.drawethree.xprivatemines.mines.loader;

import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlPrivateMineLoader implements PrivateMineLoader {
   @Override
   public List<PrivateMineImpl> loadMinesFromFile(File file) {
      if (!file.exists()) {
         PrivateMinesLogger.warning("Mines file does not exist: " + file.getName());
         return List.of();
      } else {
         long start = System.currentTimeMillis();
         YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
         ConfigurationSection minesSection = config.getConfigurationSection("mines");
         if (minesSection == null) {
            PrivateMinesLogger.warning("No 'mines' section found in file.");
            return List.of();
         } else {
            long parseEnd = System.currentTimeMillis();
            PrivateMinesLogger.info("YAML parse completed in " + (parseEnd - start) + "ms");
            Set<String> keys = minesSection.getKeys(false);
            List<PrivateMineImpl> loadedMines = keys.parallelStream().map(key -> {
               UUID uuid;
               try {
                  uuid = UUID.fromString(key);
               } catch (IllegalArgumentException var6x) {
                  PrivateMinesLogger.warning("Invalid UUID key in mines.yml: " + key);
                  return null;
               }

               ConfigurationSection mineSection = minesSection.getConfigurationSection(key);
               if (mineSection == null) {
                  return null;
               } else {
                  try {
                     PrivateMineImpl mine = new PrivateMineImpl(uuid, mineSection);
                     return mine.getSchematic() != null ? mine : null;
                  } catch (Exception var5x) {
                     PrivateMinesLogger.warning("Failed to load mine " + key + ": " + var5x.getMessage());
                     return null;
                  }
               }
            }).filter(Objects::nonNull).toList();
            long end = System.currentTimeMillis();
            PrivateMinesLogger.info(
               "Loaded " + loadedMines.size() + " mines in " + (end - start) + "ms (parse " + (parseEnd - start) + "ms, construct " + (end - parseEnd) + "ms)"
            );
            return loadedMines;
         }
      }
   }
}
