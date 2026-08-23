package dev.drawethree.xprivatemines.api.addons;

import dev.drawethree.xprivatemines.api.XPrivateMinesAPI;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.event.Listener;

public interface XPrivateMinesAddonContext {
   XPrivateMinesAPI getAPI();

   File getDataFolder();

   Logger getLogger();

   String getAddonName();

   String getAddonVersion();

   String getAddonAuthor();

   void registerEvents(Listener var1);
}
