package dev.drawethree.xprivatemines.utils.chat;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatInputManager implements Listener {
   private final HashMap<UUID, ChatInputManager.InputSession<?>> activeSessions = new HashMap<>();
   private final XPrivateMines plugin;

   public ChatInputManager(XPrivateMines plugin) {
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public boolean isWaiting(UUID uuid) {
      return this.activeSessions.containsKey(uuid);
   }

   public void waitForIntInput(Player player, TitleMessage title, int min, int max, Consumer<Integer> onSuccess, Runnable onFail) {
      this.waitForInput(player, title, input -> {
         try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
               onFail.run();
               return;
            }

            onSuccess.accept(value);
         } catch (NumberFormatException var6x) {
            onFail.run();
         }
      });
   }

   public void waitForDoubleInput(Player player, TitleMessage title, double min, double max, Consumer<Double> onSuccess, Runnable onFail) {
      this.waitForInput(player, title, input -> {
         try {
            double value = Double.parseDouble(input);
            if (value < min || value > max) {
               onFail.run();
               return;
            }

            onSuccess.accept(value);
         } catch (NumberFormatException var9) {
            onFail.run();
         }
      });
   }

   public void waitForInput(Player player, TitleMessage title, Consumer<String> onInput) {
      this.activeSessions.put(player.getUniqueId(), new ChatInputManager.InputSession(onInput));
      PlayerUtils.sendTitle(player, title);
      SoundUtils.playInfo(player);
   }

   public void cancelInput(Player player) {
      this.activeSessions.remove(player.getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChat(AsyncPlayerChatEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      if (this.activeSessions.containsKey(uuid)) {
         event.setCancelled(true);
         String message = event.getMessage().trim();
         if (message.equalsIgnoreCase("cancel")) {
            PlayerUtils.sendMessage(event.getPlayer(), "Input cancelled.");
            this.activeSessions.remove(uuid);
            SoundUtils.playError(event.getPlayer());
         } else {
            ChatInputManager.InputSession<?> session = this.activeSessions.remove(uuid);
            Bukkit.getScheduler().runTask(this.plugin, () -> session.handle(message));
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.activeSessions.remove(event.getPlayer().getUniqueId());
   }

   private static class InputSession<T> {
      private final Consumer<String> handler;

      public InputSession(Consumer<String> handler) {
         this.handler = handler;
      }

      public void handle(String input) {
         this.handler.accept(input);
      }
   }
}
