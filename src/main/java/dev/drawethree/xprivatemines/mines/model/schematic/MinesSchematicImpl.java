package dev.drawethree.xprivatemines.mines.model.schematic;

import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.api.model.SchematicSettings;
import java.io.File;
import lombok.Generated;

public class MinesSchematicImpl implements MinesSchematic {
   private File file;
   private String name;
   private SchematicSettings settings;

   @Generated
   public MinesSchematicImpl(File file, String name, SchematicSettings settings) {
      this.file = file;
      this.name = name;
      this.settings = settings;
   }

   @Generated
   @Override
   public File getFile() {
      return this.file;
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public SchematicSettings getSettings() {
      return this.settings;
   }
}
