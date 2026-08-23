package dev.drawethree.xprivatemines.mines.setup;

import org.codemc.worldguardwrapper.flag.WrappedState;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.schematic.MinesSchematicImpl;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class SchematicSetupManager {
   private final XPrivateMines plugin;
   private final Map<UUID, SchematicSetupSession> sessions = new ConcurrentHashMap<>();

   public SchematicSetupManager(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public SchematicSetupSession getOrCreate(Player player) {
      return this.sessions.computeIfAbsent(player.getUniqueId(), id -> {
         SchematicSetupSession session = new SchematicSetupSession(id);
         this.seedFlags(session);
         return session;
      });
   }

   public SchematicSetupSession get(Player player) {
      return this.sessions.get(player.getUniqueId());
   }

   public void remove(Player player) {
      this.sessions.remove(player.getUniqueId());
   }

   public void clear() {
      this.sessions.clear();
   }

   private void seedFlags(SchematicSetupSession session) {
      MinesSchematicImpl defaultSchematic = this.plugin.getMinesManager().getDefaultSchematic();
      if (defaultSchematic != null && defaultSchematic.getSettings() != null) {
         Map<String, WrappedState> mine = defaultSchematic.getSettings().getMineRegionFlags();
         Map<String, WrappedState> region = defaultSchematic.getSettings().getRegionFlags();
         if (mine != null) {
            session.getMineFlags().putAll(mine);
         }

         if (region != null) {
            session.getRegionFlags().putAll(region);
         }
      } else {
         session.getMineFlags().put("block-break", WrappedState.ALLOW);
         session.getRegionFlags().put("build", WrappedState.DENY);
         session.getRegionFlags().put("block-break", WrappedState.DENY);
      }
   }
}
