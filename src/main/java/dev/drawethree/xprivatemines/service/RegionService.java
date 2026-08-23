package dev.drawethree.xprivatemines.service;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.region.RegionUtils;
import java.util.concurrent.CompletableFuture;
import me.lucko.helper.serialize.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class RegionService {
   private final XPrivateMines plugin;

   public RegionService(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void applyFlagsToMine(PrivateMineImpl mine) {
      RegionUtils.applyFlagsToRegion(mine.getRegion(), mine.getSchematic().getSettings().getRegionFlags());
      RegionUtils.applyFlagsToRegion(mine.getMineImpl().getRegion(), mine.getSchematic().getSettings().getMineRegionFlags());
   }

   public void recreateMineRegion(PrivateMineImpl mine) {
      this.deleteRegion(mine.getMineImpl().getRegion());
      mine.getMineImpl().setRegion(null);
      this.createPrivateMineInnerRegion(mine);
   }

   private void deleteRegion(IWrappedRegion region) {
      if (region != null) {
         this.plugin.getWorldGuardWrapper().removeRegion(this.plugin.getPrivateMinesConfig().getMinesWorld(), region.getId());
      }
   }

   public void createRegions(PrivateMineImpl mine) {
      this.createPrivateMineOuterRegion(mine);
      this.createPrivateMineInnerRegion(mine);
   }

   private void createPrivateMineOuterRegion(PrivateMineImpl mine) {
      if (mine.getRegion() != null) {
         RegionUtils.applyFlagsToRegion(mine.getRegion(), mine.getSchematic().getSettings().getRegionFlags());
      } else {
         Location min = this.getIslandRegionPos(mine, 1);
         Location max = this.getIslandRegionPos(mine, 2);
         IWrappedRegion region = RegionUtils.createCuboidRegion(mine.getUuid() + "_pmine", min, max);
         region.setPriority(mine.getSchematic().getSettings().getRegionPriority());
         RegionUtils.applyFlagsToRegion(region, mine.getSchematic().getSettings().getRegionFlags());
         mine.setRegion(region);
      }
   }

   private void createPrivateMineInnerRegion(PrivateMineImpl mine) {
      if (mine.getMineImpl().getRegion() != null) {
         RegionUtils.applyFlagsToRegion(mine.getRegion(), mine.getSchematic().getSettings().getMineRegionFlags());
      } else {
         int upgradeLevel = mine.getExpandLevel();
         Location pos1 = this.getMinesRegionPos(mine, 1);
         Location pos2 = this.getMinesRegionPos(mine, 2);
         World world = pos1.getWorld();
         int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
         int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
         int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
         int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
         int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
         int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
         Location min = new Location(world, minX, minY, minZ);
         Location max = new Location(world, maxX, maxY, maxZ);
         min = min.add(-upgradeLevel, 0.0, -upgradeLevel);
         max = max.add(upgradeLevel, 0.0, upgradeLevel);
         IWrappedRegion region = RegionUtils.createCuboidRegion(mine.getUuid().toString() + "_mine", min, max);
         region.setPriority(mine.getSchematic().getSettings().getMineRegionPriority());
         RegionUtils.applyFlagsToRegion(region, mine.getSchematic().getSettings().getMineRegionFlags());
         mine.getMineImpl().setRegion(region);
         mine.getMineImpl().setTotalBlockCount(RegionUtils.getTotalBlockCountInRegion(region));
         mine.getMineImpl().setEstimatedRemainingBlocks(mine.getMineImpl().getTotalBlockCount());
         XPrivateMines.getInstance().ifPacketEngine(engine -> engine.onRegionChanged(mine));
      }
   }

   public Location getMinesRegionPos(PrivateMineImpl mine, int pos) {
      Position position = pos == 1 ? mine.getSchematic().getSettings().getMinesPos1() : mine.getSchematic().getSettings().getMinesPos2();
      return position.add(mine.getLocationSettings().getXOffset(), 0.0, mine.getZOffset()).toLocation();
   }

   public Location getIslandRegionPos(PrivateMineImpl mine, int pos) {
      Position position = pos == 1 ? mine.getSchematic().getSettings().getRegionPos1() : mine.getSchematic().getSettings().getRegionPos2();
      return position.add(mine.getXOffset(), 0.0, mine.getZOffset()).toLocation();
   }

   public CompletableFuture<Void> clearRegionAsync(IWrappedRegion r) {
      return this.clearRegionAsync(r, false);
   }

   public CompletableFuture<Void> clearRegionAsync(IWrappedRegion r, boolean includeBedrockShell) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      ICuboidSelection selection = (ICuboidSelection)r.getSelection();
      World bukkitWorld = selection.getMinimumPoint().getWorld();
      Bukkit.getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               try {
                  com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
                  BlockVector3 min = BlockVector3.at(
                     selection.getMinimumPoint().getBlockX(), selection.getMinimumPoint().getBlockY(), selection.getMinimumPoint().getBlockZ()
                  );
                  BlockVector3 max = BlockVector3.at(
                     selection.getMaximumPoint().getBlockX(), selection.getMaximumPoint().getBlockY(), selection.getMaximumPoint().getBlockZ()
                  );
                  BlockVector3 clearMin = includeBedrockShell ? min.subtract(1, 1, 1) : min;
                  BlockVector3 clearMax = includeBedrockShell ? max.add(1, 0, 1) : max;
                  Region region = new CuboidRegion(weWorld, clearMin, clearMax);

                  try {
                     EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();

                     try {
                        BlockState air = BlockTypes.AIR.getDefaultState();
                        editSession.setBlocks(region, air);
                        future.complete(null);
                     } catch (Throwable var14) {
                        if (editSession != null) {
                           try {
                              editSession.close();
                           } catch (Throwable var13) {
                              var14.addSuppressed(var13);
                           }
                        }

                        throw var14;
                     }

                     if (editSession != null) {
                        editSession.close();
                     }
                  } catch (Exception var15) {
                     var15.printStackTrace();
                     future.completeExceptionally(var15);
                  }
               } catch (Exception var16) {
                  var16.printStackTrace();
                  future.completeExceptionally(var16);
               }
            }
         );
      return future;
   }

   public void fixLightning(PrivateMineImpl mine) {
      IWrappedRegion region = mine.getRegion();
      ICuboidSelection sel = (ICuboidSelection)region.getSelection();
      Location min = sel.getMinimumPoint();
      Location max = sel.getMaximumPoint();
      if (min != null && max != null && min.getWorld().equals(max.getWorld())) {
         com.sk89q.worldedit.world.World world = FaweAPI.getWorld(min.getWorld().getName());
         BlockVector3 minV = BlockVector3.at(min.getBlockX(), min.getBlockY(), min.getBlockZ());
         BlockVector3 maxV = BlockVector3.at(max.getBlockX(), max.getBlockY(), max.getBlockZ());
         CuboidRegion cuboidRegion = new CuboidRegion(minV, maxV);
         System.out.println(cuboidRegion);
         FaweAPI.fixLighting(world, cuboidRegion, null, RelightMode.ALL);
         System.out.println("Lighting fixed");
      }
   }
}
