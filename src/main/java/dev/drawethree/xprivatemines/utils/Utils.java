package dev.drawethree.xprivatemines.utils;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import me.lucko.helper.serialize.Direction;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class Utils {
   public static Point getPointFromConfig(FileConfiguration config, String path) {
      String input = config.getString(path);
      if (input != null && !input.isEmpty()) {
         String[] split = input.split(";");

         try {
            Position position = getPositionFromConfig(config, path);
            float yaw = 0.0F;
            float pitch = 0.0F;
            if (split.length >= 5) {
               yaw = Float.parseFloat(split[3]);
               pitch = Float.parseFloat(split[4]);
            } else {
               PrivateMinesLogger.warning("Warning: Direction data (yaw, pitch) missing in config for path '" + path + "'. Using default direction.");
            }

            Direction direction = Direction.of(yaw, pitch);
            return Point.of(position, direction);
         } catch (Exception var8) {
            var8.printStackTrace();
            return null;
         }
      } else {
         PrivateMinesLogger.warning("Config path '" + path + "' is null or empty.");
         return null;
      }
   }

   public static Position getPositionFromConfig(FileConfiguration config, String path) {
      String input = config.getString(path);
      if (input != null && !input.isEmpty()) {
         String[] split = input.split(";");
         if (split.length < 3) {
            PrivateMinesLogger.warning("Invalid position data at path '" + path + "'. Expected format: x;y;z[;yaw;pitch]");
            return null;
         } else {
            try {
               World world = XPrivateMines.getInstance().getPrivateMinesConfig().getMinesWorld();
               double x = Double.parseDouble(split[0]);
               double y = Double.parseDouble(split[1]);
               double z = Double.parseDouble(split[2]);
               return Position.of(x, y, z, world);
            } catch (NumberFormatException var11) {
               PrivateMinesLogger.warning("Number format error in position config at path '" + path + "': " + var11.getMessage());
               return null;
            } catch (Exception var12) {
               var12.printStackTrace();
               return null;
            }
         }
      } else {
         PrivateMinesLogger.warning("Config path '" + path + "' is null or empty.");
         return null;
      }
   }
}
