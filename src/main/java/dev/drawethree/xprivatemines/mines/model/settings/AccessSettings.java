package dev.drawethree.xprivatemines.mines.model.settings;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;

public class AccessSettings {
   private boolean open;
   private Set<UUID> bannedPlayers;

   public AccessSettings() {
      this.open = false;
      this.bannedPlayers = new HashSet<>();
   }

   public AccessSettings(boolean open, Set<UUID> bannedPlayers) {
      this.open = open;
      this.bannedPlayers = bannedPlayers;
   }

   @Generated
   public boolean isOpen() {
      return this.open;
   }

   @Generated
   public Set<UUID> getBannedPlayers() {
      return this.bannedPlayers;
   }

   @Generated
   public void setOpen(boolean open) {
      this.open = open;
   }

   @Generated
   public void setBannedPlayers(Set<UUID> bannedPlayers) {
      this.bannedPlayers = bannedPlayers;
   }
}
