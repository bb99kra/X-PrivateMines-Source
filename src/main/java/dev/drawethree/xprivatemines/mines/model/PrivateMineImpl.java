package dev.drawethree.xprivatemines.mines.model;

import org.codemc.worldguardwrapper.region.IWrappedRegion;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.Mine;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.mines.model.settings.AccessSettings;
import dev.drawethree.xprivatemines.mines.model.settings.EconomySettings;
import dev.drawethree.xprivatemines.mines.model.settings.LocationSettings;
import dev.drawethree.xprivatemines.mines.model.settings.MineSettings;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Generated;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PrivateMineImpl implements PrivateMine {
   private final UUID uuid;
   private UUID owner;
   private MinesSchematic schematic;
   private MineImpl mineImpl;
   private IWrappedRegion region;
   private LocationSettings locationSettings;
   private AccessSettings accessSettings;
   private EconomySettings economySettings;
   private MineSettings mineSettings;
   private boolean dirty;
   private String mineName;
   private String mineMotd;

   private PrivateMineImpl(OfflinePlayer owner, MinesSchematic schematic, long x, long z, double entryFee, double tax, int resetPercentage, MineTierImpl tier) {
      this.uuid = UUID.randomUUID();
      this.owner = owner != null ? owner.getUniqueId() : null;
      this.schematic = schematic;
      this.mineImpl = new MineImpl();
      this.mineSettings = new MineSettings(0, resetPercentage, tier);
      this.economySettings = new EconomySettings(entryFee, tax, 0.0);
      this.accessSettings = new AccessSettings();
      this.locationSettings = new LocationSettings(
         x, z, schematic.getSettings().getSpawn().add(x, 0.0, z).toLocation(), schematic.getSettings().getResetLocation().add(x, 0.0, z).toLocation()
      );

      try {
         this.saveToConfig();
      } catch (IOException var14) {
         var14.printStackTrace();
      }
   }

   public PrivateMineImpl(UUID uuid, ConfigurationSection section) {
      this.uuid = uuid;
      this.loadFromConfig(section);
   }

   @Override
   public void teleport(Player p) {
      Schedulers.sync().run(() -> p.teleport(this.locationSettings.getSpawnLocation()));
   }

   public void teleportToReset(Player p) {
      Schedulers.sync().run(() -> p.teleport(this.locationSettings.getResetLocation()));
   }

   @Override
   public boolean isBanned(OfflinePlayer p) {
      return this.accessSettings.getBannedPlayers().contains(p.getUniqueId());
   }

   @Override
   public List<OfflinePlayer> getBannedPlayers() {
      return this.accessSettings.getBannedPlayers().stream().<OfflinePlayer>map(Players::getOfflineNullable).collect(Collectors.toList());
   }

   @Override
   public Set<UUID> getBannedPlayersUUID() {
      return this.accessSettings.getBannedPlayers();
   }

   @Override
   public OfflinePlayer getOfflineOwner() {
      return this.owner == null ? null : Players.getOfflineNullable(this.owner);
   }

   @Override
   public Mine getMine() {
      return this.mineImpl;
   }

   private void loadFromConfig(ConfigurationSection config) {
      try {
         MinesSchematic schematic = XPrivateMines.getInstance().getMinesManager().getSchematic(config.getString("schematic"));
         if (schematic == null) {
            return;
         }

         this.schematic = schematic;
         String owner = config.getString("owner");
         if (owner != null) {
            this.owner = UUID.fromString(owner);
         }

         int expandLevel = config.getInt("expandLevel");
         int resetPercentage = config.getInt("reset-percentage", XPrivateMines.getInstance().getPrivateMinesConfig().getDefaultResetPercentage());
         MineTierImpl tier = XPrivateMines.getInstance().getMineTierManager().getTier(config.getString("tier"));
         this.mineSettings = new MineSettings(expandLevel, resetPercentage, tier);
         long xOffset = config.getLong("x-offset");
         long zOffset = config.getLong("z-offset");
         Location spawn = config.getLocation("spawn");
         Location reset = config.getLocation("reset-teleport");
         this.locationSettings = new LocationSettings(xOffset, zOffset, spawn, reset);
         Set<UUID> bannedPlayers = config.getStringList("banned-players").stream().map(UUID::fromString).collect(Collectors.toSet());
         boolean open = config.getBoolean("open");
         this.accessSettings = new AccessSettings(open, bannedPlayers);
         double tax = config.getDouble("tax");
         double entryfee = config.getDouble("entryfee");
         double unclaimedMoney = config.getDouble("unclaimed-money");
         this.economySettings = new EconomySettings(entryfee, tax, unclaimedMoney);
         this.region = (IWrappedRegion)XPrivateMines.getInstance()
            .getWorldGuardWrapper()
            .getRegion(XPrivateMines.getInstance().getPrivateMinesConfig().getMinesWorld(), this.uuid.toString() + "_pmine")
            .orElse(null);
         this.mineImpl = new MineImpl(this, config);
         this.mineName = config.getString("mine-name", null);
         this.mineMotd = config.getString("mine-motd", null);
      } catch (Exception var21) {
         var21.printStackTrace();
      }
   }

   public void saveToConfig() throws IOException {
      YamlConfiguration config = XPrivateMines.getInstance().getMinesConfig().getYamlConfig();
      String rootPath = "mines." + this.uuid.toString() + ".";
      if (this.owner != null) {
         config.set(rootPath + "owner", this.owner.toString());
      } else {
         config.set(rootPath + "owner", null);
      }

      config.set(rootPath + "schematic", this.schematic.getName());
      config.set(rootPath + "x-offset", this.locationSettings.getXOffset());
      config.set(rootPath + "z-offset", this.locationSettings.getZOffset());
      config.set(rootPath + "banned-players", this.accessSettings.getBannedPlayers().stream().map(UUID::toString).collect(Collectors.toList()));
      config.set(rootPath + "mine-material", this.mineImpl.getSelectedBlock() == null ? null : this.mineImpl.getSelectedBlock().serialize());
      config.set(rootPath + "open", this.accessSettings.isOpen());
      config.set(rootPath + "tax", this.economySettings.getTax());
      config.set(rootPath + "entryfee", this.economySettings.getEntryFee());
      config.set(rootPath + "expandLevel", this.mineSettings.getExpandLevel());
      config.set(rootPath + "reset-percentage", this.mineSettings.getResetPercentage());
      config.set(rootPath + "spawn", this.locationSettings.getSpawnLocation());
      config.set(rootPath + "reset-teleport", this.locationSettings.getResetLocation());
      config.set(rootPath + "unclaimed-money", this.economySettings.getUnclaimedMoney());
      config.set(rootPath + "tier", this.mineSettings.getTier().getKey());
      config.set(rootPath + "mine-name", this.mineName);
      config.set(rootPath + "mine-motd", this.mineMotd);
      PrivateMinesLogger.info("Saving PrivateMine " + this.uuid + " to config");
      XPrivateMines.getInstance().getMinesConfig().save();
   }

   @Override
   public boolean isInPrivateMine(Location location) {
      return location != null && this.region != null
         ? this.locationSettings.getSpawnLocation().getWorld().getName().equals(location.getWorld().getName()) && this.region.contains(location)
         : false;
   }

   @Override
   public boolean isInMine(Location location) {
      return location == null
         ? false
         : this.locationSettings.getSpawnLocation().getWorld().getName().equals(location.getWorld().getName()) && this.mineImpl.getRegion().contains(location);
   }

   @Override
   public boolean isOwner(OfflinePlayer p) {
      return p.getUniqueId().equals(this.owner);
   }

   @Override
   public List<Player> getPlayersInPrivateMine() {
      return Players.all().stream().filter(player -> this.isInPrivateMine(player.getLocation())).collect(Collectors.toList());
   }

   @Override
   public List<Player> getPlayersInMine() {
      return Players.all().stream().filter(player -> this.isInMine(player.getLocation())).collect(Collectors.toList());
   }

   @Override
   public int getMineSize() {
      return this.schematic.getSettings().getMineSize() + this.mineSettings.getExpandLevel();
   }

   @Override
   public MinesSchematic getSchematic() {
      return this.schematic;
   }

   @Override
   public Location getSpawnLocation() {
      return this.locationSettings.getSpawnLocation();
   }

   @Override
   public synchronized double getUnclaimedMoney() {
      return this.economySettings.getUnclaimedMoney();
   }

   @Override
   public double getZOffset() {
      return this.locationSettings.getZOffset();
   }

   @Override
   public double getXOffset() {
      return this.locationSettings.getXOffset();
   }

   @Override
   public boolean isOpen() {
      return this.accessSettings.isOpen();
   }

   @Override
   public boolean isOpenTo(OfflinePlayer offlinePlayer) {
      return this.accessSettings.isOpen() || !this.accessSettings.getBannedPlayers().contains(offlinePlayer.getUniqueId());
   }

   @Override
   public double getEntryFee() {
      return this.economySettings.getEntryFee();
   }

   @Override
   public double getTax() {
      return this.economySettings.getTax();
   }

   @Override
   public int getExpandLevel() {
      return this.mineSettings.getExpandLevel();
   }

   @Override
   public MineTier getTier() {
      return this.mineSettings.getTier();
   }

   @Override
   public void setTier(MineTier mineTier) {
      this.mineSettings.setTier(mineTier);
   }

   @Override
   public synchronized void setUnclaimedMoney(double money) {
      this.economySettings.setUnclaimedMoney(money);
      this.dirty = true;
   }

   @Override
   public void setExpandLevel(int level) {
      this.mineSettings.setExpandLevel(level);
      this.dirty = true;
   }

   @Override
   public double getResetPercentage() {
      return this.mineSettings.getResetPercentage();
   }

   public void setTier(MineTierImpl tier) {
      this.mineSettings.setTier(tier);
      this.dirty = true;
   }

   @Override
   public void setOpen(boolean open) {
      this.accessSettings.setOpen(open);
      this.dirty = true;
   }

   @Override
   public void setEntryFee(double newFee) {
      this.economySettings.setEntryFee(newFee);
      this.dirty = true;
   }

   @Override
   public void setTax(double newTax) {
      this.economySettings.setTax(newTax);
      this.dirty = true;
   }

   @Override
   public void setResetPercentage(int resetPercentage) {
      this.mineSettings.setResetPercentage(resetPercentage);
      this.dirty = true;
   }

   @Override
   public void setOwner(UUID owner) {
      this.owner = owner;
      this.dirty = true;
   }

   @Override
   public String getMineName() {
      return this.mineName;
   }

   @Override
   public void setMineName(String name) {
      this.mineName = name;
      this.dirty = true;
   }

   @Override
   public String getMineMotd() {
      return this.mineMotd;
   }

   @Override
   public void setMineMotd(String motd) {
      this.mineMotd = motd;
      this.dirty = true;
   }

   @Generated
   @Override
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   @Override
   public UUID getOwner() {
      return this.owner;
   }

   @Generated
   public MineImpl getMineImpl() {
      return this.mineImpl;
   }

   @Generated
   public IWrappedRegion getRegion() {
      return this.region;
   }

   @Generated
   public void setRegion(IWrappedRegion region) {
      this.region = region;
   }

   @Generated
   public LocationSettings getLocationSettings() {
      return this.locationSettings;
   }

   @Generated
   public AccessSettings getAccessSettings() {
      return this.accessSettings;
   }

   @Generated
   public EconomySettings getEconomySettings() {
      return this.economySettings;
   }

   @Generated
   public MineSettings getMineSettings() {
      return this.mineSettings;
   }

   @Generated
   public boolean isDirty() {
      return this.dirty;
   }

   @Generated
   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }

   public static class Builder {
      private OfflinePlayer owner;
      private MinesSchematic schematic;
      private long x;
      private long z;
      private double entryFee;
      private double tax;
      private int resetPercentage;
      private MineTierImpl tier;

      public PrivateMineImpl.Builder setOwner(OfflinePlayer owner) {
         this.owner = owner;
         return this;
      }

      public PrivateMineImpl.Builder setSchematic(MinesSchematic schematic) {
         this.schematic = schematic;
         return this;
      }

      public PrivateMineImpl.Builder setX(long x) {
         this.x = x;
         return this;
      }

      public PrivateMineImpl.Builder setZ(long z) {
         this.z = z;
         return this;
      }

      public PrivateMineImpl.Builder setEntryFee(double entryFee) {
         this.entryFee = entryFee;
         return this;
      }

      public PrivateMineImpl.Builder setTax(double tax) {
         this.tax = tax;
         return this;
      }

      public PrivateMineImpl.Builder setResetPercentage(int resetPercentage) {
         this.resetPercentage = resetPercentage;
         return this;
      }

      public PrivateMineImpl.Builder setTier(MineTierImpl tier) {
         this.tier = tier;
         return this;
      }

      public PrivateMineImpl build() {
         return new PrivateMineImpl(this.owner, this.schematic, this.x, this.z, this.entryFee, this.tax, this.resetPercentage, this.tier);
      }
   }
}
