package dev.drawethree.xprivatemines.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import org.bukkit.Bukkit;

public class SchematicService {
   public static final int PASTE_Y = 100;
   private final XPrivateMines plugin;
   private int lastX;
   private int lastZ;

   public SchematicService(XPrivateMines plugin) {
      this.plugin = plugin;
      this.lastX = plugin.getConfig().getInt("last-x");
      this.lastZ = plugin.getConfig().getInt("last-z");
   }

   public CompletableFuture<BlockVector3> pasteMinesSchematic(MinesSchematic schem) {
      CompletableFuture<BlockVector3> future = new CompletableFuture<>();
      BlockVector3 pasteLoc = this.reserveNextMineLocation();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            World weWorld = BukkitAdapter.adapt(Bukkit.getWorld(this.plugin.getPrivateMinesConfig().getMinesWorldName()));
            PrivateMinesLogger.info("Pasting a new " + schem.getFile().getName() + " schematic");
            ClipboardFormat format = ClipboardFormats.findByFile(schem.getFile());
            ClipboardReader reader = format.getReader(new FileInputStream(schem.getFile()));

            try {
               Clipboard clipboard = reader.read();

               try {
                  EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).maxBlocks(-1).build();

                  try {
                     Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).ignoreAirBlocks(false).to(pasteLoc).build();
                     Operations.complete(operation);
                     PrivateMinesLogger.info("Pasted new " + schem.getFile().getName() + " at " + pasteLoc);
                     future.complete(pasteLoc);
                  } catch (Throwable var13) {
                     if (editSession != null) {
                        try {
                           editSession.close();
                        } catch (Throwable var12) {
                           var13.addSuppressed(var12);
                        }
                     }

                     throw var13;
                  }

                  if (editSession != null) {
                     editSession.close();
                  }
               } catch (WorldEditException var14) {
                  var14.printStackTrace();
                  future.completeExceptionally(var14);
               }
            } catch (Throwable var15) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var11) {
                     var15.addSuppressed(var11);
                  }
               }

               throw var15;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (IOException var16) {
            var16.printStackTrace();
            future.completeExceptionally(var16);
         }
      });
      return future;
   }

   public synchronized BlockVector3 calculateNewMinesVector() {
      int spaceBetweenMines = this.plugin.getPrivateMinesConfig().getSpaceBetweenMines();
      if (this.lastX >= spaceBetweenMines * 10) {
         this.lastX = 0;
         this.lastZ += spaceBetweenMines;
      }

      int x = this.lastX;
      int z = this.lastZ;
      this.lastX += spaceBetweenMines;
      return BlockVector3.at(x, 100, z);
   }

   public synchronized BlockVector3 reserveNextMineLocation() {
      int space = this.plugin.getPrivateMinesConfig().getSpaceBetweenMines();
      if (this.lastX >= space * 10) {
         this.lastX = 0;
         this.lastZ += space;
      }

      BlockVector3 loc = BlockVector3.at(this.lastX, 100, this.lastZ);
      this.lastX += space;
      this.plugin.getConfig().set("last-x", this.lastX);
      this.plugin.getConfig().set("last-z", this.lastZ);
      this.plugin.saveConfig();
      return loc;
   }

   public void reload() {
      this.lastX = this.plugin.getConfig().getInt("last-x");
      this.lastZ = this.plugin.getConfig().getInt("last-z");
   }

   @Generated
   public int getLastX() {
      return this.lastX;
   }

   @Generated
   public int getLastZ() {
      return this.lastZ;
   }
}
