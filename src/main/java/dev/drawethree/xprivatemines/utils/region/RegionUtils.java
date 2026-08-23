package dev.drawethree.xprivatemines.utils.region;

import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import dev.drawethree.xprivatemines.XPrivateMines;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.World;

public class RegionUtils {
   public static IWrappedRegion createCuboidRegion(String id, Location min, Location max) {
      Optional<IWrappedRegion> regionOpt = XPrivateMines.getInstance().getWorldGuardWrapper().addCuboidRegion(id, min, max);
      return regionOpt.orElseThrow(() -> new IllegalStateException("Failed to create region: " + id));
   }

   public static void applyFlagsToRegion(IWrappedRegion region, Map<String, WrappedState> flags) {
      resetAllFlags(region);

      for (Entry<String, WrappedState> entry : flags.entrySet()) {
         String key = entry.getKey();
         WrappedState value = entry.getValue();
         Optional<IWrappedFlag<WrappedState>> flagOpt = WorldGuardWrapper.getInstance().getFlag(key, WrappedState.class);
         flagOpt.ifPresent(flag -> region.setFlag(flag, value));
      }
   }

   public static void resetAllFlags(IWrappedRegion region) {
      Map<IWrappedFlag<?>, Object> currentFlags = region.getFlags();

      for (IWrappedFlag<?> flag : currentFlags.keySet()) {
         region.setFlag(flag, null);
      }
   }

   public static boolean ensureStateFlag(IWrappedRegion region, String flagName, WrappedState state) {
      Optional<IWrappedFlag<WrappedState>> flagOpt = WorldGuardWrapper.getInstance().getFlag(flagName, WrappedState.class);
      if (flagOpt.isEmpty()) {
         return false;
      } else {
         IWrappedFlag<WrappedState> flag = flagOpt.get();
         Optional<WrappedState> current = region.getFlag(flag);
         if (current.isPresent() && current.get() == state) {
            return false;
         } else {
            region.setFlag(flag, state);
            return true;
         }
      }
   }

   public static long getTotalBlockCountInRegion(IWrappedRegion region) {
      ICuboidSelection selection = (ICuboidSelection)region.getSelection();
      Location min = selection.getMinimumPoint();
      Location max = selection.getMaximumPoint();
      int xSize = max.getBlockX() - min.getBlockX() + 1;
      int ySize = max.getBlockY() - min.getBlockY() + 1;
      int zSize = max.getBlockZ() - min.getBlockZ() + 1;
      return (long)xSize * ySize * zSize;
   }

   public static int getNonAirBlockCount(IWrappedRegion region) {
      ICuboidSelection selection = (ICuboidSelection)region.getSelection();
      int count = 0;
      Location min = selection.getMinimumPoint();
      Location max = selection.getMaximumPoint();
      World world = min.getWorld();

      for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
         for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
               if (!world.getBlockAt(x, y, z).getType().isAir()) {
                  count++;
               }
            }
         }
      }

      return count;
   }
}
