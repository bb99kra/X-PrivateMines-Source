package dev.drawethree.xprivatemines.manager;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.manager.MineTierManager;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.config.MineTiersConfig;
import dev.drawethree.xprivatemines.mines.model.MineTierImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;

public class MineTierManagerImpl implements MineTierManager {
   private final Map<String, MineTierImpl> tiers = new LinkedHashMap<>();
   private MineTierImpl defaultTier;
   private final XPrivateMines plugin;

   public MineTierManagerImpl(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void load() {
      this.loadAllTiers();
   }

   public void reload() {
      this.load();
   }

   private void loadAllTiers() {
      this.tiers.clear();
      MineTiersConfig config = this.plugin.getMineTiersConfig();
      ConfigurationSection section = config.getYamlConfig().getConfigurationSection("tiers");
      if (section != null) {
         for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name", key);
            double upgradeCost = section.getDouble(key + ".upgrade-cost", 0.0);
            Map<MineBlock, Integer> blocks = new LinkedHashMap<>();
            ConfigurationSection blockSec = section.getConfigurationSection(key + ".blocks");
            if (blockSec != null) {
               for (String materialKey : blockSec.getKeys(false)) {
                  MineBlock block = MineBlock.parse(materialKey);
                  if (block == null) {
                     PrivateMinesLogger.warning("Invalid block: " + materialKey);
                  } else {
                     blocks.put(block, blockSec.getInt(materialKey));
                  }
               }
            }

            MineTierImpl tier = new MineTierImpl(key, name, upgradeCost, blocks);
            this.tiers.put(key, tier);
            if (this.defaultTier == null) {
               this.defaultTier = tier;
            }

            PrivateMinesLogger.info("Loaded mine tier " + name);
         }
      }
   }

   @Override
   public Optional<MineTier> getNextTier(String currentKey) {
      boolean found = false;

      for (Entry<String, MineTierImpl> entry : this.tiers.entrySet()) {
         if (found) {
            return Optional.of(entry.getValue());
         }

         if (entry.getKey().equals(currentKey)) {
            found = true;
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<MineTier> getNextTier(MineTier currentTier) {
      return this.getNextTier(currentTier.getKey());
   }

   @Override
   public List<MineTier> getAllTiers() {
      return new ArrayList<>(this.tiers.values());
   }

   public MineTierImpl getDefaultTier() {
      return this.defaultTier;
   }

   public MineTierImpl getTier(String key) {
      return this.tiers.getOrDefault(key, this.getDefaultTier());
   }

   public MineTierImpl getTierStrictly(String key) {
      return this.tiers.get(key);
   }
}
