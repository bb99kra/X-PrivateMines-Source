package dev.drawethree.xprivatemines.mines.loader;

import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import java.io.File;
import java.util.List;

public interface PrivateMineLoader {
   List<PrivateMineImpl> loadMinesFromFile(File var1);
}
