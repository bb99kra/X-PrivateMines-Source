package dev.drawethree.xprivatemines;

import org.bstats.bukkit.Metrics;
import com.github.lalyos.jfiglet.FigletFont;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import dev.drawethree.xprivatemines.addons.AddonManager;
import dev.drawethree.xprivatemines.api.XPrivateMinesAPI;
import dev.drawethree.xprivatemines.api.XPrivateMinesAPIImpl;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.config.FileManager;
import dev.drawethree.xprivatemines.config.MessageConfig;
import dev.drawethree.xprivatemines.config.MineTiersConfig;
import dev.drawethree.xprivatemines.config.MinesConfig;
import dev.drawethree.xprivatemines.config.PrivateMinesConfig;
import dev.drawethree.xprivatemines.config.SchematicSettingsConfig;
import dev.drawethree.xprivatemines.gui.config.GUIConfigLoader;
import dev.drawethree.xprivatemines.hook.CustomBlockHooks;
import dev.drawethree.xprivatemines.hook.ItemsAdderHook;
import dev.drawethree.xprivatemines.hook.xprison.XPrisonAreaEnchantAudit;
import dev.drawethree.xprivatemines.listener.PrivateMineRegionListener;
import dev.drawethree.xprivatemines.listener.PrivateMinesListener;
import dev.drawethree.xprivatemines.listener.WandListener;
import dev.drawethree.xprivatemines.manager.CooldownManager;
import dev.drawethree.xprivatemines.manager.EconomyManager;
import dev.drawethree.xprivatemines.manager.MineTierManagerImpl;
import dev.drawethree.xprivatemines.manager.PrivateMinesManagerImpl;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupManager;
import dev.drawethree.xprivatemines.placeholders.PrivateMinesPlaceholder;
import dev.drawethree.xprivatemines.service.SchematicCreationService;
import dev.drawethree.xprivatemines.task.PrivateMineAutoSaveTask;
import dev.drawethree.xprivatemines.task.PrivateMineResetTask;
import dev.drawethree.xprivatemines.utils.PlaceholderUtils;
import dev.drawethree.xprivatemines.utils.chat.ChatInputManager;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import dev.drawethree.xprivatemines.utils.world.VoidWorldGenerator;
import dev.drawethree.xprivatemines.virtual.VirtualMineEngine;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import lombok.Generated;
import lombok.NonNull;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;

public final class XPrivateMines extends ExtendedJavaPlugin {
   private static boolean IS_PAPER;
   private BukkitAudiences adventure;
   private static XPrivateMines instance;
   private PrivateMinesConfig privateMinesConfig;
   private MinesConfig minesConfig;
   private MessageConfig messageConfig;
   private SchematicSettingsConfig schematicSettingsConfig;
   private MineTiersConfig mineTiersConfig;
   private PrivateMinesManagerImpl minesManager;
   private MineTierManagerImpl mineTierManager;
   private ChatInputManager chatInputManager;
   private GUIConfigLoader guiConfigLoader;
   private FileManager fileManager;
   private PrivateMineResetTask resetTask;
   private PrivateMineAutoSaveTask autoSaveTask;
   private EconomyManager economyManager;
   private AddonManager addonManager;
   private CustomBlockHooks customBlockHooks;
   private SchematicSetupManager schematicSetupManager;
   private SchematicCreationService schematicCreationService;
   private VirtualMineEngine virtualMineEngine;
   private boolean packetMinesEnabled;
   private boolean packetEventsLoaded;

   protected void load() {
      instance = this;

      try {
         Class.forName("com.destroystokyo.paper.Title");
         IS_PAPER = true;
      } catch (ClassNotFoundException var2) {
      }

      PrivateMinesLogger.setLogger(this.getLogger());
      this.saveDefaultConfig();
      this.packetMinesEnabled = this.getConfig().getBoolean("packet-mines", false);
      if (this.packetMinesEnabled && !this.isPacketModeVersionSupported()) {
         this.getLogger()
            .warning(
               "packet-mines is enabled but this server version ("
                  + Bukkit.getBukkitVersion()
                  + ") is below the supported floor for packet mode (Spigot/Paper 1.20+). Falling back to the classic real-block reset - mines still work normally. Set packet-mines: false in config.yml to silence this."
            );
         this.packetMinesEnabled = false;
      }

      if (this.packetMinesEnabled) {
         PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
         PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(false);
         PacketEvents.getAPI().load();
         this.packetEventsLoaded = true;
      }
   }

   private boolean isPacketModeVersionSupported() {
      try {
         String raw = Bukkit.getBukkitVersion().split("-")[0];
         String[] parts = raw.split("\\.");
         int major = Integer.parseInt(parts[0]);
         int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
         return major > 1 || major == 1 && minor >= 20;
      } catch (Throwable var5) {
         return true;
      }
   }

   protected void enable() {
      this.adventure = BukkitAudiences.create(this);
      this.saveDefaultConfig();
      this.fileManager = new FileManager(this);
      this.customBlockHooks = new CustomBlockHooks(this);
      this.privateMinesConfig = new PrivateMinesConfig(this);
      this.privateMinesConfig.load();
      this.createPrivateMinesWorldIfNeeded();
      if (this.packetMinesEnabled) {
         PacketEvents.getAPI().init();
         this.virtualMineEngine = new VirtualMineEngine(this);
         this.virtualMineEngine.init();
      } else {
         this.reloadConfig();
         this.getConfig().set("packet-mines-last-mode", "fawe");
         this.saveConfig();
      }

      this.minesConfig = new MinesConfig(this);
      this.minesConfig.load();
      this.printOnEnableMessage();
      this.messageConfig = new MessageConfig(this);
      this.messageConfig.load();
      this.schematicSettingsConfig = new SchematicSettingsConfig(this);
      this.schematicSettingsConfig.load();
      this.mineTiersConfig = new MineTiersConfig(this);
      this.mineTiersConfig.load();
      this.fileManager.getConfig("guis.yml").copyDefaults(true).save();
      this.schematicSetupManager = new SchematicSetupManager(this);
      this.schematicCreationService = new SchematicCreationService(this);
      this.registerMainCommand();
      this.registerMainListener();
      this.guiConfigLoader = new GUIConfigLoader(this);
      this.chatInputManager = new ChatInputManager(this);
      this.economyManager = new EconomyManager(this);
      this.economyManager.enable();
      this.registerPlaceholders();
      this.mineTierManager = new MineTierManagerImpl(this);
      this.mineTierManager.load();
      this.minesManager = new PrivateMinesManagerImpl(this);
      this.minesManager.load();
      this.customBlockHooks.registerAll();
      this.resetTask = new PrivateMineResetTask(this, this.minesManager);
      this.resetTask.start();
      this.autoSaveTask = new PrivateMineAutoSaveTask(this);
      this.autoSaveTask.start();
      SoundUtils.init(this.privateMinesConfig.getYamlConfig());
      PlaceholderUtils.init(this.privateMinesConfig.getYamlConfig());
      XPrivateMinesAPI.setInstance(new XPrivateMinesAPIImpl(this));
      this.addonManager = new AddonManager(this);
      this.addonManager.loadAddons();
      this.startMetricsIfEnabled();
      XPrisonAreaEnchantAudit.scheduleIfNeeded(this);
   }

   private void registerMainListener() {
      new PrivateMinesListener(this).register();
      new PrivateMineRegionListener(this).register();
      new WandListener(this).register();
   }

   private void createPrivateMinesWorldIfNeeded() {
      String worldName = this.privateMinesConfig.getMinesWorldName();
      File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
      WorldCreator creator = new WorldCreator(worldName);
      creator.generator(new VoidWorldGenerator());
      creator.environment(Environment.NORMAL);
      creator.type(WorldType.FLAT);
      creator.generateStructures(false);
      if (!worldFolder.exists()) {
         this.getLogger().info("Creating flat world: " + worldName);
         World world = Bukkit.createWorld(creator);
         world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
         if (world != null) {
            PrivateMinesLogger.info("World created successfully!");
         } else {
            PrivateMinesLogger.warning("World creation failed.");
         }
      } else {
         PrivateMinesLogger.info("World folder already exists, loading: " + worldName);
         World world = Bukkit.createWorld(creator);
         world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
         if (world != null) {
            PrivateMinesLogger.info("World loaded successfully.");
         } else {
            PrivateMinesLogger.warning("World loading failed.");
         }
      }
   }

   private void startMetricsIfEnabled() {
      if (this.privateMinesConfig.isMetricsEnabled()) {
         new Metrics(this, 26484);
      }
   }

   public void reload() {
      this.privateMinesConfig.reload();
      this.minesConfig.reload();
      this.messageConfig.reload();
      this.mineTiersConfig.reload();
      this.schematicSettingsConfig.reload();
      this.fileManager.getConfig("guis.yml").reload();
      this.economyManager.reload();
      this.mineTierManager.reload();
      this.minesManager.reload();
      CooldownManager.INSTANCE.reload();
      SoundUtils.init(this.privateMinesConfig.getYamlConfig());
      PlaceholderUtils.init(this.privateMinesConfig.getYamlConfig());
      if (this.addonManager != null) {
         this.addonManager.reloadAddons();
      }
   }

   protected void disable() {
      if (this.virtualMineEngine != null) {
         this.virtualMineEngine.shutdown();
      }

      if (this.packetEventsLoaded) {
         PacketEvents.getAPI().terminate();
      }

      if (this.autoSaveTask != null) {
         this.autoSaveTask.cancel();
         this.autoSaveTask.run();
      }

      if (this.resetTask != null) {
         this.resetTask.cancel();
      }

      if (this.minesManager != null) {
         this.minesManager.stopPregen();
         this.minesManager.saveLastXZ();
      }

      if (this.schematicSetupManager != null) {
         this.schematicSetupManager.clear();
      }

      if (this.addonManager != null) {
         this.addonManager.unloadAddons();
      }
   }

   private void printOnEnableMessage() {
      try {
         PrivateMinesLogger.info("\n\n" + FigletFont.convertOneLine("X - PRIVATE MINES"));
         PrivateMinesLogger.info(this.getDescription().getVersion());
         PrivateMinesLogger.info("&fBy: &e" + this.getDescription().getAuthors());
         PrivateMinesLogger.info("&fWebsite: &e" + this.getDescription().getWebsite());
         PrivateMinesLogger.info("&fDiscord Support: &ehttps://discord.gg/ZeSkmEC6mG");
      } catch (IOException var2) {
      }
   }

   private void registerMainCommand() {
      new PrivateMineCommand(this).register();
      PrivateMinesLogger.info("Registered main command.");
   }

   public WorldGuardWrapper getWorldGuardWrapper() {
      return WorldGuardWrapper.getInstance();
   }

   private void registerPlaceholders() {
      if (this.isPlaceholderAPIEnabled()) {
         new PrivateMinesPlaceholder(this).register();
      }
   }

   public boolean isPlaceholderAPIEnabled() {
      return this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
   }

   public YamlConfiguration getGuiConfig() {
      return this.fileManager.getConfig("guis.yml").reload().get();
   }

   public void debug(String msg) {
      if (this.privateMinesConfig.isDebugMode()) {
         PrivateMinesLogger.info(TextUtils.applyColor(msg));
      }
   }

   public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
      if (this.privateMinesConfig == null) {
         return super.getDefaultWorldGenerator(worldName, id);
      } else {
         return (ChunkGenerator)(worldName.equalsIgnoreCase(this.privateMinesConfig.getMinesWorldName())
            ? new VoidWorldGenerator()
            : super.getDefaultWorldGenerator(worldName, id));
      }
   }

   public boolean isXPrisonEnabled() {
      return this.getServer().getPluginManager().isPluginEnabled("X-Prison");
   }

   public boolean isPacketMinesActive() {
      return this.virtualMineEngine != null;
   }

   public void ifPacketEngine(Consumer<VirtualMineEngine> consumer) {
      if (this.virtualMineEngine != null) {
         consumer.accept(this.virtualMineEngine);
      }
   }

   public boolean isItemsAdderEnabled() {
      return this.customBlockHooks != null && this.customBlockHooks.getItemsAdderHook().isEnabled();
   }

   public boolean isNexoEnabled() {
      return this.customBlockHooks != null && this.customBlockHooks.getNexoHook().isEnabled();
   }

   public boolean isOraxenEnabled() {
      return this.customBlockHooks != null && this.customBlockHooks.getOraxenHook().isEnabled();
   }

   public ItemsAdderHook getItemsAdderHook() {
      return this.customBlockHooks == null ? null : this.customBlockHooks.getItemsAdderHook();
   }

   public void refreshCustomBlockConfigs(String providerName) {
      Schedulers.sync().run(() -> {
         if (this.mineTierManager != null) {
            this.mineTierManager.reload();
         }

         if (this.privateMinesConfig != null) {
            this.privateMinesConfig.reloadGuiBlocks();
         }

         PrivateMinesLogger.info(providerName + " data loaded - refreshed mine tiers and selectable blocks.");
      });
   }

   @NonNull
   public BukkitAudiences adventure() {
      if (this.adventure == null) {
         throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
      } else {
         return this.adventure;
      }
   }

   public static boolean isUseMiniMessage() {
      return IS_PAPER && instance.getPrivateMinesConfig().isUseMiniMessage();
   }

   @Generated
   public static XPrivateMines getInstance() {
      return instance;
   }

   @Generated
   public PrivateMinesConfig getPrivateMinesConfig() {
      return this.privateMinesConfig;
   }

   @Generated
   public MinesConfig getMinesConfig() {
      return this.minesConfig;
   }

   @Generated
   public MessageConfig getMessageConfig() {
      return this.messageConfig;
   }

   @Generated
   public SchematicSettingsConfig getSchematicSettingsConfig() {
      return this.schematicSettingsConfig;
   }

   @Generated
   public MineTiersConfig getMineTiersConfig() {
      return this.mineTiersConfig;
   }

   @Generated
   public PrivateMinesManagerImpl getMinesManager() {
      return this.minesManager;
   }

   @Generated
   public MineTierManagerImpl getMineTierManager() {
      return this.mineTierManager;
   }

   @Generated
   public ChatInputManager getChatInputManager() {
      return this.chatInputManager;
   }

   @Generated
   public GUIConfigLoader getGuiConfigLoader() {
      return this.guiConfigLoader;
   }

   @Generated
   public FileManager getFileManager() {
      return this.fileManager;
   }

   @Generated
   public PrivateMineResetTask getResetTask() {
      return this.resetTask;
   }

   @Generated
   public PrivateMineAutoSaveTask getAutoSaveTask() {
      return this.autoSaveTask;
   }

   @Generated
   public EconomyManager getEconomyManager() {
      return this.economyManager;
   }

   @Generated
   public AddonManager getAddonManager() {
      return this.addonManager;
   }

   @Generated
   public CustomBlockHooks getCustomBlockHooks() {
      return this.customBlockHooks;
   }

   @Generated
   public SchematicSetupManager getSchematicSetupManager() {
      return this.schematicSetupManager;
   }

   @Generated
   public SchematicCreationService getSchematicCreationService() {
      return this.schematicCreationService;
   }

   @Generated
   public VirtualMineEngine getVirtualMineEngine() {
      return this.virtualMineEngine;
   }
}
