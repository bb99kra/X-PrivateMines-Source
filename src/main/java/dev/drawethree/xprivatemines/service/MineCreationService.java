package dev.drawethree.xprivatemines.service;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.PrivateMineCreateEvent;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.registry.PrivateMineRegistry;
import dev.drawethree.xprivatemines.registry.SchematicRegistry;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import me.lucko.helper.Events;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MineCreationService {
   private final XPrivateMines plugin;
   private final PrivateMineRegistry registry;
   private final SchematicRegistry schematicRegistry;
   private final SchematicService schematicService;
   private final MineRefillService resetService;
   private final RegionService regionService;

   public MineCreationService(
      XPrivateMines plugin,
      PrivateMineRegistry registry,
      SchematicRegistry schematicRegistry,
      SchematicService schematicService,
      MineRefillService resetService,
      RegionService regionService
   ) {
      this.plugin = plugin;
      this.registry = registry;
      this.schematicRegistry = schematicRegistry;
      this.schematicService = schematicService;
      this.resetService = resetService;
      this.regionService = regionService;
   }

   public CompletableFuture<PrivateMineImpl> createPrivateMine(OfflinePlayer owner, MinesSchematic schematic) {
      if (owner == null) {
         return this.createAndReturnNewMine(null, schematic);
      } else {
         Optional<PrivateMineImpl> availableMine = this.registry.getAvailableMine(schematic);
         if (availableMine.isPresent()) {
            PrivateMineImpl mine = availableMine.get();
            this.resetService.refill(mine);
            this.registry.assignMineToOwner(mine, owner);
            if (owner.isOnline()) {
               Player player = owner.getPlayer();
               TitleMessage titleMessage = this.plugin.getMessageConfig().getTitle("mine-created");
               PlayerUtils.sendTitle(player, titleMessage);
               PlayerUtils.sendMessage(player, this.plugin.getMessageConfig().getMessage("mine-created"));
               if (this.plugin.getPrivateMinesConfig().isTeleportOnCreate()) {
                  mine.teleport(player);
               }
            }

            return CompletableFuture.completedFuture(mine);
         } else {
            return this.createAndReturnNewMine(owner, schematic);
         }
      }
   }

   private CompletableFuture<PrivateMineImpl> createAndReturnNewMine(OfflinePlayer owner, MinesSchematic schematic) {
      if (schematic == null) {
         schematic = this.schematicRegistry.getDefault();
      }

      CompletableFuture<PrivateMineImpl> future = new CompletableFuture<>();
      MinesSchematic finalSchematic = schematic;
      PrivateMinesLogger.info("Creating a new private mine");
      this.schematicService
         .pasteMinesSchematic(schematic)
         .whenComplete(
            (pastedAt, throwable) -> {
               if (throwable != null) {
                  if (owner != null && owner.isOnline()) {
                     Player player = owner.getPlayer();
                     if (player != null) {
                        player.sendMessage(TextUtils.applyColor("&cSomething went wrong. Please contact admin."));
                     }
                  }

                  PrivateMinesLogger.warning("Unable to paste new mines schematic for " + (owner != null ? owner.getName() : "null"));
                  throwable.printStackTrace();
                  future.completeExceptionally(throwable);
               } else {
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           try {
                              PrivateMineImpl mine = new PrivateMineImpl.Builder()
                                 .setOwner(owner)
                                 .setSchematic(finalSchematic)
                                 .setX(pastedAt.getBlockX())
                                 .setZ(pastedAt.getBlockZ())
                                 .setTax(this.plugin.getPrivateMinesConfig().getDefaultTax())
                                 .setEntryFee(this.plugin.getPrivateMinesConfig().getDefaultEntryFee())
                                 .setResetPercentage(this.plugin.getPrivateMinesConfig().getDefaultResetPercentage())
                                 .setTier(this.plugin.getMineTierManager().getDefaultTier())
                                 .build();
                              this.regionService.createRegions(mine);
                              this.resetService.refill(mine);
                              this.registry.registerMine(mine);
                              this.registry.indexMine(mine);
                              if (owner != null && owner.isOnline()) {
                                 Player playerx = owner.getPlayer();
                                 if (playerx != null) {
                                    TitleMessage titleMessage = this.plugin.getMessageConfig().getTitle("mine-created");
                                    PlayerUtils.sendTitle(playerx, titleMessage);
                                    PlayerUtils.sendMessage(playerx, this.plugin.getMessageConfig().getMessage("mine-created"));
                                    if (this.plugin.getPrivateMinesConfig().isTeleportOnCreate()) {
                                       mine.teleport(playerx);
                                    }
                                 }
                              }

                              PrivateMineCreateEvent event = new PrivateMineCreateEvent(mine);
                              Events.callSync(event);
                              XPrivateMines.getInstance().debug("Called PrivateMineCreateEvent event");
                              future.complete(mine);
                           } catch (Exception var8) {
                              var8.printStackTrace();
                              future.completeExceptionally(var8);
                           }
                        }
                     );
               }
            }
         );
      return future;
   }
}
