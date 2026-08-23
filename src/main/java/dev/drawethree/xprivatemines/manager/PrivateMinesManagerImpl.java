package dev.drawethree.xprivatemines.manager;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.manager.PrivateMinesManager;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.config.PrivateMinesConfig;
import dev.drawethree.xprivatemines.gui.confirmation.DeleteMineGui;
import dev.drawethree.xprivatemines.mines.loader.PrivateMineLoader;
import dev.drawethree.xprivatemines.mines.loader.SchematicLoader;
import dev.drawethree.xprivatemines.mines.loader.YamlPrivateMineLoader;
import dev.drawethree.xprivatemines.mines.model.MineTierImpl;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.mines.model.schematic.MinesSchematicImpl;
import dev.drawethree.xprivatemines.registry.PrivateMineRegistry;
import dev.drawethree.xprivatemines.registry.SchematicRegistry;
import dev.drawethree.xprivatemines.service.MineAccessService;
import dev.drawethree.xprivatemines.service.MineCreationService;
import dev.drawethree.xprivatemines.service.MineDeletionService;
import dev.drawethree.xprivatemines.service.MineExpandService;
import dev.drawethree.xprivatemines.service.MinePregenService;
import dev.drawethree.xprivatemines.service.MineRefillService;
import dev.drawethree.xprivatemines.service.MineResetService;
import dev.drawethree.xprivatemines.service.MineUpgradeService;
import dev.drawethree.xprivatemines.service.PacketMineResetService;
import dev.drawethree.xprivatemines.service.PlayerTeleportService;
import dev.drawethree.xprivatemines.service.RegionService;
import dev.drawethree.xprivatemines.service.SchematicService;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Generated;
import me.lucko.helper.menu.paginated.PaginatedGui;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public class PrivateMinesManagerImpl implements PrivateMinesManager {
   public static final File PRIVATE_MINES_DIRECTORY = new File(XPrivateMines.getInstance().getDataFolder() + "/mines");
   private final XPrivateMines plugin;
   private final PrivateMineLoader loader;
   private final SchematicLoader schematicLoader;
   private final PrivateMineRegistry mineRegistry;
   private final SchematicRegistry schematicRegistry;
   private final MineCreationService creationService;
   private final MineDeletionService deletionService;
   private final MineResetService resetService;
   private final MineRefillService refillService;
   private final MinePregenService pregenService;
   private final MineExpandService expandService;
   private final MineUpgradeService upgradeService;
   private final MineAccessService accessService;
   private final PlayerTeleportService teleportService;
   private final RegionService regionService;
   private final SchematicService schematicService;
   private volatile boolean minesReady = false;

   public PrivateMinesManagerImpl(XPrivateMines plugin) {
      this.plugin = plugin;
      this.teleportService = new PlayerTeleportService();
      this.accessService = new MineAccessService();
      this.mineRegistry = new PrivateMineRegistry();
      this.loader = new YamlPrivateMineLoader();
      this.regionService = new RegionService(plugin);
      this.schematicService = new SchematicService(plugin);
      this.schematicLoader = new SchematicLoader(plugin);
      this.schematicRegistry = new SchematicRegistry();
      this.resetService = new MineResetService(plugin);
      this.refillService = (MineRefillService)(plugin.isPacketMinesActive()
         ? new PacketMineResetService(plugin, plugin.getVirtualMineEngine(), this.resetService)
         : this.resetService);
      this.deletionService = new MineDeletionService(plugin, this.mineRegistry, this.teleportService, this.regionService, this.resetService, this.refillService);
      this.creationService = new MineCreationService(
         plugin, this.mineRegistry, this.schematicRegistry, this.schematicService, this.refillService, this.regionService
      );
      this.pregenService = new MinePregenService(plugin, this.creationService);
      this.upgradeService = new MineUpgradeService(plugin, this.refillService, this.regionService);
      this.expandService = new MineExpandService(plugin, this.refillService, this.regionService);
   }

   public void load() {
      this.createDirs();
      this.loadAllSchematics();
      this.loadAllPrivateMinesAsync();
   }

   public void reload() {
      this.schematicService.reload();
      this.reloadSchematics();
   }

   public void reloadSchematics() {
      this.schematicRegistry.clear();
      this.loadAllSchematics();
   }

   public MinesSchematicImpl getDefaultSchematic() {
      return this.schematicRegistry.getDefault();
   }

   public MinesSchematicImpl getSchematicByName(String name) {
      return this.schematicRegistry.get(name);
   }

   @Override
   public Collection<PrivateMine> getAll() {
      return new ArrayList<>(this.mineRegistry.getAll());
   }

   @Override
   public boolean isMinesReady() {
      return this.minesReady;
   }

   @Override
   public Collection<MinesSchematic> getAllSchematics() {
      return new ArrayList<>(this.schematicRegistry.getAll());
   }

   public Collection<PrivateMineImpl> getAllInternal() {
      return new ArrayList<>(this.mineRegistry.getAll());
   }

   @Override
   public PrivateMine getPrivateMine(OfflinePlayer player) {
      return this.mineRegistry.getMineByPlayer(player.getUniqueId());
   }

   @Override
   public PrivateMine getMineById(UUID mineUuid) {
      return this.mineRegistry.getMineById(mineUuid);
   }

   @Override
   public PrivateMine getMineByOwner(UUID ownerUuid) {
      return this.mineRegistry.getMineByPlayer(ownerUuid);
   }

   public PrivateMineImpl getPrivateMineInternal(OfflinePlayer player) {
      return this.mineRegistry.getMineByPlayer(player.getUniqueId());
   }

   @Override
   public PrivateMine getPrivateMineAtLocation(Location location) {
      return this.mineRegistry.getMineAtLocation(location);
   }

   @Override
   public CompletableFuture<PrivateMine> createPrivateMine(OfflinePlayer owner, MinesSchematic schematic) {
      return this.creationService.createPrivateMine(owner, schematic).thenApply(mine -> (PrivateMine)mine);
   }

   @Override
   public void deleteMine(CommandSender sender, PrivateMine mine) {
      if (mine instanceof PrivateMineImpl) {
         this.deletionService.deleteMine(sender, (PrivateMineImpl)mine);
      }
   }

   @Override
   public void refill(PrivateMine mine) {
      if (mine instanceof PrivateMineImpl) {
         this.refill((PrivateMineImpl)mine, null);
      }
   }

   public void refill(PrivateMineImpl mine, CommandSender sender) {
      this.refillService.refill(mine, sender);
   }

   private void createDirs() {
      File minesDir = new File(this.plugin.getDataFolder(), "mines");
      File schematicsDir = new File(this.plugin.getDataFolder(), "schematics");
      if (!minesDir.exists()) {
         minesDir.mkdirs();
      }

      if (!schematicsDir.exists()) {
         schematicsDir.mkdirs();
      }
   }

   private void loadAllSchematics() {
      long start = System.currentTimeMillis();
      List<MinesSchematicImpl> loaded = this.schematicLoader.loadSchematicsFromDirectory(new File(this.plugin.getDataFolder(), "schematics"));
      long end = System.currentTimeMillis();
      this.plugin.debug("Loaded " + loaded.size() + " schematics from directory in " + (end - start) + "ms");
      this.schematicRegistry.register(loaded);
   }

   private void loadAllPrivateMinesAsync() {
      this.plugin.debug("Loading all private mines...");
      this.mineRegistry.clear();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         long start = System.currentTimeMillis();
         File file = new File(this.plugin.getDataFolder(), "mines.yml");
         List<PrivateMineImpl> loaded = this.loader.loadMinesFromFile(file);
         long end = System.currentTimeMillis();
         this.plugin.debug("Loaded " + loaded.size() + " private mines in " + (end - start) + "ms");
         start = System.currentTimeMillis();
         this.plugin.debug("Registering all mines");
         this.mineRegistry.registerMines(loaded);
         end = System.currentTimeMillis();
         this.plugin.debug("Registered all mines in " + (end - start) + "ms");
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            this.plugin.debug("Applying flags to all mines");
            long startNew = System.currentTimeMillis();
            loaded.forEach(this.regionService::applyFlagsToMine);
            loaded.forEach(this.mineRegistry::indexMine);
            long endNew = System.currentTimeMillis();
            this.plugin.debug("Applied flags to all mines in " + (endNew - startNew) + "ms");
            this.minesReady = true;
            this.plugin.ifPacketEngine(engine -> {
               engine.onMinesLoaded(this.getAllInternal());
               if (this.refillService instanceof PacketMineResetService packetService) {
                  packetService.onMinesLoaded(this.getAllInternal(), this.regionService);
               }
            });
         });
      });
   }

   public MinesSchematic getSchematic(String name) {
      return this.schematicRegistry.get(name);
   }

   @Override
   public void banPlayer(PrivateMine mine, OfflinePlayer player) {
      if (mine instanceof PrivateMineImpl) {
         this.accessService.banPlayer((PrivateMineImpl)mine, player);
      }
   }

   @Override
   public void unbanPlayer(PrivateMine mine, OfflinePlayer player) {
      if (mine instanceof PrivateMineImpl) {
         this.accessService.unbanPlayer((PrivateMineImpl)mine, player);
      }
   }

   @Override
   public boolean isMaxExpand(PrivateMine mine) {
      return mine instanceof PrivateMineImpl && this.expandService.isMaxExpand((PrivateMineImpl)mine);
   }

   @Override
   public boolean isMaxTier(PrivateMine mine) {
      return mine instanceof PrivateMineImpl && this.upgradeService.isMaxTier((PrivateMineImpl)mine);
   }

   @Override
   public boolean expandMine(PrivateMine mine, Player player) {
      return mine instanceof PrivateMineImpl && this.expandService.expandMine((PrivateMineImpl)mine, player);
   }

   @Override
   public boolean upgradeMine(PrivateMine mine, Player player) {
      return mine instanceof PrivateMineImpl ? this.upgradeService.upgradeMine((PrivateMineImpl)mine, player) : false;
   }

   @Override
   public double getNextUpgradeCost(PrivateMine mine) {
      return mine instanceof PrivateMineImpl ? this.upgradeService.getNextUpgradeCost((PrivateMineImpl)mine) : 0.0;
   }

   @Override
   public boolean forceExpand(CommandSender sender, PrivateMine mine, int expandAmount) {
      return mine instanceof PrivateMineImpl && this.expandService.forceExpand(sender, (PrivateMineImpl)mine, expandAmount);
   }

   @Override
   public boolean forceUpgrade(CommandSender sender, PrivateMine mine, MineTier newTier) {
      return !(mine instanceof PrivateMineImpl) || newTier != null && !(newTier instanceof MineTierImpl)
         ? false
         : this.upgradeService.forceUpgrade(sender, (PrivateMineImpl)mine, (MineTierImpl)newTier);
   }

   public boolean forceSetBlock(PrivateMine mine, XMaterial material) {
      return this.setBlock(mine, material);
   }

   @Override
   public boolean setBlock(PrivateMine mine, XMaterial material) {
      return this.setBlock(mine, material == null ? null : MineBlock.of(material));
   }

   @Override
   public boolean setBlock(PrivateMine mine, String materialName) {
      MineBlock block = null;
      if (materialName != null && !materialName.isBlank()) {
         block = MineBlock.parse(materialName);
         if (block.isCustom() && !block.getHook().isCustomBlock(materialName)) {
            return false;
         }
      }

      return this.setBlock(mine, block);
   }

   private boolean setBlock(PrivateMine mine, MineBlock block) {
      if (mine instanceof PrivateMineImpl mineImpl) {
         mineImpl.getMineImpl().setSelectedBlock(block);
         this.refillService.refill(mineImpl);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldReset(PrivateMine mine) {
      return mine instanceof PrivateMineImpl && this.refillService.shouldReset((PrivateMineImpl)mine);
   }

   @Override
   public void kickPlayer(Player player) {
      this.accessService.kickPlayer(player);
   }

   @Override
   public void reassignMine(PrivateMine mine, OfflinePlayer newOwner) {
      this.mineRegistry.assignMineToOwner((PrivateMineImpl)mine, newOwner);
   }

   @Override
   public void pregen(CommandSender sender, MinesSchematic schematic, int amount) {
      this.pregenService.pregen(sender, schematic, amount);
   }

   @Override
   public boolean isPregenRunning() {
      return this.pregenService.isRunning();
   }

   @Override
   public void stopPregen() {
      this.pregenService.stopPregen();
   }

   @Override
   public int getPregenCompleted() {
      return this.pregenService.getCompleted();
   }

   @Override
   public int getPregenTotal() {
      return this.pregenService.getTotal();
   }

   public void saveLastXZ() {
      this.plugin.reloadConfig();
      this.plugin.getConfig().set("last-x", this.schematicService.getLastX());
      this.plugin.getConfig().set("last-z", this.schematicService.getLastZ());
      this.plugin.saveConfig();
   }

   public void showPrivateMineList(Player p) {
      PaginatedGui builder = PaginatedGuiBuilder.create()
         .lines(6)
         .title("&2&lPrivate Mines &7- &a&lVisit")
         .nextPageSlot(51)
         .previousPageSlot(47)
         .nextPageItem(pageInfo -> ItemStackBuilder.of(Material.ARROW).name("&aNext Page").build())
         .previousPageItem(pageInfo -> ItemStackBuilder.of(Material.ARROW).name("&cPrevious Page").build())
         .build(
            p,
            paginatedGui -> this.mineRegistry
               .getAll()
               .stream()
               .filter(privateMine -> privateMine.getOwner() != null)
               .filter(privateMine -> privateMine.isOpenTo(p))
               .map(
                  privateMine -> ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
                     .transformMeta(meta -> ((SkullMeta)meta).setOwningPlayer(privateMine.getOfflineOwner()))
                     .name("&e&l" + privateMine.getDisplayName())
                     .lore(
                        " ",
                        "&7* Miners: &c" + privateMine.getPlayersInPrivateMine().size(),
                        " ",
                        "&7* Entry Fee: &c" + this.plugin.getEconomyManager().format(p, privateMine.getEntryFee()),
                        "&7* Miner Tax: &c" + String.format("%,.2f%%", privateMine.getTax()),
                        " ",
                        "&e> Click to teleport"
                     )
                     .build(() -> {
                        if (privateMine.isBanned(p)) {
                           PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("you-banned"));
                           SoundUtils.playError(p);
                        } else if (!this.plugin.getEconomyManager().has(p, privateMine.getEntryFee()) && !privateMine.isOwner(p)) {
                           PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("no-money"));
                           SoundUtils.playError(p);
                        } else {
                           if (!privateMine.isOwner(p)) {
                              this.plugin.getEconomyManager().withdraw(p, privateMine.getEntryFee());
                              privateMine.setUnclaimedMoney(privateMine.getUnclaimedMoney() + privateMine.getEntryFee());
                           }

                           privateMine.teleport(p);
                           SoundUtils.playSuccess(p);
                        }
                     })
               )
               .collect(Collectors.toList())
         );
      builder.open();
   }

   public void showLeaderboard(Player p, String category) {
      PrivateMinesConfig.LeaderboardCategoryConfig cfg = this.plugin.getPrivateMinesConfig().getLeaderboardCategories().get(category);
      if (cfg != null) {
         List<MineTier> allTiers = this.plugin.getMineTierManager().getAllTiers();

         Comparator<PrivateMine> comparator = switch (category) {
            case "size" -> Comparator.comparingInt(PrivateMine::getMineSize).reversed();
            case "tax" -> Comparator.comparingDouble(PrivateMine::getTax).reversed();
            case "fee" -> Comparator.comparingDouble(PrivateMine::getEntryFee).reversed();
            default -> Comparator.<PrivateMine>comparingInt(m -> m.getTier() != null ? allTiers.indexOf(m.getTier()) : -1).reversed();
         };
         List<PrivateMine> sorted = this.mineRegistry.getAll().stream().filter(m -> m.getOwner() != null).sorted(comparator).collect(Collectors.toList());
         AtomicInteger rankCounter = new AtomicInteger(1);
         PaginatedGui builder = PaginatedGuiBuilder.create()
            .lines(6)
            .title(cfg.guiTitle())
            .nextPageSlot(51)
            .previousPageSlot(47)
            .nextPageItem(pageInfo -> ItemStackBuilder.of(Material.ARROW).name("&aNext Page").build())
            .previousPageItem(pageInfo -> ItemStackBuilder.of(Material.ARROW).name("&cPrevious Page").build())
            .build(
               p,
               paginatedGui -> sorted.stream()
                  .map(
                     mine -> {
                        int rank = rankCounter.getAndIncrement();
                        OfflinePlayer owner = mine.getOfflineOwner();
                        String ownerName = owner != null && owner.getName() != null ? owner.getName() : "Unknown";
                        String tierName = mine.getTier() != null ? mine.getTier().getName() : "N/A";
                        String itemName = this.applyLeaderboardPlaceholders(cfg.itemName(), rank, mine, ownerName, tierName, p);
                        List<String> lore = cfg.itemLore()
                           .stream()
                           .map(line -> this.applyLeaderboardPlaceholders(line, rank, mine, ownerName, tierName, p))
                           .collect(Collectors.toList());
                        return ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
                           .transformMeta(meta -> ((SkullMeta)meta).setOwningPlayer(owner))
                           .name(itemName)
                           .lore(lore)
                           .build(() -> {
                              if (mine.isBanned(p)) {
                                 PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("you-banned"));
                                 SoundUtils.playError(p);
                              } else if (!this.plugin.getEconomyManager().has(p, mine.getEntryFee()) && !mine.isOwner(p)) {
                                 PlayerUtils.sendMessage(p, this.plugin.getMessageConfig().getMessage("no-money"));
                                 SoundUtils.playError(p);
                              } else {
                                 if (!mine.isOwner(p)) {
                                    this.plugin.getEconomyManager().withdraw(p, mine.getEntryFee());
                                    mine.setUnclaimedMoney(mine.getUnclaimedMoney() + mine.getEntryFee());
                                 }

                                 mine.teleport(p);
                                 SoundUtils.playSuccess(p);
                              }
                           });
                     }
                  )
                  .collect(Collectors.toList())
            );
         builder.open();
      }
   }

   private String applyLeaderboardPlaceholders(String text, int rank, PrivateMine mine, String ownerName, String tierName, Player viewer) {
      return text.replace("%rank%", String.valueOf(rank))
         .replace("%mine_name%", mine.getDisplayName())
         .replace("%owner%", ownerName)
         .replace("%tier%", tierName)
         .replace("%size%", String.valueOf(mine.getMineSize()))
         .replace("%tax%", String.format("%,.2f", mine.getTax()))
         .replace("%fee%", this.plugin.getEconomyManager().format(viewer, mine.getEntryFee()));
   }

   public void showPrivateMineAdminList(Player p) {
      PaginatedGui builder = PaginatedGuiBuilder.create()
         .lines(6)
         .title("&2&lPrivate Mines &7┃ &aAdmin Panel")
         .nextPageSlot(51)
         .previousPageSlot(47)
         .nextPageItem(pageInfo -> ItemStackBuilder.of(Material.ARROW).name("&aNext Page").lore("&7Go to page &f" + (pageInfo.getCurrent() + 1)).build())
         .previousPageItem(
            pageInfo -> ItemStackBuilder.of(Material.ARROW).name("&cPrevious Page").lore("&7Go to page &f" + (pageInfo.getCurrent() - 1)).build()
         )
         .build(
            p,
            paginatedGui -> this.mineRegistry
               .getAll()
               .stream()
               .map(
                  mine -> {
                     OfflinePlayer owner = mine.getOfflineOwner();
                     String status = mine.isOpen() ? "&aOPEN" : "&cCLOSED";
                     String tier = mine.getTier() != null ? mine.getTier().getName() : "N/A";
                     return ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
                        .transformMeta(meta -> {
                           if (owner != null) {
                              ((SkullMeta)meta).setOwningPlayer(owner);
                           }
                        })
                        .name("&e&l" + (owner != null ? owner.getName() : "Unclaimed Mine"))
                        .lore(
                           "&7Private Mine Overview",
                           " ",
                           "&8┃ &7Status: " + status,
                           "&8┃ &7Tier: &b" + tier,
                           "&8┃ &7Expand Level: &d" + mine.getExpandLevel(),
                           " ",
                           "&8┃ &7Entry Fee: &6" + this.plugin.getEconomyManager().format(p, mine.getEntryFee()),
                           "&8┃ &7Tax: &c" + String.format("%,.2f%%", mine.getTax()),
                           "&8┃ &7Unclaimed Currency: &a" + this.plugin.getEconomyManager().format(p, mine.getUnclaimedMoney()),
                           " ",
                           "&8┃ &7Players Inside: &f" + mine.getPlayersInPrivateMine().size(),
                           " ",
                           "&c▶ Left-Click &7to teleport",
                           "&e▶ Right-Click &7to delete"
                        )
                        .build((Runnable)(() -> {
                           if (owner != null) {
                              DeleteMineGui gui = new DeleteMineGui(p, mine);
                              gui.setFallbackGui(player -> paginatedGui);
                              gui.open();
                           }
                        }), () -> {
                           mine.teleport(p);
                           SoundUtils.playSuccess(p);
                           paginatedGui.close();
                        });
                  }
               )
               .collect(Collectors.toList())
         );
      builder.open();
   }

   public List<String> getAllSchematicNames() {
      return this.schematicRegistry.getAll().stream().map(MinesSchematicImpl::getName).collect(Collectors.toList());
   }

   @Generated
   public MineAccessService getAccessService() {
      return this.accessService;
   }
}
