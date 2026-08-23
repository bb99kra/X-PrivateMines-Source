package dev.drawethree.xprivatemines.virtual.bridge;

import dev.drawethree.xprison.api.virtualblocks.VirtualBlockProviders;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.virtual.VirtualMineEngine;

public final class XPrisonBridgeActivator {
   private static XPrisonVirtualBridge activeBridge;

   private XPrisonBridgeActivator() {
   }

   public static void activate(XPrivateMines plugin, VirtualMineEngine engine) {
      XPrisonVirtualBridge bridge = new XPrisonVirtualBridge(plugin, engine);
      VirtualBlockProviders.register(bridge);
      engine.activateBridge(bridge);
      activeBridge = bridge;
      PrivateMinesLogger.info("Packet-mines: X-Prison virtual-blocks bridge active (autosell, enchants, bombs and quests run on virtual blocks).");
   }

   public static void deactivate() {
      if (activeBridge != null) {
         VirtualBlockProviders.unregister(activeBridge);
         activeBridge = null;
      }
   }
}
