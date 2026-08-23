package dev.drawethree.xprivatemines.utils.text;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.utils.compat.MinecraftVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

public class TextUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
   private static final Map<Character, String> CODES = Map.ofEntries(
      Map.entry('0', "black"),
      Map.entry('1', "dark_blue"),
      Map.entry('2', "dark_green"),
      Map.entry('3', "dark_aqua"),
      Map.entry('4', "dark_red"),
      Map.entry('5', "dark_purple"),
      Map.entry('6', "gold"),
      Map.entry('7', "gray"),
      Map.entry('8', "dark_gray"),
      Map.entry('9', "blue"),
      Map.entry('a', "green"),
      Map.entry('b', "aqua"),
      Map.entry('c', "red"),
      Map.entry('d', "light_purple"),
      Map.entry('e', "yellow"),
      Map.entry('f', "white"),
      Map.entry('l', "bold"),
      Map.entry('m', "strikethrough"),
      Map.entry('n', "underlined"),
      Map.entry('o', "italic"),
      Map.entry('r', "reset")
   );

   public static String applyColor(String message) {
      if (XPrivateMines.isUseMiniMessage()) {
         return legacyToMiniMessage(message);
      } else {
         if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_16)) {
            for (Matcher matcher = HEX_PATTERN.matcher(message); matcher.find(); matcher = HEX_PATTERN.matcher(message)) {
               String hex = matcher.group();
               ChatColor hexColor = ChatColor.of(hex);
               String before = message.substring(0, matcher.start());
               String after = message.substring(matcher.end());
               message = before + hexColor + after;
            }
         }

         return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
      }
   }

   public static Component toComponent(String message) {
      return MiniMessage.miniMessage().deserialize(applyColor(message));
   }

   public static List<String> applyColor(List<String> list) {
      List<String> returnVal = new ArrayList<>(list.size());
      list.forEach(s -> returnVal.add(applyColor(s)));
      return returnVal;
   }

   public static Component toItemComponent(String message) {
      return ((Builder)((Builder)Component.text().decoration(TextDecoration.ITALIC, false)).append(toComponent(message))).build();
   }

   public static List<Component> toItemComponent(List<String> message) {
      List<Component> components = new ArrayList<>(message.size());

      for (String line : message) {
         components.add(toItemComponent(line));
      }

      return components;
   }

   public static List<Component> toComponent(List<String> message) {
      List<Component> components = new ArrayList<>();

      for (String s : applyColor(message)) {
         components.add(MiniMessage.miniMessage().deserialize(s));
      }

      return components;
   }

   private static String legacyToMiniMessage(String input) {
      StringBuilder result = new StringBuilder();

      for (int i = 0; i < input.length(); i++) {
         char c = input.charAt(i);
         if ((c == '&' || c == 167) && i + 1 < input.length()) {
            char code = Character.toLowerCase(input.charAt(i + 1));
            if (CODES.containsKey(code)) {
               result.append("<").append(CODES.get(code)).append(">");
               i++;
               continue;
            }
         }

         result.append(c);
      }

      return result.toString();
   }

   private TextUtils() {
      throw new UnsupportedOperationException("Cannot instantiate");
   }
}
