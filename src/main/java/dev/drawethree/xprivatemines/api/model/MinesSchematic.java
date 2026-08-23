package dev.drawethree.xprivatemines.api.model;

import java.io.File;

public interface MinesSchematic {
   File getFile();

   String getName();

   SchematicSettings getSettings();
}
