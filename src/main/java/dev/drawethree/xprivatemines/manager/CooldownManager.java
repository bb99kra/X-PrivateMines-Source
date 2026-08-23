package dev.drawethree.xprivatemines.manager;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.cooldown.CooldownType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.entity.Player;

public enum CooldownManager {
   INSTANCE;

   private Map<CooldownType, CooldownMap<Player>> cooldowns;

   private CooldownManager() {
      this.loadCooldowns();
   }

   private void loadCooldowns() {
      this.cooldowns = new HashMap<>();

      for (CooldownType type : CooldownType.values()) {
         int cooldown = XPrivateMines.getInstance().getPrivateMinesConfig().getYamlConfig().getInt(type.getConfigPath());
         this.cooldowns.put(type, CooldownMap.create(Cooldown.of(cooldown, TimeUnit.SECONDS)));
      }
   }

   public void reload() {
      this.loadCooldowns();
   }

   public boolean hasCooldown(CooldownType type, Player sender) {
      return !this.cooldowns.get(type).test(sender);
   }

   public long getRemainingTime(CooldownType type, Player sender) {
      return this.cooldowns.get(type).get(sender).remainingTime(TimeUnit.SECONDS);
   }
}
