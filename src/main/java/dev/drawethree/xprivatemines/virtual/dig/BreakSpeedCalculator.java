package dev.drawethree.xprivatemines.virtual.dig;

import java.util.Map.Entry;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class BreakSpeedCalculator {
   private static final float CORRECT_TOOL_DIVISOR = 30.0F;
   private static final float INCORRECT_TOOL_DIVISOR = 100.0F;
   private static final float HASTE_BONUS_PER_LEVEL = 0.2F;
   private static final float FATIGUE_BASE = 0.3F;
   private static final int FATIGUE_MAX_LEVEL = 4;
   private static final float ENVIRONMENT_PENALTY = 5.0F;
   private static final PotionEffectType HASTE = resolveEffect("HASTE", "FAST_DIGGING");
   private static final PotionEffectType MINING_FATIGUE = resolveEffect("MINING_FATIGUE", "SLOW_DIGGING");

   private BreakSpeedCalculator() {
   }

   public static float damagePerTick(Player player, BlockData block) {
      float hardness = block.getMaterial().getHardness();
      ItemStack tool = player.getInventory().getItemInMainHand();
      boolean correctTool = isPickaxe(tool);
      float speed = toolSpeed(tool, correctTool);
      int hasteLevel = effectLevel(player, HASTE);
      int fatigueLevel = effectLevel(player, MINING_FATIGUE);
      boolean submerged = player.isInWater() && !hasAquaAffinity(player);
      boolean onGround = player.isOnGround();
      return damagePerTick(speed, hardness, hasteLevel, fatigueLevel, submerged, onGround, correctTool);
   }

   public static float damagePerTick(float toolSpeed, float hardness, int hasteLevel, int fatigueLevel, boolean submerged, boolean onGround) {
      return damagePerTick(toolSpeed, hardness, hasteLevel, fatigueLevel, submerged, onGround, true);
   }

   public static float damagePerTick(
      float toolSpeed, float hardness, int hasteLevel, int fatigueLevel, boolean submerged, boolean onGround, boolean correctTool
   ) {
      if (hardness < 0.0F) {
         return 0.0F;
      } else if (hardness == 0.0F) {
         return 1.0F;
      } else {
         float speed = toolSpeed;
         if (hasteLevel > 0) {
            speed = toolSpeed * (1.0F + 0.2F * hasteLevel);
         }

         if (fatigueLevel > 0) {
            speed *= (float)Math.pow(0.3F, Math.min(fatigueLevel, 4));
         }

         if (submerged) {
            speed /= 5.0F;
         }

         if (!onGround) {
            speed /= 5.0F;
         }

         return speed / hardness / (correctTool ? 30.0F : 100.0F);
      }
   }

   public static boolean isInstantBreak(Player player, BlockData block) {
      return player.getGameMode() == GameMode.CREATIVE ? true : damagePerTick(player, block) >= 1.0F;
   }

   public static int requiredTicks(Player player, BlockData block) {
      return requiredTicks(damagePerTick(player, block));
   }

   public static int requiredTicks(float damagePerTick) {
      if (damagePerTick <= 0.0F) {
         return Integer.MAX_VALUE;
      } else {
         return damagePerTick >= 1.0F ? 0 : (int)Math.ceil(1.0F / damagePerTick);
      }
   }

   private static float toolSpeed(ItemStack tool, boolean pickaxe) {
      if (!pickaxe) {
         return 1.0F;
      } else {
         float base = pickaxeTierSpeed(tool.getType());
         int efficiency = enchantLevel(tool, "efficiency");
         if (efficiency > 0) {
            base += efficiency * efficiency + 1;
         }

         return base;
      }
   }

   private static boolean isPickaxe(ItemStack tool) {
      return tool != null && tool.getType().name().endsWith("_PICKAXE");
   }

   private static float pickaxeTierSpeed(Material type) {
      String name = type.name();
      if (name.startsWith("WOODEN")) {
         return 2.0F;
      } else if (name.startsWith("STONE")) {
         return 4.0F;
      } else if (name.startsWith("IRON")) {
         return 6.0F;
      } else if (name.startsWith("DIAMOND")) {
         return 8.0F;
      } else if (name.startsWith("NETHERITE")) {
         return 9.0F;
      } else {
         return name.startsWith("GOLDEN") ? 12.0F : 2.0F;
      }
   }

   private static int effectLevel(Player player, PotionEffectType type) {
      if (type == null) {
         return 0;
      } else {
         PotionEffect effect = player.getPotionEffect(type);
         return effect == null ? 0 : effect.getAmplifier() + 1;
      }
   }

   private static boolean hasAquaAffinity(Player player) {
      ItemStack helmet = player.getInventory().getHelmet();
      return helmet != null && helmet.getType() != Material.AIR && enchantLevel(helmet, "aqua_affinity") > 0;
   }

   private static int enchantLevel(ItemStack item, String keyName) {
      if (item != null && item.hasItemMeta()) {
         for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            if (entry.getKey().getKey().getKey().equalsIgnoreCase(keyName)) {
               return entry.getValue();
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   private static PotionEffectType resolveEffect(String... names) {
      for (String name : names) {
         try {
            PotionEffectType type = PotionEffectType.getByName(name);
            if (type != null) {
               return type;
            }
         } catch (Throwable var6) {
         }
      }

      return null;
   }
}
