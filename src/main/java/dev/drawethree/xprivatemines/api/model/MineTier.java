package dev.drawethree.xprivatemines.api.model;

import com.cryptomorin.xseries.XMaterial;
import java.util.Map;

public interface MineTier {
   String getKey();

   String getName();

   double getUpgradeCost();

   Map<XMaterial, Integer> getBlockWeights();

   Map<String, Integer> getBlockWeightsAsStrings();
}
