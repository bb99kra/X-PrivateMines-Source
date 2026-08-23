package dev.drawethree.xprivatemines.virtual.bridge;

import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface BreakIntegration {
   BreakIntegration NONE = new BreakIntegration() {};

   default boolean isActive() {
      return false;
   }

   default AutoCloseable openBreakContext(Location location, MineBlock block, Player player) {
      return () -> {};
   }
}
