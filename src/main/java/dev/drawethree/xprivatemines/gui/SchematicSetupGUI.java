package dev.drawethree.xprivatemines.gui;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupSession;
import dev.drawethree.xprivatemines.mines.setup.SetupStep;
import dev.drawethree.xprivatemines.service.SchematicCreationService;
import dev.drawethree.xprivatemines.utils.gui.XPrivateMinesGui;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import dev.drawethree.xprivatemines.utils.wand.WandUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.lucko.helper.menu.Item;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SchematicSetupGUI extends XPrivateMinesGui {
   private static final int SLOT_INFO = 4;
   private static final int SLOT_WAND = 10;
   private static final int SLOT_BUILD = 11;
   private static final int SLOT_MINE = 12;
   private static final int SLOT_REGION = 13;
   private static final int SLOT_SPAWN = 14;
   private static final int SLOT_RESET = 15;
   private static final int SLOT_FLAGS = 16;
   private static final int SLOT_CREATE = 31;
   private static final int SLOT_CANCEL = 40;
   private final XPrivateMines plugin;

   public SchematicSetupGUI(XPrivateMines plugin, Player player) {
      super(player, 5, "&8&lSchematic Setup");
      this.plugin = plugin;
   }

   @Override
   public void redraw() {
      this.clearItems();
      this.fillWith(this.filler());
      SchematicSetupSession session = this.plugin.getSchematicSetupManager().getOrCreate(this.getPlayer());
      this.setItem(4, this.infoItem(session));
      this.setItem(10, this.wandItem());
      this.setItem(
         11,
         this.regionStepItem(
            SetupStep.BUILD,
            session.getBuildPos1(),
            session.getBuildPos2(),
            Material.GRASS_BLOCK,
            "Select the outer bounds of your whole build.",
            () -> this.capture(session, SetupStep.BUILD)
         )
      );
      this.setItem(
         12,
         this.regionStepItem(
            SetupStep.MINE,
            session.getMinePos1(),
            session.getMinePos2(),
            Material.DIAMOND_ORE,
            "Select the hollow inner area that gets filled with ore.",
            () -> this.capture(session, SetupStep.MINE)
         )
      );
      this.setItem(
         13,
         this.regionStepItem(
            SetupStep.REGION,
            session.getRegionPos1(),
            session.getRegionPos2(),
            Material.BEDROCK,
            "Select the protected zone enclosing the whole build.",
            () -> this.capture(session, SetupStep.REGION)
         )
      );
      this.setItem(14, this.pointStepItem(SetupStep.SPAWN, session.getSpawn(), Material.LIME_BED, "Stand where players should teleport to.", () -> {
         session.captureSpawn(this.getPlayer().getLocation());
         PlayerUtils.sendMessage(this.getPlayer(), "&aSpawn point captured. &7Next: &fReset &7(optional) or &fCreate&7.");
         SoundUtils.playSuccess(this.getPlayer());
         this.redraw();
      }));
      this.setItem(
         15, this.pointStepItem(SetupStep.RESET, session.getReset(), Material.RED_BED, "Where players are moved during a reset (defaults to spawn).", () -> {
            session.captureReset(this.getPlayer().getLocation());
            PlayerUtils.sendMessage(this.getPlayer(), "&aReset point captured.");
            SoundUtils.playSuccess(this.getPlayer());
            this.redraw();
         })
      );
      this.setItem(16, this.flagsItem(session));
      this.setItem(31, this.createItem(session));
      this.setItem(40, this.cancelItem());
   }

   private Item filler() {
      return ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").buildItem().build();
   }

   private Item infoItem(SchematicSetupSession session) {
      int required = (int)Arrays.stream(SetupStep.values()).filter(SetupStep::isRequired).count();
      int done = (int)Arrays.stream(SetupStep.values()).filter(s -> s.isRequired() && s.isComplete(session)).count();
      return ItemStackBuilder.of(Material.WRITABLE_BOOK)
         .name("&b&lSchematic Setup")
         .lore(
            "&7Progress: &f" + done + "&7/&f" + required + " &7required steps",
            "",
            "&71. Grab the &fwand &7and click two corners",
            "&72. Click &fBuild / Mine / Region &7to save them",
            "&73. Capture &fSpawn &7(and Reset)",
            "&74. Adjust &fRegion Flags &7if needed",
            "&75. Click &fCreate &7and name it",
            "",
            "&8X and Z become offsets; Y is placed from paste height."
         )
         .buildItem()
         .build();
   }

   private Item wandItem() {
      return ItemStackBuilder.of(Material.BLAZE_ROD)
         .name("&6&lGet Selection Wand")
         .lore("&7Left-click a block &8» &acorner 1", "&7Right-click a block &8» &acorner 2", "", "&eClick &7to receive the wand.")
         .build(() -> {
            this.getPlayer().getInventory().addItem(new ItemStack[]{WandUtil.createWand()});
            PlayerUtils.sendMessage(this.getPlayer(), "&aYou received the schematic wand. &7Click two corners, then reopen this menu.");
            SoundUtils.playSuccess(this.getPlayer());
            this.close();
         });
   }

   private Item regionStepItem(SetupStep step, Location p1, Location p2, Material doneMaterial, String hint, Runnable click) {
      boolean done = p1 != null && p2 != null;
      ItemStackBuilder builder = ItemStackBuilder.of(done ? doneMaterial : Material.RED_STAINED_GLASS_PANE)
         .name((done ? "&a&l✔ " : "&c&l✘ ") + step.getDisplayName());
      if (done) {
         builder.lore(
            "&7Corner 1: &f" + this.block(p1),
            "&7Corner 2: &f" + this.block(p2),
            "&7Size: &f" + this.dimensions(p1, p2),
            "",
            "&eClick &7to re-capture from your wand selection."
         );
      } else {
         builder.lore("&7" + hint, "", "&eClick &7to save your current wand selection here.");
      }

      return builder.build(click);
   }

   private Item pointStepItem(SetupStep step, Location location, Material doneMaterial, String hint, Runnable click) {
      boolean done = location != null;
      Material material = done ? doneMaterial : (step.isRequired() ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE);
      String prefix = done ? "&a&l✔ " : (step.isRequired() ? "&c&l✘ " : "&7&l• ");
      ItemStackBuilder builder = ItemStackBuilder.of(material).name(prefix + step.getDisplayName());
      if (done) {
         builder.lore("&7At: &f" + this.exact(location), "&7Facing: &f" + this.facing(location), "", "&eClick &7to re-capture your current position.");
      } else {
         builder.lore("&7" + hint, "", "&eStand there, then click.");
      }

      return builder.build(click);
   }

   private Item flagsItem(SchematicSetupSession session) {
      return ItemStackBuilder.of(Material.REDSTONE_TORCH)
         .name("&d&lRegion Flags")
         .lore(
            "&7Mine flags: &f" + session.getMineFlags().size(),
            "&7Protection flags: &f" + session.getRegionFlags().size(),
            "",
            "&8Seeded from your default schematic.",
            "&eClick &7to edit WorldGuard flags."
         )
         .build(() -> {
            this.setFallbackGui(null);
            SchematicFlagsGUI gui = new SchematicFlagsGUI(this.plugin, this.getPlayer(), session);
            gui.setFallbackGui(pl -> new SchematicSetupGUI(this.plugin, pl));
            gui.open();
         });
   }

   private Item createItem(SchematicSetupSession session) {
      List<SetupStep> missing = session.missingRequiredSteps();
      if (missing.isEmpty()) {
         return ItemStackBuilder.of(Material.EMERALD_BLOCK)
            .name("&a&l✔ Create Schematic")
            .lore(
               "&7Everything required is captured!", "&7Mine size: &f" + SchematicCreationService.computeMineSize(session), "", "&eClick &7to name & create."
            )
            .build(() -> this.promptCreate(session));
      } else {
         List<String> lore = new ArrayList<>();
         lore.add("&7Still missing:");

         for (SetupStep step : missing) {
            lore.add(" &c✘ &f" + step.getDisplayName());
         }

         lore.add("");
         lore.add("&cComplete the steps above first.");
         return ItemStackBuilder.of(Material.BARRIER)
            .name("&c&l✘ Create Schematic")
            .lore(lore)
            .build(
               () -> {
                  PlayerUtils.sendMessage(
                     this.getPlayer(), "&cComplete all required steps first: &f" + String.join(", ", missing.stream().map(SetupStep::getDisplayName).toList())
                  );
                  SoundUtils.playError(this.getPlayer());
               }
            );
      }
   }

   private Item cancelItem() {
      return ItemStackBuilder.of(Material.TNT)
         .name("&c&lCancel Setup")
         .lore("&7Discards everything you've captured.", "", "&eClick &7to cancel.")
         .build(() -> {
            this.plugin.getSchematicSetupManager().remove(this.getPlayer());
            PlayerUtils.sendMessage(this.getPlayer(), "&7Schematic setup cancelled.");
            SoundUtils.playClose(this.getPlayer());
            this.close();
         });
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void capture(SchematicSetupSession session, SetupStep step) {
      boolean captured = switch (step) {
         case BUILD -> session.captureBuild();
         case MINE -> session.captureMine();
         case REGION -> session.captureRegion();
         default -> false;
      };
      if (captured) {
         PlayerUtils.sendMessage(
            this.getPlayer(), "&a" + step.getDisplayName() + " captured &7(" + this.dimensions(this.activePos1(session), this.activePos2(session)) + ")."
         );
         SoundUtils.playSuccess(this.getPlayer());
      } else {
         PlayerUtils.sendMessage(this.getPlayer(), "&cNo wand selection yet — left/right-click two corners with the wand first.");
         SoundUtils.playError(this.getPlayer());
      }

      this.redraw();
   }

   private Location activePos1(SchematicSetupSession session) {
      return session.getPos1();
   }

   private Location activePos2(SchematicSetupSession session) {
      return session.getPos2();
   }

   private void promptCreate(SchematicSetupSession session) {
      if (!session.allCapturedPointsShareWorld()) {
         PlayerUtils.sendMessage(this.getPlayer(), "&cAll captured points must be in the same world. Re-capture them in one world.");
         SoundUtils.playError(this.getPlayer());
      } else {
         this.setFallbackGui(null);
         this.getPlayer().closeInventory();
         PlayerUtils.sendMessage(this.getPlayer(), "&eType a name for your schematic in chat &7(letters, numbers, hyphens) &8— or type &fcancel&8.");
         TitleMessage prompt = new TitleMessage("schematic-name", true, "&6Name Your Schematic", "&7Type it in chat", 10, 60, 10);
         this.plugin.getChatInputManager().waitForInput(this.getPlayer(), prompt, input -> this.handleNameInput(session, input));
      }
   }

   private void handleNameInput(SchematicSetupSession session, String input) {
      this.plugin.getSchematicCreationService().createFromSession(this.getPlayer(), session, input);
   }

   private String block(Location location) {
      return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
   }

   private String exact(Location location) {
      return this.round(location.getX()) + ", " + this.round(location.getY()) + ", " + this.round(location.getZ());
   }

   private double round(double value) {
      return Math.round(value * 10.0) / 10.0;
   }

   private String dimensions(Location p1, Location p2) {
      if (p1 != null && p2 != null) {
         int x = Math.abs(p1.getBlockX() - p2.getBlockX()) + 1;
         int y = Math.abs(p1.getBlockY() - p2.getBlockY()) + 1;
         int z = Math.abs(p1.getBlockZ() - p2.getBlockZ()) + 1;
         return x + "×" + y + "×" + z;
      } else {
         return "?";
      }
   }

   private String facing(Location location) {
      float yaw = (location.getYaw() % 360.0F + 360.0F) % 360.0F;
      if (yaw >= 315.0F || yaw < 45.0F) {
         return "South";
      } else if (yaw < 135.0F) {
         return "West";
      } else {
         return yaw < 225.0F ? "North" : "East";
      }
   }
}
