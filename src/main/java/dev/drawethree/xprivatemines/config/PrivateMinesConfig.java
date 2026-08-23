package dev.drawethree.xprivatemines.config;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class PrivateMinesConfig {
   private final XPrivateMines plugin;
   private final FileManager.Config config;
   private double minTax;
   private double maxTax;
   private double defaultTax;
   private double minEntryFee;
   private double maxEntryFee;
   private double defaultEntryFee;
   private int defaultResetPercentage;
   private int minResetPercentage;
   private int maxResetPercentage;
   private String minesWorld;
   private List<MineBlock> blocks;
   private boolean metricsEnabled;
   private boolean debugMode;
   private boolean useMiniMessage;
   private int resetCheckInterval;
   private int autoSaveInterval;
   private int spaceBetweenMines;
   private boolean teleportOnCreate;
   private boolean teleportExistingOnCreate;
   private String[] mainCommandAliases;
   private int maxMineName;
   private int maxMineMotd;
   private boolean motdShownToOwner;
   private Map<String, PrivateMinesConfig.LeaderboardCategoryConfig> leaderboardCategories;
   private boolean packetMines;
   private boolean packetBreakEffects;
   private boolean packetVanillaDropsToInventory;
   private boolean packetInstantBreak;
   private double packetDigLeniency;
   private boolean packetTeleportOnReset;
   private boolean packetClearRealInterior;
   private int packetStoreEvictionMinutes;
   private int packetMaxDigsPerTick;
   private int packetMaxQueuedDigs;
   private boolean resetHotkeyEnabled;
   private boolean resetHotkeyRequireSneak;

   public PrivateMinesConfig(XPrivateMines plugin) {
      this.plugin = plugin;
      this.config = this.plugin.getFileManager().getConfig("config.yml").copyDefaults(true).save();
   }

   private void loadVariables() {
      YamlConfiguration config = this.getYamlConfig();
      this.metricsEnabled = config.getBoolean("enable-metrics");
      this.debugMode = config.getBoolean("enable-debug");
      this.useMiniMessage = config.getBoolean("use-minimessage");
      this.minTax = config.getDouble("min-tax");
      this.maxTax = config.getDouble("max-tax");
      this.defaultTax = config.getDouble("default-tax");
      this.minEntryFee = config.getDouble("min-entry-fee");
      this.maxEntryFee = config.getDouble("max-entry-fee");
      this.defaultEntryFee = config.getDouble("default-entry-fee");
      this.defaultResetPercentage = config.getInt("default-reset-percentage");
      this.minResetPercentage = config.getInt("min-reset-percentage");
      this.maxResetPercentage = config.getInt("max-reset-percentage");
      this.defaultResetPercentage = config.getInt("default-reset-percentage");
      this.resetCheckInterval = config.getInt("reset-check-interval-seconds");
      this.autoSaveInterval = config.getInt("auto-save-interval-seconds", 90);
      this.spaceBetweenMines = config.getInt("space-between-mines");
      this.teleportOnCreate = config.getBoolean("teleport-on-create", true);
      this.teleportExistingOnCreate = config.getBoolean("teleport-existing-on-create", true);
      this.minesWorld = config.getString("private-mines-world");
      this.reloadGuiBlocks();
      this.mainCommandAliases = config.getStringList("main-command").toArray(new String[0]);
      this.maxMineName = config.getInt("mine-name.max-length", 32);
      this.maxMineMotd = config.getInt("mine-motd.max-length", 128);
      this.motdShownToOwner = config.getBoolean("mine-motd.show-to-owner", false);
      this.leaderboardCategories = this.loadLeaderboardCategories(config);
      this.loadPacketMinesSettings(config);
      this.resetHotkeyEnabled = config.getBoolean("mine-reset-hotkey.enabled", false);
      this.resetHotkeyRequireSneak = config.getBoolean("mine-reset-hotkey.require-sneak", true);
   }

   private void loadPacketMinesSettings(YamlConfiguration config) {
      this.packetMines = config.getBoolean("packet-mines", false);
      this.packetBreakEffects = config.getBoolean("packet-mines-settings.break-effects", true);
      this.packetVanillaDropsToInventory = config.getBoolean("packet-mines-settings.vanilla-drops-to-inventory", true);
      this.packetInstantBreak = config.getBoolean("packet-mines-settings.instant-break", false);
      this.packetTeleportOnReset = config.getBoolean("packet-mines-settings.teleport-on-reset", false);
      this.packetClearRealInterior = config.getBoolean("packet-mines-settings.clear-real-interior", true);
      this.packetDigLeniency = config.getDouble("packet-mines-settings.dig-leniency", 0.85);
      if (this.packetDigLeniency <= 0.0 || this.packetDigLeniency > 1.0) {
         PrivateMinesLogger.warning("packet-mines-settings.dig-leniency must be in (0, 1]; got " + this.packetDigLeniency + " - using 0.85.");
         this.packetDigLeniency = 0.85;
      }

      this.packetStoreEvictionMinutes = config.getInt("packet-mines-settings.store-eviction-minutes", 0);
      if (this.packetStoreEvictionMinutes < 0) {
         PrivateMinesLogger.warning(
            "packet-mines-settings.store-eviction-minutes must be >= 0; got " + this.packetStoreEvictionMinutes + " - disabling eviction."
         );
         this.packetStoreEvictionMinutes = 0;
      }

      this.packetMaxDigsPerTick = config.getInt("packet-mines-settings.max-digs-per-tick", 0);
      if (this.packetMaxDigsPerTick < 0) {
         PrivateMinesLogger.warning("packet-mines-settings.max-digs-per-tick must be >= 0 (0 = unlimited); got " + this.packetMaxDigsPerTick + " - using 0.");
         this.packetMaxDigsPerTick = 0;
      }

      this.packetMaxQueuedDigs = config.getInt("packet-mines-settings.max-queued-digs", 20000);
      if (this.packetMaxQueuedDigs < 0) {
         PrivateMinesLogger.warning("packet-mines-settings.max-queued-digs must be >= 0 (0 = unbounded); got " + this.packetMaxQueuedDigs + " - using 0.");
         this.packetMaxQueuedDigs = 0;
      }
   }

   public void reloadGuiBlocks() {
      this.blocks = this.getYamlConfig().getStringList("gui.blocks").stream().map(MineBlock::parse).filter(Objects::nonNull).collect(Collectors.toList());
   }

   private Map<String, PrivateMinesConfig.LeaderboardCategoryConfig> loadLeaderboardCategories(YamlConfiguration config) {
      ConfigurationSection section = config.getConfigurationSection("leaderboard.categories");
      if (section == null) {
         return Collections.emptyMap();
      } else {
         Map<String, PrivateMinesConfig.LeaderboardCategoryConfig> map = new LinkedHashMap<>();

         for (String key : section.getKeys(false)) {
            ConfigurationSection cat = section.getConfigurationSection(key);
            if (cat != null) {
               map.put(
                  key,
                  new PrivateMinesConfig.LeaderboardCategoryConfig(
                     cat.getBoolean("enabled", true),
                     cat.getString("display-name", key),
                     cat.getString("gui-title", "&2&lLeaderboard"),
                     cat.getString("item-name", "&e&l#%rank% &f%mine_name%"),
                     cat.getStringList("item-lore")
                  )
               );
            }
         }

         return map;
      }
   }

   private FileManager.Config getConfig() {
      return this.config;
   }

   public YamlConfiguration getYamlConfig() {
      return this.config.get();
   }

   public void load() {
      this.getConfig().reload();
      this.loadVariables();
   }

   public void reload() {
      this.load();
   }

   public String getMinesWorldName() {
      return this.minesWorld;
   }

   public World getMinesWorld() {
      return this.plugin.getServer().getWorld(this.minesWorld);
   }

   @Generated
   public double getMinTax() {
      return this.minTax;
   }

   @Generated
   public double getMaxTax() {
      return this.maxTax;
   }

   @Generated
   public double getDefaultTax() {
      return this.defaultTax;
   }

   @Generated
   public double getMinEntryFee() {
      return this.minEntryFee;
   }

   @Generated
   public double getMaxEntryFee() {
      return this.maxEntryFee;
   }

   @Generated
   public double getDefaultEntryFee() {
      return this.defaultEntryFee;
   }

   @Generated
   public int getDefaultResetPercentage() {
      return this.defaultResetPercentage;
   }

   @Generated
   public int getMinResetPercentage() {
      return this.minResetPercentage;
   }

   @Generated
   public int getMaxResetPercentage() {
      return this.maxResetPercentage;
   }

   @Generated
   public List<MineBlock> getBlocks() {
      return this.blocks;
   }

   @Generated
   public boolean isMetricsEnabled() {
      return this.metricsEnabled;
   }

   @Generated
   public boolean isDebugMode() {
      return this.debugMode;
   }

   @Generated
   public boolean isUseMiniMessage() {
      return this.useMiniMessage;
   }

   @Generated
   public int getResetCheckInterval() {
      return this.resetCheckInterval;
   }

   @Generated
   public int getAutoSaveInterval() {
      return this.autoSaveInterval;
   }

   @Generated
   public int getSpaceBetweenMines() {
      return this.spaceBetweenMines;
   }

   @Generated
   public boolean isTeleportOnCreate() {
      return this.teleportOnCreate;
   }

   @Generated
   public boolean isTeleportExistingOnCreate() {
      return this.teleportExistingOnCreate;
   }

   @Generated
   public String[] getMainCommandAliases() {
      return this.mainCommandAliases;
   }

   @Generated
   public int getMaxMineName() {
      return this.maxMineName;
   }

   @Generated
   public int getMaxMineMotd() {
      return this.maxMineMotd;
   }

   @Generated
   public boolean isMotdShownToOwner() {
      return this.motdShownToOwner;
   }

   @Generated
   public Map<String, PrivateMinesConfig.LeaderboardCategoryConfig> getLeaderboardCategories() {
      return this.leaderboardCategories;
   }

   @Generated
   public boolean isPacketMines() {
      return this.packetMines;
   }

   @Generated
   public boolean isPacketBreakEffects() {
      return this.packetBreakEffects;
   }

   @Generated
   public boolean isPacketVanillaDropsToInventory() {
      return this.packetVanillaDropsToInventory;
   }

   @Generated
   public boolean isPacketInstantBreak() {
      return this.packetInstantBreak;
   }

   @Generated
   public double getPacketDigLeniency() {
      return this.packetDigLeniency;
   }

   @Generated
   public boolean isPacketTeleportOnReset() {
      return this.packetTeleportOnReset;
   }

   @Generated
   public boolean isPacketClearRealInterior() {
      return this.packetClearRealInterior;
   }

   @Generated
   public int getPacketStoreEvictionMinutes() {
      return this.packetStoreEvictionMinutes;
   }

   @Generated
   public int getPacketMaxDigsPerTick() {
      return this.packetMaxDigsPerTick;
   }

   @Generated
   public int getPacketMaxQueuedDigs() {
      return this.packetMaxQueuedDigs;
   }

   @Generated
   public boolean isResetHotkeyEnabled() {
      return this.resetHotkeyEnabled;
   }

   @Generated
   public boolean isResetHotkeyRequireSneak() {
      return this.resetHotkeyRequireSneak;
   }

   public record LeaderboardCategoryConfig(boolean enabled, String displayName, String guiTitle, String itemName, List<String> itemLore) {
   }
}
