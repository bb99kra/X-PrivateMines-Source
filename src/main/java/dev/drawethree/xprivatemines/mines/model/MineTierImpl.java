package dev.drawethree.xprivatemines.mines.model;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Generated;

public class MineTierImpl implements MineTier {
   private final String key;
   private final String name;
   private final double upgradeCost;
   private final Map<MineBlock, Integer> mineBlockWeights;

   public MineTierImpl(String key, String name, double upgradeCost, Map<MineBlock, Integer> mineBlockWeights) {
      this.key = key;
      this.name = name;
      this.upgradeCost = upgradeCost;
      this.mineBlockWeights = mineBlockWeights;
   }

   @Override
   public Map<XMaterial, Integer> getBlockWeights() {
      Map<XMaterial, Integer> result = new LinkedHashMap<>();
      this.mineBlockWeights.forEach((block, weight) -> {
         if (!block.isCustom()) {
            result.put(block.getXMaterial(), weight);
         }
      });
      return result;
   }

   @Override
   public Map<String, Integer> getBlockWeightsAsStrings() {
      Map<String, Integer> result = new LinkedHashMap<>();
      this.mineBlockWeights.forEach((block, weight) -> result.put(block.serialize(), weight));
      return result;
   }

   @Generated
   @Override
   public String getKey() {
      return this.key;
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public double getUpgradeCost() {
      return this.upgradeCost;
   }

   @Generated
   public Map<MineBlock, Integer> getMineBlockWeights() {
      return this.mineBlockWeights;
   }
}
