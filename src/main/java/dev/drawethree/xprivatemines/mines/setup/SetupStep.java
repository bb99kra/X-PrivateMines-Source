package dev.drawethree.xprivatemines.mines.setup;

import java.util.function.Predicate;
import lombok.Generated;

public enum SetupStep {
   BUILD("Build region", true, s -> s.getBuildPos1() != null && s.getBuildPos2() != null),
   MINE("Mine region", true, s -> s.getMinePos1() != null && s.getMinePos2() != null),
   REGION("Protection region", true, s -> s.getRegionPos1() != null && s.getRegionPos2() != null),
   SPAWN("Spawn point", true, s -> s.getSpawn() != null),
   RESET("Reset point", false, s -> s.getReset() != null);

   private final String displayName;
   private final boolean required;
   private final Predicate<SchematicSetupSession> completePredicate;

   private SetupStep(String displayName, boolean required, Predicate<SchematicSetupSession> completePredicate) {
      this.displayName = displayName;
      this.required = required;
      this.completePredicate = completePredicate;
   }

   public boolean isComplete(SchematicSetupSession session) {
      return this.completePredicate.test(session);
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public boolean isRequired() {
      return this.required;
   }

   @Generated
   public Predicate<SchematicSetupSession> getCompletePredicate() {
      return this.completePredicate;
   }
}
