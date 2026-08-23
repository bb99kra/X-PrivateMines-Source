package dev.drawethree.xprivatemines.mines.model.schematic;

import org.codemc.worldguardwrapper.flag.WrappedState;
import dev.drawethree.xprivatemines.api.model.SchematicSettings;
import dev.drawethree.xprivatemines.utils.Utils;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class SchematicSettingsImpl implements SchematicSettings {
   private String permission;
   private Point spawn;
   private Point resetLocation;
   private Position regionPos1;
   private Position regionPos2;
   private Position minesPos1;
   private Position minesPos2;
   private int maxExpand;
   private int mineSize;
   private double expandCost;
   private int regionPriority;
   private int mineRegionPriority;
   private Map<String, WrappedState> regionFlags;
   private Map<String, WrappedState> mineRegionFlags;
   private boolean bedrockWalls;

   public static SchematicSettings fromFile(FileConfiguration config, String schematicName) {
      String basePath = "schematic-settings." + schematicName;
      String permission = config.getString(basePath + ".permission");
      Point spawn = Utils.getPointFromConfig(config, basePath + ".spawn");
      Point reset = Utils.getPointFromConfig(config, basePath + ".reset-teleport");
      int maxUpgrade = config.getInt(basePath + ".max-expand");
      int mineSize = config.getInt(basePath + ".mine-size");
      double expandCost = config.getDouble(basePath + ".expand-cost");
      Position regionPos1 = Utils.getPositionFromConfig(config, basePath + ".region.pos1");
      Position regionPos2 = Utils.getPositionFromConfig(config, basePath + ".region.pos2");
      Position minesPos1 = Utils.getPositionFromConfig(config, basePath + ".mine.pos1");
      Position minesPos2 = Utils.getPositionFromConfig(config, basePath + ".mine.pos2");
      int regionPriority = config.getInt(basePath + ".region.priority", 1);
      int mineRegionPriority = config.getInt(basePath + ".mine.priority", 2);
      Map<String, WrappedState> regionFlags = getFlagsFromConfig(config, basePath + ".region.wg-flags");
      Map<String, WrappedState> mineFlags = getFlagsFromConfig(config, basePath + ".mine.wg-flags");
      boolean bedrockWalls = config.getBoolean(basePath + ".bedrock-walls", false);
      return builder()
         .permission(permission)
         .spawn(spawn)
         .resetLocation(reset)
         .regionPos1(regionPos1)
         .regionPos2(regionPos2)
         .minesPos1(minesPos1)
         .minesPos2(minesPos2)
         .maxExpand(maxUpgrade)
         .mineSize(mineSize)
         .expandCost(expandCost)
         .regionPriority(regionPriority)
         .mineRegionPriority(mineRegionPriority)
         .regionFlags(regionFlags)
         .mineRegionFlags(mineFlags)
         .bedrockWalls(bedrockWalls)
         .build();
   }

   private static Map<String, WrappedState> getFlagsFromConfig(FileConfiguration config, String path) {
      Map<String, WrappedState> flags = new HashMap<>();
      ConfigurationSection section = config.getConfigurationSection(path);
      if (section != null) {
         for (String key : section.getKeys(false)) {
            String value = section.getString(key);

            try {
               WrappedState state = WrappedState.valueOf(value.toUpperCase());
               flags.put(key, state);
            } catch (IllegalArgumentException var8) {
               PrivateMinesLogger.warning("Invalid flag state: " + value + " for key: " + key);
            }
         }
      }

      return flags;
   }

   @Generated
   SchematicSettingsImpl(
      String permission,
      Point spawn,
      Point resetLocation,
      Position regionPos1,
      Position regionPos2,
      Position minesPos1,
      Position minesPos2,
      int maxExpand,
      int mineSize,
      double expandCost,
      int regionPriority,
      int mineRegionPriority,
      Map<String, WrappedState> regionFlags,
      Map<String, WrappedState> mineRegionFlags,
      boolean bedrockWalls
   ) {
      this.permission = permission;
      this.spawn = spawn;
      this.resetLocation = resetLocation;
      this.regionPos1 = regionPos1;
      this.regionPos2 = regionPos2;
      this.minesPos1 = minesPos1;
      this.minesPos2 = minesPos2;
      this.maxExpand = maxExpand;
      this.mineSize = mineSize;
      this.expandCost = expandCost;
      this.regionPriority = regionPriority;
      this.mineRegionPriority = mineRegionPriority;
      this.regionFlags = regionFlags;
      this.mineRegionFlags = mineRegionFlags;
      this.bedrockWalls = bedrockWalls;
   }

   @Generated
   public static SchematicSettingsImpl.SchematicSettingsImplBuilder builder() {
      return new SchematicSettingsImpl.SchematicSettingsImplBuilder();
   }

   @Generated
   @Override
   public String getPermission() {
      return this.permission;
   }

   @Generated
   @Override
   public Point getSpawn() {
      return this.spawn;
   }

   @Generated
   @Override
   public Point getResetLocation() {
      return this.resetLocation;
   }

   @Generated
   @Override
   public Position getRegionPos1() {
      return this.regionPos1;
   }

   @Generated
   @Override
   public Position getRegionPos2() {
      return this.regionPos2;
   }

   @Generated
   @Override
   public Position getMinesPos1() {
      return this.minesPos1;
   }

   @Generated
   @Override
   public Position getMinesPos2() {
      return this.minesPos2;
   }

   @Generated
   @Override
   public int getMaxExpand() {
      return this.maxExpand;
   }

   @Generated
   @Override
   public int getMineSize() {
      return this.mineSize;
   }

   @Generated
   @Override
   public double getExpandCost() {
      return this.expandCost;
   }

   @Generated
   @Override
   public int getRegionPriority() {
      return this.regionPriority;
   }

   @Generated
   @Override
   public int getMineRegionPriority() {
      return this.mineRegionPriority;
   }

   @Generated
   @Override
   public Map<String, WrappedState> getRegionFlags() {
      return this.regionFlags;
   }

   @Generated
   @Override
   public Map<String, WrappedState> getMineRegionFlags() {
      return this.mineRegionFlags;
   }

   @Generated
   @Override
   public boolean isBedrockWalls() {
      return this.bedrockWalls;
   }

   @Generated
   public void setPermission(String permission) {
      this.permission = permission;
   }

   @Generated
   public void setSpawn(Point spawn) {
      this.spawn = spawn;
   }

   @Generated
   public void setResetLocation(Point resetLocation) {
      this.resetLocation = resetLocation;
   }

   @Generated
   public void setRegionPos1(Position regionPos1) {
      this.regionPos1 = regionPos1;
   }

   @Generated
   public void setRegionPos2(Position regionPos2) {
      this.regionPos2 = regionPos2;
   }

   @Generated
   public void setMinesPos1(Position minesPos1) {
      this.minesPos1 = minesPos1;
   }

   @Generated
   public void setMinesPos2(Position minesPos2) {
      this.minesPos2 = minesPos2;
   }

   @Generated
   public void setMaxExpand(int maxExpand) {
      this.maxExpand = maxExpand;
   }

   @Generated
   public void setMineSize(int mineSize) {
      this.mineSize = mineSize;
   }

   @Generated
   public void setExpandCost(double expandCost) {
      this.expandCost = expandCost;
   }

   @Generated
   public void setRegionPriority(int regionPriority) {
      this.regionPriority = regionPriority;
   }

   @Generated
   public void setMineRegionPriority(int mineRegionPriority) {
      this.mineRegionPriority = mineRegionPriority;
   }

   @Generated
   public void setRegionFlags(Map<String, WrappedState> regionFlags) {
      this.regionFlags = regionFlags;
   }

   @Generated
   public void setMineRegionFlags(Map<String, WrappedState> mineRegionFlags) {
      this.mineRegionFlags = mineRegionFlags;
   }

   @Generated
   public void setBedrockWalls(boolean bedrockWalls) {
      this.bedrockWalls = bedrockWalls;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SchematicSettingsImpl other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getMaxExpand() != other.getMaxExpand()) {
         return false;
      } else if (this.getMineSize() != other.getMineSize()) {
         return false;
      } else if (Double.compare(this.getExpandCost(), other.getExpandCost()) != 0) {
         return false;
      } else if (this.getRegionPriority() != other.getRegionPriority()) {
         return false;
      } else if (this.getMineRegionPriority() != other.getMineRegionPriority()) {
         return false;
      } else if (this.isBedrockWalls() != other.isBedrockWalls()) {
         return false;
      } else {
         Object this$permission = this.getPermission();
         Object other$permission = other.getPermission();
         if (this$permission == null ? other$permission == null : this$permission.equals(other$permission)) {
            Object this$spawn = this.getSpawn();
            Object other$spawn = other.getSpawn();
            if (this$spawn == null ? other$spawn == null : this$spawn.equals(other$spawn)) {
               Object this$resetLocation = this.getResetLocation();
               Object other$resetLocation = other.getResetLocation();
               if (this$resetLocation == null ? other$resetLocation == null : this$resetLocation.equals(other$resetLocation)) {
                  Object this$regionPos1 = this.getRegionPos1();
                  Object other$regionPos1 = other.getRegionPos1();
                  if (this$regionPos1 == null ? other$regionPos1 == null : this$regionPos1.equals(other$regionPos1)) {
                     Object this$regionPos2 = this.getRegionPos2();
                     Object other$regionPos2 = other.getRegionPos2();
                     if (this$regionPos2 == null ? other$regionPos2 == null : this$regionPos2.equals(other$regionPos2)) {
                        Object this$minesPos1 = this.getMinesPos1();
                        Object other$minesPos1 = other.getMinesPos1();
                        if (this$minesPos1 == null ? other$minesPos1 == null : this$minesPos1.equals(other$minesPos1)) {
                           Object this$minesPos2 = this.getMinesPos2();
                           Object other$minesPos2 = other.getMinesPos2();
                           if (this$minesPos2 == null ? other$minesPos2 == null : this$minesPos2.equals(other$minesPos2)) {
                              Object this$regionFlags = this.getRegionFlags();
                              Object other$regionFlags = other.getRegionFlags();
                              if (this$regionFlags == null ? other$regionFlags == null : this$regionFlags.equals(other$regionFlags)) {
                                 Object this$mineRegionFlags = this.getMineRegionFlags();
                                 Object other$mineRegionFlags = other.getMineRegionFlags();
                                 return this$mineRegionFlags == null ? other$mineRegionFlags == null : this$mineRegionFlags.equals(other$mineRegionFlags);
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof SchematicSettingsImpl;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getMaxExpand();
      result = result * 59 + this.getMineSize();
      long $expandCost = Double.doubleToLongBits(this.getExpandCost());
      result = result * 59 + (int)($expandCost >>> 32 ^ $expandCost);
      result = result * 59 + this.getRegionPriority();
      result = result * 59 + this.getMineRegionPriority();
      result = result * 59 + (this.isBedrockWalls() ? 79 : 97);
      Object $permission = this.getPermission();
      result = result * 59 + ($permission == null ? 43 : $permission.hashCode());
      Object $spawn = this.getSpawn();
      result = result * 59 + ($spawn == null ? 43 : $spawn.hashCode());
      Object $resetLocation = this.getResetLocation();
      result = result * 59 + ($resetLocation == null ? 43 : $resetLocation.hashCode());
      Object $regionPos1 = this.getRegionPos1();
      result = result * 59 + ($regionPos1 == null ? 43 : $regionPos1.hashCode());
      Object $regionPos2 = this.getRegionPos2();
      result = result * 59 + ($regionPos2 == null ? 43 : $regionPos2.hashCode());
      Object $minesPos1 = this.getMinesPos1();
      result = result * 59 + ($minesPos1 == null ? 43 : $minesPos1.hashCode());
      Object $minesPos2 = this.getMinesPos2();
      result = result * 59 + ($minesPos2 == null ? 43 : $minesPos2.hashCode());
      Object $regionFlags = this.getRegionFlags();
      result = result * 59 + ($regionFlags == null ? 43 : $regionFlags.hashCode());
      Object $mineRegionFlags = this.getMineRegionFlags();
      return result * 59 + ($mineRegionFlags == null ? 43 : $mineRegionFlags.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "SchematicSettingsImpl(permission="
         + this.getPermission()
         + ", spawn="
         + this.getSpawn()
         + ", resetLocation="
         + this.getResetLocation()
         + ", regionPos1="
         + this.getRegionPos1()
         + ", regionPos2="
         + this.getRegionPos2()
         + ", minesPos1="
         + this.getMinesPos1()
         + ", minesPos2="
         + this.getMinesPos2()
         + ", maxExpand="
         + this.getMaxExpand()
         + ", mineSize="
         + this.getMineSize()
         + ", expandCost="
         + this.getExpandCost()
         + ", regionPriority="
         + this.getRegionPriority()
         + ", mineRegionPriority="
         + this.getMineRegionPriority()
         + ", regionFlags="
         + this.getRegionFlags()
         + ", mineRegionFlags="
         + this.getMineRegionFlags()
         + ", bedrockWalls="
         + this.isBedrockWalls()
         + ")";
   }

   @Generated
   public static class SchematicSettingsImplBuilder {
      @Generated
      private String permission;
      @Generated
      private Point spawn;
      @Generated
      private Point resetLocation;
      @Generated
      private Position regionPos1;
      @Generated
      private Position regionPos2;
      @Generated
      private Position minesPos1;
      @Generated
      private Position minesPos2;
      @Generated
      private int maxExpand;
      @Generated
      private int mineSize;
      @Generated
      private double expandCost;
      @Generated
      private int regionPriority;
      @Generated
      private int mineRegionPriority;
      @Generated
      private Map<String, WrappedState> regionFlags;
      @Generated
      private Map<String, WrappedState> mineRegionFlags;
      @Generated
      private boolean bedrockWalls;

      @Generated
      SchematicSettingsImplBuilder() {
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder permission(String permission) {
         this.permission = permission;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder spawn(Point spawn) {
         this.spawn = spawn;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder resetLocation(Point resetLocation) {
         this.resetLocation = resetLocation;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder regionPos1(Position regionPos1) {
         this.regionPos1 = regionPos1;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder regionPos2(Position regionPos2) {
         this.regionPos2 = regionPos2;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder minesPos1(Position minesPos1) {
         this.minesPos1 = minesPos1;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder minesPos2(Position minesPos2) {
         this.minesPos2 = minesPos2;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder maxExpand(int maxExpand) {
         this.maxExpand = maxExpand;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder mineSize(int mineSize) {
         this.mineSize = mineSize;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder expandCost(double expandCost) {
         this.expandCost = expandCost;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder regionPriority(int regionPriority) {
         this.regionPriority = regionPriority;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder mineRegionPriority(int mineRegionPriority) {
         this.mineRegionPriority = mineRegionPriority;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder regionFlags(Map<String, WrappedState> regionFlags) {
         this.regionFlags = regionFlags;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder mineRegionFlags(Map<String, WrappedState> mineRegionFlags) {
         this.mineRegionFlags = mineRegionFlags;
         return this;
      }

      @Generated
      public SchematicSettingsImpl.SchematicSettingsImplBuilder bedrockWalls(boolean bedrockWalls) {
         this.bedrockWalls = bedrockWalls;
         return this;
      }

      @Generated
      public SchematicSettingsImpl build() {
         return new SchematicSettingsImpl(
            this.permission,
            this.spawn,
            this.resetLocation,
            this.regionPos1,
            this.regionPos2,
            this.minesPos1,
            this.minesPos2,
            this.maxExpand,
            this.mineSize,
            this.expandCost,
            this.regionPriority,
            this.mineRegionPriority,
            this.regionFlags,
            this.mineRegionFlags,
            this.bedrockWalls
         );
      }

      @Generated
      @Override
      public String toString() {
         return "SchematicSettingsImpl.SchematicSettingsImplBuilder(permission="
            + this.permission
            + ", spawn="
            + this.spawn
            + ", resetLocation="
            + this.resetLocation
            + ", regionPos1="
            + this.regionPos1
            + ", regionPos2="
            + this.regionPos2
            + ", minesPos1="
            + this.minesPos1
            + ", minesPos2="
            + this.minesPos2
            + ", maxExpand="
            + this.maxExpand
            + ", mineSize="
            + this.mineSize
            + ", expandCost="
            + this.expandCost
            + ", regionPriority="
            + this.regionPriority
            + ", mineRegionPriority="
            + this.mineRegionPriority
            + ", regionFlags="
            + this.regionFlags
            + ", mineRegionFlags="
            + this.mineRegionFlags
            + ", bedrockWalls="
            + this.bedrockWalls
            + ")";
      }
   }
}
