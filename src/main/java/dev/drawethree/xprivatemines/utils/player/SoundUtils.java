package dev.drawethree.xprivatemines.utils.player;

import com.cryptomorin.xseries.XSound;
import java.util.EnumMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SoundUtils {
   private static final EnumMap<SoundUtils.SoundType, XSound> sounds = new EnumMap<>(SoundUtils.SoundType.class);

   public static void init(FileConfiguration config) {
      loadSound(config, SoundUtils.SoundType.CLICK, "sounds.click", XSound.UI_BUTTON_CLICK);
      loadSound(config, SoundUtils.SoundType.SUCCESS, "sounds.success", XSound.ENTITY_PLAYER_LEVELUP);
      loadSound(config, SoundUtils.SoundType.INFO, "sounds.info", XSound.BLOCK_NOTE_BLOCK_PLING);
      loadSound(config, SoundUtils.SoundType.ERROR, "sounds.error", XSound.BLOCK_NOTE_BLOCK_BASS);
      loadSound(config, SoundUtils.SoundType.TELEPORT, "sounds.teleport", XSound.ENTITY_ENDERMAN_TELEPORT);
      loadSound(config, SoundUtils.SoundType.OPEN, "sounds.open", XSound.BLOCK_WOODEN_DOOR_OPEN);
      loadSound(config, SoundUtils.SoundType.CLOSE, "sounds.close", XSound.BLOCK_WOODEN_DOOR_CLOSE);
      loadSound(config, SoundUtils.SoundType.UPGRADE, "sounds.upgrade", XSound.UI_TOAST_CHALLENGE_COMPLETE);
   }

   private static void loadSound(FileConfiguration config, SoundUtils.SoundType type, String path, XSound fallback) {
      String soundName = config.getString(path);
      XSound sound = XSound.matchXSound(soundName).orElse(fallback);
      sounds.put(type, sound);
   }

   private static void play(Player player, SoundUtils.SoundType type, float volume, float pitch) {
      if (player != null) {
         XSound sound = sounds.get(type);
         if (sound != null && sound.isSupported()) {
            sound.play(player, volume, pitch);
         }
      }
   }

   public static void playClick(Player player) {
      play(player, SoundUtils.SoundType.CLICK, 1.0F, 1.0F);
   }

   public static void playSuccess(Player player) {
      play(player, SoundUtils.SoundType.SUCCESS, 1.0F, 1.0F);
   }

   public static void playInfo(Player player) {
      play(player, SoundUtils.SoundType.INFO, 1.0F, 1.0F);
   }

   public static void playError(Player player) {
      play(player, SoundUtils.SoundType.ERROR, 1.0F, 0.5F);
   }

   public static void playTeleport(Player player) {
      play(player, SoundUtils.SoundType.TELEPORT, 1.0F, 1.0F);
   }

   public static void playOpen(Player player) {
      play(player, SoundUtils.SoundType.OPEN, 1.0F, 0.5F);
   }

   public static void playClose(Player player) {
      play(player, SoundUtils.SoundType.CLOSE, 1.0F, 0.5F);
   }

   public static void playUpgrade(Player player) {
      play(player, SoundUtils.SoundType.UPGRADE, 1.0F, 1.0F);
   }

   private static enum SoundType {
      CLICK,
      SUCCESS,
      INFO,
      ERROR,
      TELEPORT,
      OPEN,
      CLOSE,
      UPGRADE;
   }
}
