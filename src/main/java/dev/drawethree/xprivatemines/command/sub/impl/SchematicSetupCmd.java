package dev.drawethree.xprivatemines.command.sub.impl;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.gui.SchematicFlagsGUI;
import dev.drawethree.xprivatemines.gui.SchematicSetupGUI;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupManager;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupSession;
import dev.drawethree.xprivatemines.mines.setup.SetupStep;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.wand.WandUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SchematicSetupCmd extends PrivateMineSubCommand {
   private static final List<String> ACTIONS = List.of("wand", "build", "mine", "region", "spawn", "reset", "flags", "status", "create", "cancel");

   public SchematicSetupCmd(PrivateMineCommand command) {
      super(command, "schematic", "sch");
   }

   @Override
   public boolean execute(CommandSender sender, List<String> args) {
      if (sender instanceof Player player) {
         XPrivateMines plugin = this.getCommand().getPlugin();
         SchematicSetupManager manager = plugin.getSchematicSetupManager();
         if (args.isEmpty()) {
            new SchematicSetupGUI(plugin, player).open();
            return true;
         } else {
            SchematicSetupSession session = manager.getOrCreate(player);
            String action = args.get(0).toLowerCase();
            switch (action) {
               case "wand":
                  player.getInventory().addItem(new ItemStack[]{WandUtil.createWand()});
                  this.sendWandHelp(player);
                  new SchematicSetupGUI(plugin, player).open();
                  break;
               case "build":
                  this.captureRegion(player, session, SetupStep.BUILD, session.captureBuild());
                  break;
               case "mine":
                  this.captureRegion(player, session, SetupStep.MINE, session.captureMine());
                  break;
               case "region":
                  this.captureRegion(player, session, SetupStep.REGION, session.captureRegion());
                  break;
               case "spawn":
                  session.captureSpawn(player.getLocation());
                  PlayerUtils.sendMessage(player, "&aSpawn point captured.");
                  SoundUtils.playSuccess(player);
                  break;
               case "reset":
                  session.captureReset(player.getLocation());
                  PlayerUtils.sendMessage(player, "&aReset point captured.");
                  SoundUtils.playSuccess(player);
                  break;
               case "flags":
                  new SchematicFlagsGUI(plugin, player, session).open();
                  break;
               case "status":
                  this.sendStatus(player, session);
                  break;
               case "create":
                  if (args.size() < 2) {
                     PlayerUtils.sendMessage(player, "&cUsage: &f/pmine schematic create <name>");
                     return true;
                  }

                  plugin.getSchematicCreationService().createFromSession(player, session, args.get(1));
                  break;
               case "cancel":
                  manager.remove(player);
                  PlayerUtils.sendMessage(player, "&7Schematic setup cancelled.");
                  SoundUtils.playClose(player);
                  break;
               default:
                  return false;
            }

            return true;
         }
      } else {
         PlayerUtils.sendMessage(sender, "&cOnly players can use the schematic creator.");
         return true;
      }
   }

   private void captureRegion(Player player, SchematicSetupSession session, SetupStep step, boolean captured) {
      if (captured) {
         PlayerUtils.sendMessage(player, "&a" + step.getDisplayName() + " captured from your wand selection.");
         SoundUtils.playSuccess(player);
      } else {
         PlayerUtils.sendMessage(player, "&cNo wand selection yet — left/right-click two corners with the wand first.");
         SoundUtils.playError(player);
      }
   }

   private void sendWandHelp(Player player) {
      PlayerUtils.sendMessage(player, "&6&lSchematic Wand");
      PlayerUtils.sendMessage(player, "&71. &eLeft-click &7a block = corner 1, &eRight-click &7= corner 2");
      PlayerUtils.sendMessage(player, "&72. Click &fBuild / Mine / Region &7in the menu to save the selection");
      PlayerUtils.sendMessage(player, "&73. Capture &fSpawn&7, then click &aCreate");
   }

   private void sendStatus(Player player, SchematicSetupSession session) {
      PlayerUtils.sendMessage(player, "&6&lSchematic Setup — Status");

      for (SetupStep step : SetupStep.values()) {
         boolean done = step.isComplete(session);
         String mark = done ? "&a✔" : (step.isRequired() ? "&c✘" : "&7•");
         String suffix = step.isRequired() ? "" : " &8(optional)";
         PlayerUtils.sendMessage(player, " " + mark + " &f" + step.getDisplayName() + suffix);
      }

      if (session.isReadyToCreate()) {
         PlayerUtils.sendMessage(player, "&aReady! Run &f/pmine schematic create <name>");
      } else {
         String missing = session.missingRequiredSteps().stream().map(SetupStep::getDisplayName).collect(Collectors.joining(", "));
         PlayerUtils.sendMessage(player, "&7Still missing: &f" + missing);
      }
   }

   @Override
   public String getUsage() {
      return "&e⚠ [&6XPrivateMines&e] &c/pmine schematic [wand|build|mine|region|spawn|reset|flags|status|create <name>|cancel] &7~ &fIn-game schematic creator";
   }

   @Override
   public List<String> getTabComplete(List<String> args) {
      if (args.size() == 1) {
         String typing = args.get(0).toLowerCase();
         return ACTIONS.stream().filter(a -> a.startsWith(typing)).collect(Collectors.toList());
      } else {
         return List.of();
      }
   }
}
