package dev.drawethree.xprivatemines.api.model;

import org.codemc.worldguardwrapper.flag.WrappedState;
import java.util.Map;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;

public interface SchematicSettings {
   String getPermission();

   boolean isBedrockWalls();

   Point getSpawn();

   Point getResetLocation();

   Position getRegionPos1();

   Position getRegionPos2();

   Position getMinesPos1();

   Position getMinesPos2();

   int getMaxExpand();

   int getMineSize();

   double getExpandCost();

   int getRegionPriority();

   int getMineRegionPriority();

   Map<String, WrappedState> getRegionFlags();

   Map<String, WrappedState> getMineRegionFlags();
}
