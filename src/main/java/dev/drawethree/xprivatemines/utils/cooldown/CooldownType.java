package dev.drawethree.xprivatemines.utils.cooldown;

import lombok.Generated;

public enum CooldownType {
   MINE_RESET("player-cooldowns.mine-reset"),
   BLOCK_CHANGE("player-cooldowns.block-change");

   private final String configPath;

   private CooldownType(String configPath) {
      this.configPath = configPath;
   }

   @Generated
   public String getConfigPath() {
      return this.configPath;
   }
}
