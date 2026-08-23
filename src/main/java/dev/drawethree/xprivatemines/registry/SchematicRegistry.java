package dev.drawethree.xprivatemines.registry;

import dev.drawethree.xprivatemines.mines.model.schematic.MinesSchematicImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicRegistry {
   private final Map<String, MinesSchematicImpl> schematicMap = new HashMap<>();
   private MinesSchematicImpl defaultSchematic;

   public void register(List<MinesSchematicImpl> schematics) {
      for (MinesSchematicImpl schematic : schematics) {
         this.register(schematic);
      }
   }

   public void register(MinesSchematicImpl schematic) {
      String key = schematic.getName().toLowerCase();
      this.schematicMap.put(key, schematic);
      if (this.defaultSchematic == null) {
         this.defaultSchematic = schematic;
      }
   }

   public void clear() {
      this.schematicMap.clear();
      this.defaultSchematic = null;
   }

   public MinesSchematicImpl get(String name) {
      return this.schematicMap.get(name.toLowerCase());
   }

   public Collection<MinesSchematicImpl> getAll() {
      return Collections.unmodifiableCollection(this.schematicMap.values());
   }

   public MinesSchematicImpl getDefault() {
      return this.defaultSchematic;
   }
}
