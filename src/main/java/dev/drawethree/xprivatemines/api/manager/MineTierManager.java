package dev.drawethree.xprivatemines.api.manager;

import dev.drawethree.xprivatemines.api.model.MineTier;
import java.util.List;
import java.util.Optional;

public interface MineTierManager {
   Optional<MineTier> getNextTier(String var1);

   Optional<MineTier> getNextTier(MineTier var1);

   List<MineTier> getAllTiers();

   MineTier getDefaultTier();

   MineTier getTier(String var1);
}
