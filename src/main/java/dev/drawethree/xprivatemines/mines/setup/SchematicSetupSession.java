package dev.drawethree.xprivatemines.mines.setup;

import org.codemc.worldguardwrapper.flag.WrappedState;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.World;

public class SchematicSetupSession {
   private final UUID playerId;
   private Location pos1;
   private Location pos2;
   private Location buildPos1;
   private Location buildPos2;
   private Location minePos1;
   private Location minePos2;
   private Location regionPos1;
   private Location regionPos2;
   private Location spawn;
   private Location reset;
   private final Map<String, WrappedState> mineFlags = new LinkedHashMap<>();
   private final Map<String, WrappedState> regionFlags = new LinkedHashMap<>();

   public SchematicSetupSession(UUID playerId) {
      this.playerId = playerId;
   }

   public void setPos1(Location pos1) {
      this.pos1 = pos1.clone();
   }

   public void setPos2(Location pos2) {
      this.pos2 = pos2.clone();
   }

   public boolean hasActiveSelection() {
      return this.pos1 != null && this.pos2 != null;
   }

   public boolean captureBuild() {
      if (!this.hasActiveSelection()) {
         return false;
      } else {
         this.buildPos1 = this.pos1.clone();
         this.buildPos2 = this.pos2.clone();
         return true;
      }
   }

   public boolean captureMine() {
      if (!this.hasActiveSelection()) {
         return false;
      } else {
         this.minePos1 = this.pos1.clone();
         this.minePos2 = this.pos2.clone();
         return true;
      }
   }

   public boolean captureRegion() {
      if (!this.hasActiveSelection()) {
         return false;
      } else {
         this.regionPos1 = this.pos1.clone();
         this.regionPos2 = this.pos2.clone();
         return true;
      }
   }

   public void captureSpawn(Location location) {
      this.spawn = location.clone();
   }

   public void captureReset(Location location) {
      this.reset = location.clone();
   }

   public Location effectiveReset() {
      return this.reset != null ? this.reset : this.spawn;
   }

   public List<SetupStep> missingRequiredSteps() {
      List<SetupStep> missing = new ArrayList<>();

      for (SetupStep step : SetupStep.values()) {
         if (step.isRequired() && !step.isComplete(this)) {
            missing.add(step);
         }
      }

      return missing;
   }

   public boolean isReadyToCreate() {
      return this.missingRequiredSteps().isEmpty();
   }

   public World getPrimaryWorld() {
      return this.buildPos1 != null ? this.buildPos1.getWorld() : null;
   }

   public boolean allCapturedPointsShareWorld() {
      World world = null;

      for (Location location : new Location[]{
         this.buildPos1, this.buildPos2, this.minePos1, this.minePos2, this.regionPos1, this.regionPos2, this.spawn, this.reset
      }) {
         if (location != null && location.getWorld() != null) {
            if (world == null) {
               world = location.getWorld();
            } else if (!world.equals(location.getWorld())) {
               return false;
            }
         }
      }

      return true;
   }

   public Set<SetupStep> completedSteps() {
      Set<SetupStep> completed = EnumSet.noneOf(SetupStep.class);

      for (SetupStep step : SetupStep.values()) {
         if (step.isComplete(this)) {
            completed.add(step);
         }
      }

      return completed;
   }

   @Generated
   public UUID getPlayerId() {
      return this.playerId;
   }

   @Generated
   public Location getPos1() {
      return this.pos1;
   }

   @Generated
   public Location getPos2() {
      return this.pos2;
   }

   @Generated
   public Location getBuildPos1() {
      return this.buildPos1;
   }

   @Generated
   public Location getBuildPos2() {
      return this.buildPos2;
   }

   @Generated
   public Location getMinePos1() {
      return this.minePos1;
   }

   @Generated
   public Location getMinePos2() {
      return this.minePos2;
   }

   @Generated
   public Location getRegionPos1() {
      return this.regionPos1;
   }

   @Generated
   public Location getRegionPos2() {
      return this.regionPos2;
   }

   @Generated
   public Location getSpawn() {
      return this.spawn;
   }

   @Generated
   public Location getReset() {
      return this.reset;
   }

   @Generated
   public Map<String, WrappedState> getMineFlags() {
      return this.mineFlags;
   }

   @Generated
   public Map<String, WrappedState> getRegionFlags() {
      return this.regionFlags;
   }
}
