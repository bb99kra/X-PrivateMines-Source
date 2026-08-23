package dev.drawethree.xprivatemines.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.codemc.worldguardwrapper.flag.WrappedState;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.config.SchematicSettingsConfig;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupSession;
import dev.drawethree.xprivatemines.mines.setup.SetupStep;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SchematicCreationService {
   private final XPrivateMines plugin;
   private static final String NAME_PATTERN = "[a-z0-9-]+";

   public SchematicCreationService(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public File getSchematicsDirectory() {
      File dir = new File(this.plugin.getDataFolder(), "schematics");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      return dir;
   }

   public void createFromSession(Player player, SchematicSetupSession session, String rawName) {
      if (session != null && session.isReadyToCreate()) {
         if (!session.allCapturedPointsShareWorld()) {
            PlayerUtils.sendMessage(player, "&cAll captured points must be in the same world. Re-capture them in one world.");
            SoundUtils.playError(player);
         } else {
            String name = rawName.trim().toLowerCase();
            if (!name.matches("[a-z0-9-]+")) {
               PlayerUtils.sendMessage(player, "&cInvalid name '" + rawName + "'. Use only letters, numbers and hyphens.");
               SoundUtils.playError(player);
            } else if (!this.plugin.getSchematicSettingsConfig().hasSchematic(name) && this.plugin.getMinesManager().getSchematicByName(name) == null) {
               PlayerUtils.sendMessage(player, "&7Creating schematic &f" + name + "&7...");
               this.create(
                  session,
                  name,
                  "xprivatemines.schematic." + name,
                  () -> {
                     PlayerUtils.sendMessage(player, "&a✔ Schematic &f" + name + " &acreated! Players can now use &f/pmine create " + name);
                     PlayerUtils.sendMessage(
                        player,
                        "&7Build size: &f"
                           + dimensions(session.getBuildPos1(), session.getBuildPos2())
                           + " &8— ensure &fspace-between-mines &8(config.yml) is at least the largest horizontal size."
                     );
                     SoundUtils.playSuccess(player);
                     this.plugin.getSchematicSetupManager().remove(player);
                  },
                  error -> {
                     PlayerUtils.sendMessage(player, "&c" + error);
                     SoundUtils.playError(player);
                  }
               );
            } else {
               PlayerUtils.sendMessage(player, "&cA schematic named '" + name + "' already exists. Pick another name.");
               SoundUtils.playError(player);
            }
         }
      } else {
         String missing = session == null
            ? "everything"
            : session.missingRequiredSteps().stream().map(SetupStep::getDisplayName).collect(Collectors.joining(", "));
         PlayerUtils.sendMessage(player, "&cComplete all required steps first: &f" + missing);
         SoundUtils.playError(player);
      }
   }

   private static String dimensions(Location p1, Location p2) {
      int x = Math.abs(p1.getBlockX() - p2.getBlockX()) + 1;
      int y = Math.abs(p1.getBlockY() - p2.getBlockY()) + 1;
      int z = Math.abs(p1.getBlockZ() - p2.getBlockZ()) + 1;
      return x + "×" + y + "×" + z;
   }

   public void create(SchematicSetupSession session, String name, String permission, Runnable onSuccess, Consumer<String> onError) {
      Location b1 = session.getBuildPos1();
      Location b2 = session.getBuildPos2();
      World world = b1.getWorld();
      BlockVector3 min = BlockVector3.at(
         Math.min(b1.getBlockX(), b2.getBlockX()), Math.min(b1.getBlockY(), b2.getBlockY()), Math.min(b1.getBlockZ(), b2.getBlockZ())
      );
      BlockVector3 max = BlockVector3.at(
         Math.max(b1.getBlockX(), b2.getBlockX()), Math.max(b1.getBlockY(), b2.getBlockY()), Math.max(b1.getBlockZ(), b2.getBlockZ())
      );
      File out = new File(this.getSchematicsDirectory(), name + ".schem");
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            this.saveSchematicFile(world, min, max, out);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               try {
                  this.writeSettings(session, name, permission, min);
                  this.plugin.getSchematicSettingsConfig().save();
                  this.plugin.getMinesManager().reloadSchematics();
                  onSuccess.run();
               } catch (Exception var8x) {
                  var8x.printStackTrace();
                  onError.accept("Failed to write schematic settings: " + var8x.getMessage());
               }
            });
         } catch (Exception var11x) {
            var11x.printStackTrace();
            Bukkit.getScheduler().runTask(this.plugin, () -> onError.accept("Failed to save schematic file: " + var11x.getMessage()));
         }
      });
   }

   private void saveSchematicFile(World bukkitWorld, BlockVector3 min, BlockVector3 max, File out) throws Exception {
      com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
      CuboidRegion region = new CuboidRegion(weWorld, min, max);
      BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
      clipboard.setOrigin(min);
      EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).maxBlocks(-1).build();

      try {
         ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
         copy.setCopyingEntities(false);
         Operations.complete(copy);
      } catch (Throwable var14) {
         if (editSession != null) {
            try {
               editSession.close();
            } catch (Throwable var12) {
               var14.addSuppressed(var12);
            }
         }

         throw var14;
      }

      if (editSession != null) {
         editSession.close();
      }

      ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(out));

      try {
         writer.write(clipboard);
      } catch (Throwable var13) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var11) {
               var13.addSuppressed(var11);
            }
         }

         throw var13;
      }

      if (writer != null) {
         writer.close();
      }
   }

   private void writeSettings(SchematicSetupSession session, String name, String permission, BlockVector3 buildMin) {
      SchematicSettingsConfig config = this.plugin.getSchematicSettingsConfig();
      String base = "schematic-settings." + name.toLowerCase();
      config.set(base + ".permission", permission);
      config.set(base + ".mine-size", computeMineSize(session));
      config.set(base + ".bedrock-walls", false);
      config.set(base + ".max-expand", 10);
      config.set(base + ".expand-cost", 1000000.0);
      config.set(base + ".spawn", pointString(buildMin, session.getSpawn()));
      config.set(base + ".reset-teleport", pointString(buildMin, session.effectiveReset()));
      config.set(base + ".mine.pos1", offsetString(buildMin, session.getMinePos1()));
      config.set(base + ".mine.pos2", offsetString(buildMin, session.getMinePos2()));
      config.set(base + ".mine.priority", 2);
      this.writeFlags(config, base + ".mine.wg-flags", session.getMineFlags());
      config.set(base + ".region.pos1", offsetString(buildMin, session.getRegionPos1()));
      config.set(base + ".region.pos2", offsetString(buildMin, session.getRegionPos2()));
      config.set(base + ".region.priority", 1);
      this.writeFlags(config, base + ".region.wg-flags", session.getRegionFlags());
   }

   private void writeFlags(SchematicSettingsConfig config, String path, Map<String, WrappedState> flags) {
      for (Entry<String, WrappedState> entry : flags.entrySet()) {
         config.set(path + "." + entry.getKey(), entry.getValue().name());
      }
   }

   public static int computeMineSize(SchematicSetupSession session) {
      Location p1 = session.getMinePos1();
      Location p2 = session.getMinePos2();
      int xSpan = Math.abs(p1.getBlockX() - p2.getBlockX());
      int zSpan = Math.abs(p1.getBlockZ() - p2.getBlockZ());
      return Math.max(xSpan, zSpan) + 1;
   }

   public static String offsetString(BlockVector3 buildMin, Location point) {
      int cx = point.getBlockX() - buildMin.getBlockX();
      int cy = 100 + (point.getBlockY() - buildMin.getBlockY());
      int cz = point.getBlockZ() - buildMin.getBlockZ();
      return cx + ";" + cy + ";" + cz;
   }

   public static String pointString(BlockVector3 buildMin, Location point) {
      double cx = round(point.getX() - buildMin.getBlockX());
      double cy = round(100.0 + (point.getY() - buildMin.getBlockY()));
      double cz = round(point.getZ() - buildMin.getBlockZ());
      return cx + ";" + cy + ";" + cz + ";" + point.getYaw() + ";" + point.getPitch();
   }

   private static double round(double value) {
      return Math.round(value * 100.0) / 100.0;
   }
}
