package dev.drawethree.xprivatemines.utils.player;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.text.TextUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import java.time.Duration;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PlayerUtils {
   public static void sendMessage(CommandSender commandSender, String message) {
      if (commandSender != null) {
         if (!(commandSender instanceof Player) || ((Player)commandSender).isOnline()) {
            message = TextUtils.applyColor(message);
            if (XPrivateMines.getInstance().isPlaceholderAPIEnabled() && commandSender instanceof Player player) {
               message = PlaceholderAPI.setPlaceholders(player, message);
            }

            if (XPrivateMines.isUseMiniMessage()) {
               Audience sender = XPrivateMines.getInstance().adventure().sender(commandSender);
               sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
            } else {
               commandSender.sendMessage(message);
            }
         }
      }
   }

   public static void sendMessage(CommandSender commandSender, List<String> message) {
      if (commandSender != null) {
         if (!(commandSender instanceof Player) || ((Player)commandSender).isOnline()) {
            Audience sender = XPrivateMines.getInstance().adventure().sender(commandSender);

            for (String s : message) {
               s = TextUtils.applyColor(s);
               if (XPrivateMines.getInstance().isPlaceholderAPIEnabled() && sender instanceof Player player) {
                  s = PlaceholderAPI.setPlaceholders(player, s);
               }

               if (XPrivateMines.isUseMiniMessage()) {
                  sender.sendMessage(MiniMessage.miniMessage().deserialize(s));
               } else {
                  commandSender.sendMessage(s);
               }
            }
         }
      }
   }

   public static void sendTitle(Player player, TitleMessage title) {
      if (player != null && player.isOnline()) {
         if (title.isEnabled()) {
            if (title.getTitle() != null && !title.getTitle().isBlank() && title.getSubtitle() != null && !title.getSubtitle().isBlank()) {
               String finalTitle = TextUtils.applyColor(title.getTitle());
               String finalSubtitle = TextUtils.applyColor(title.getSubtitle());
               if (XPrivateMines.getInstance().isPlaceholderAPIEnabled()) {
                  finalTitle = PlaceholderAPI.setPlaceholders(player, finalTitle);
                  finalSubtitle = PlaceholderAPI.setPlaceholders(player, finalSubtitle);
               }

               if (XPrivateMines.isUseMiniMessage()) {
                  Component titleC = MiniMessage.miniMessage().deserialize(finalTitle);
                  Component subtitleC = MiniMessage.miniMessage().deserialize(finalSubtitle);
                  Title fullTitle = Title.title(titleC, subtitleC, Times.times(Duration.ofSeconds(1L), Duration.ofSeconds(3L), Duration.ofSeconds(1L)));
                  Audience audience = XPrivateMines.getInstance().adventure().player(player);
                  audience.showTitle(fullTitle);
               } else {
                  player.sendTitle(finalTitle, finalSubtitle, title.getFadeIn(), title.getStay(), title.getFadeOut());
               }
            }
         }
      }
   }

   private PlayerUtils() {
      throw new UnsupportedOperationException("Cannot instantiate");
   }
}
