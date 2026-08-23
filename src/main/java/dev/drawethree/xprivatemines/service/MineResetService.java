package dev.drawethree.xprivatemines.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMinePostResetEvent;
import dev.drawethree.xprivatemines.api.events.PrivateMinePreResetEvent;
import dev.drawethree.xprivatemines.config.MessageConfig;
import dev.drawethree.xprivatemines.config.PrivateMinesConfig;
import dev.drawethree.xprivatemines.manager.MineTierManagerImpl;
import dev.drawethree.xprivatemines.mines.model.MineImpl;
import dev.drawethree.xprivatemines.mines.model.MineTierImpl;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.mines.model.schematic.SchematicSettingsImpl;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.Map.Entry;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class MineResetService implements MineRefillService {
   private final PrivateMinesConfig config;
   private final MineTierManagerImpl mineTierManagerImpl;
   private final MessageConfig messageConfig;

   public MineResetService(XPrivateMines plugin) {
      this.config = plugin.getPrivateMinesConfig();
      this.messageConfig = plugin.getMessageConfig();
      this.mineTierManagerImpl = plugin.getMineTierManager();
   }

   public void resetToDefaultSettings(PrivateMineImpl mine) {
      mine.setOwner(null);
      mine.getBannedPlayersUUID().clear();
      mine.setTax(this.config.getDefaultTax());
      mine.setEntryFee(this.config.getDefaultEntryFee());
      mine.setResetPercentage(this.config.getDefaultResetPercentage());
      mine.setTier(this.mineTierManagerImpl.getDefaultTier());
      mine.setUnclaimedMoney(0.0);
      mine.setExpandLevel(0);
      mine.getMineImpl().setSelectedBlock(null);
   }

   @Override
   public void refill(PrivateMineImpl mine) {
      this.refill(mine, null);
   }

   @Override
   public void refill(PrivateMineImpl mine, CommandSender sender) {
      Schedulers.sync().run(() -> mine.getPlayersInMine().forEach(player -> mine.teleportToReset(player)));
      Schedulers.async().run(() -> {
         PrivateMinePreResetEvent event = new PrivateMinePreResetEvent(mine);
         Events.callSync(event);
         XPrivateMines.getInstance().debug("Called PrivateMinePreResetEvent event");
         if (event.isCancelled()) {
            XPrivateMines.getInstance().debug("PrivateMinePreResetEvent was cancelled");
         } else {
            try {
               World bukkitWorld = this.config.getMinesWorld();
               com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
               ICuboidSelection selection = (ICuboidSelection)mine.getMineImpl().getRegion().getSelection();
               Location min = selection.getMinimumPoint();
               Location max = selection.getMaximumPoint();
               EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build();

               try {
                  BlockVector3 mineMin = BlockVector3.at(min.getBlockX(), min.getBlockY(), min.getBlockZ());
                  BlockVector3 mineMax = BlockVector3.at(max.getBlockX(), max.getBlockY(), max.getBlockZ());
                  CuboidRegion region = new CuboidRegion(weWorld, mineMin, mineMax);
                  Pattern pattern;
                  if (mine.getMineImpl().getSelectedBlock() != null) {
                     pattern = BukkitAdapter.adapt(mine.getMineImpl().getSelectedBlock().toBlockData());
                  } else {
                     pattern = this.createPatternFromTier((MineTierImpl)mine.getTier());
                  }

                  if (mine.getSchematic().getSettings() instanceof SchematicSettingsImpl s && s.isBedrockWalls()) {
                     placeBedrockWalls(editSession, weWorld, mineMin, mineMax);
                  }

                  editSession.setBlocks(region, pattern);
               } catch (Throwable var17) {
                  if (editSession != null) {
                     try {
                        editSession.close();
                     } catch (Throwable var16) {
                        var17.addSuppressed(var16);
                     }
                  }

                  throw var17;
               }

               if (editSession != null) {
                  editSession.close();
               }

               if (sender != null) {
                  PlayerUtils.sendMessage(sender, this.messageConfig.getMessage("mine-refill"));
               }

               mine.getMineImpl().setEstimatedRemainingBlocks(mine.getMineImpl().getTotalBlockCount());
               PrivateMinePostResetEvent postResetEvent = new PrivateMinePostResetEvent(mine);
               Events.callSync(postResetEvent);
               XPrivateMines.getInstance().debug("Called PrivateMinePostResetEvent event");
            } catch (WorldEditException var18) {
               var18.printStackTrace();
            }
         }
      });
   }

   public static void placeBedrockWalls(EditSession editSession, com.sk89q.worldedit.world.World weWorld, BlockVector3 mineMin, BlockVector3 mineMax) throws WorldEditException {
      Pattern bedrock = BukkitAdapter.adapt(Material.BEDROCK.createBlockData());
      int minX = mineMin.getBlockX();
      int minY = mineMin.getBlockY();
      int minZ = mineMin.getBlockZ();
      int maxX = mineMax.getBlockX();
      int maxY = mineMax.getBlockY();
      int maxZ = mineMax.getBlockZ();
      editSession.setBlocks(new CuboidRegion(weWorld, BlockVector3.at(minX - 1, minY - 1, minZ - 1), BlockVector3.at(maxX + 1, minY - 1, maxZ + 1)), bedrock);
      editSession.setBlocks(new CuboidRegion(weWorld, BlockVector3.at(minX - 1, minY - 1, minZ - 1), BlockVector3.at(minX - 1, maxY, maxZ + 1)), bedrock);
      editSession.setBlocks(new CuboidRegion(weWorld, BlockVector3.at(maxX + 1, minY - 1, minZ - 1), BlockVector3.at(maxX + 1, maxY, maxZ + 1)), bedrock);
      editSession.setBlocks(new CuboidRegion(weWorld, BlockVector3.at(minX - 1, minY - 1, minZ - 1), BlockVector3.at(maxX + 1, maxY, minZ - 1)), bedrock);
      editSession.setBlocks(new CuboidRegion(weWorld, BlockVector3.at(minX - 1, minY - 1, maxZ + 1), BlockVector3.at(maxX + 1, maxY, maxZ + 1)), bedrock);
   }

   private Pattern createPatternFromTier(MineTierImpl tier) {
      RandomPattern pattern = new RandomPattern();

      for (Entry<MineBlock, Integer> entry : tier.getMineBlockWeights().entrySet()) {
         pattern.add(BukkitAdapter.adapt(entry.getKey().toBlockData()), entry.getValue().intValue());
      }

      return pattern;
   }

   @Override
   public boolean shouldReset(PrivateMineImpl mine) {
      MineImpl innerMineImpl = mine.getMineImpl();
      if (innerMineImpl == null) {
         return false;
      } else if (innerMineImpl.getTotalBlockCount() == 0L) {
         return false;
      } else {
         double remainingPercent = innerMineImpl.getEstimatedRemainingBlocks() * 100.0 / innerMineImpl.getTotalBlockCount();
         return remainingPercent <= mine.getResetPercentage();
      }
   }
}
