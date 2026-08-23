package dev.drawethree.xprivatemines.manager;

import dev.drawethree.xprison.api.XPrisonAPI;
import dev.drawethree.xprison.api.currency.model.XPrisonCurrency;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.economy.MineEconomyProvider;
import dev.drawethree.xprivatemines.economy.CurrencyProvider;
import dev.drawethree.xprivatemines.economy.impl.ExcellentEconomyCurrencyProvider;
import dev.drawethree.xprivatemines.economy.impl.PlayerPointsCurrencyProvider;
import dev.drawethree.xprivatemines.economy.impl.VaultCurrencyProvider;
import dev.drawethree.xprivatemines.economy.impl.XPrisonCurrencyProvider;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.math.BigDecimal;
import java.util.Locale;
import lombok.Generated;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;

public class EconomyManager implements MineEconomyProvider {
   private static final String DEFAULT_CURRENCY_NAME = "money";
   private final XPrivateMines plugin;
   private CurrencyProvider provider;

   public EconomyManager(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void enable() {
      YamlConfiguration config = this.plugin.getPrivateMinesConfig().getYamlConfig();
      String economyType = config.getString("economy.type", "Vault").trim();
      String currencyId = config.getString("economy.currency", "default").trim();
      String var4 = economyType.toLowerCase(Locale.ROOT);
      switch (var4) {
         case "x-prison":
            this.setupXPrison(currencyId);
            break;
         case "excellenteconomy":
            this.setupExcellentEconomy(currencyId);
            break;
         case "playerpoints":
            this.setupPlayerPoints();
            break;
         case "vault":
         default:
            this.setupVault();
      }
   }

   private void setupXPrison(String currencyId) {
      if (!Bukkit.getPluginManager().isPluginEnabled("X-Prison")) {
         PrivateMinesLogger.warning("X-Prison is not enabled!");
      } else {
         XPrisonCurrency currency;
         try {
            currency = XPrisonAPI.getInstance().getCurrencyApi().getCurrency(currencyId);
         } catch (LinkageError | RuntimeException var4) {
            PrivateMinesLogger.warning("X-Prison's Currency module is not available - cannot use X-Prison economy.");
            return;
         }

         if (currency == null) {
            PrivateMinesLogger.warning("Invalid X-Prison currency: " + currencyId);
         } else {
            this.provider = new XPrisonCurrencyProvider(currency);
            PrivateMinesLogger.info("Using X-Prison currency: " + currency.getName());
         }
      }
   }

   private void setupExcellentEconomy(String currencyId) {
      if (!Bukkit.getPluginManager().isPluginEnabled("ExcellentEconomy")) {
         PrivateMinesLogger.warning("ExcellentEconomy is not enabled!");
      } else {
         RegisteredServiceProvider<ExcellentEconomyAPI> provider = Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class);
         if (provider == null) {
            PrivateMinesLogger.warning("Unable to find ExcellentEconomyAPI service!");
         } else {
            ExcellentEconomyAPI api = (ExcellentEconomyAPI)provider.getProvider();
            ExcellentCurrency currency = api.getCurrency(currencyId);
            if (currency == null) {
               PrivateMinesLogger.warning("Invalid ExcellentEconomy currency: " + currencyId);
            } else {
               this.provider = new ExcellentEconomyCurrencyProvider(api, currency);
               PrivateMinesLogger.info("Using ExcellentEconomy currency: " + currency.getName());
            }
         }
      }
   }

   private void setupPlayerPoints() {
      if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
         PrivateMinesLogger.warning("PlayerPoints is not enabled!");
      } else {
         PlayerPointsAPI api = PlayerPoints.getInstance().getAPI();
         if (api == null) {
            PrivateMinesLogger.warning("PlayerPoints API is not initialized!");
         } else {
            this.provider = new PlayerPointsCurrencyProvider(api);
            PrivateMinesLogger.info("Using PlayerPoints as economy.");
         }
      }
   }

   private void setupVault() {
      Economy economy = this.getVaultEconomy();
      if (economy == null) {
         PrivateMinesLogger.warning("Vault economy not found or not available!");
      } else {
         this.provider = new VaultCurrencyProvider(economy);
         PrivateMinesLogger.info("Using Vault economy.");
      }
   }

   private Economy getVaultEconomy() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
      return rsp != null ? (Economy)rsp.getProvider() : null;
   }

   public void reload() {
      this.enable();
   }

   @Override
   public boolean withdraw(Player player, double amount) {
      return this.provider != null && this.provider.withdraw(player, BigDecimal.valueOf(amount));
   }

   @Override
   public boolean deposit(Player player, double amount) {
      return this.provider != null && this.provider.deposit(player, BigDecimal.valueOf(amount));
   }

   @Override
   public double getBalance(Player player) {
      return this.provider != null ? this.provider.getBalance(player).doubleValue() : 0.0;
   }

   @Override
   public double getBalance(OfflinePlayer player) {
      return this.provider != null ? this.provider.getBalance(player).doubleValue() : 0.0;
   }

   @Override
   public boolean has(Player player, double amount) {
      return this.provider != null && this.provider.has(player, BigDecimal.valueOf(amount));
   }

   @Override
   public String format(Player player, double amount) {
      return this.provider != null ? this.provider.format(player, BigDecimal.valueOf(amount)) : String.valueOf(amount);
   }

   public String getCurrencyName() {
      if (this.provider == null) {
         return "money";
      } else {
         String name = this.provider.getCurrencyName();
         return name != null && !name.isBlank() ? name : "money";
      }
   }

   @Generated
   public CurrencyProvider getProvider() {
      return this.provider;
   }
}
