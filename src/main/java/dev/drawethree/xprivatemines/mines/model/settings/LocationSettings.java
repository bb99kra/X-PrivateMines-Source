package dev.drawethree.xprivatemines.mines.model.settings;

import lombok.Generated;
import org.bukkit.Location;

public class LocationSettings {
   private final long xOffset;
   private final long zOffset;
   private Location spawnLocation;
   private Location resetLocation;

   public LocationSettings(long xOffset, long zOffset, Location spawnLocation, Location resetLocation) {
      this.xOffset = xOffset;
      this.zOffset = zOffset;
      this.spawnLocation = spawnLocation;
      this.resetLocation = resetLocation;
   }

   @Generated
   public long getXOffset() {
      return this.xOffset;
   }

   @Generated
   public long getZOffset() {
      return this.zOffset;
   }

   @Generated
   public Location getSpawnLocation() {
      return this.spawnLocation;
   }

   @Generated
   public Location getResetLocation() {
      return this.resetLocation;
   }

   @Generated
   public void setSpawnLocation(Location spawnLocation) {
      this.spawnLocation = spawnLocation;
   }

   @Generated
   public void setResetLocation(Location resetLocation) {
      this.resetLocation = resetLocation;
   }
}
