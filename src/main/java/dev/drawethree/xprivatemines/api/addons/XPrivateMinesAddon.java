package dev.drawethree.xprivatemines.api.addons;

public interface XPrivateMinesAddon {
   void onEnable(XPrivateMinesAddonContext var1);

   void onDisable();

   default void onReload() {
   }
}
