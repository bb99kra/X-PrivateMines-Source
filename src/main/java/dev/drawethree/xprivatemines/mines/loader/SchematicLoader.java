package dev.drawethree.xprivatemines.mines.loader;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.SchematicSettings;
import dev.drawethree.xprivatemines.config.SchematicSettingsConfig;
import dev.drawethree.xprivatemines.mines.model.schematic.MinesSchematicImpl;
import dev.drawethree.xprivatemines.mines.model.schematic.SchematicSettingsImpl;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicLoader {
   private final SchematicSettingsConfig config;

   public SchematicLoader(XPrivateMines plugin) {
      this.config = plugin.getSchematicSettingsConfig();
   }

   public List<MinesSchematicImpl> loadSchematicsFromDirectory(File directory) {
      List<MinesSchematicImpl> schematics = new ArrayList<>();
      if (!directory.exists()) {
         directory.mkdir();
         PrivateMinesLogger.warning("Schematics directory did not exist. Created new one: " + directory.getAbsolutePath());
      }

      File[] files = directory.listFiles((dir, name) -> name.endsWith(".schem"));
      if (files == null) {
         return schematics;
      } else {
         for (File file : files) {
            try {
               String name = file.getName().replace(".schem", "");
               SchematicSettings settings = SchematicSettingsImpl.fromFile(this.config.getYamlConfig(), name);
               MinesSchematicImpl schematic = new MinesSchematicImpl(file, name, settings);
               schematics.add(schematic);
               PrivateMinesLogger.info("✅ Loaded schematic: " + file.getName());
            } catch (Exception var11) {
               PrivateMinesLogger.warning("❌ Failed to load schematic: " + file.getName());
               var11.printStackTrace();
            }
         }

         return schematics;
      }
   }
}
