package dev.drawethree.xprivatemines.service;

import org.bukkit.entity.Player;

public class PlayerTeleportService {
   public void teleportToSpawn(Player p) {
      p.performCommand("spawn");
   }
}
