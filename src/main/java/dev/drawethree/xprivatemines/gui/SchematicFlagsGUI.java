package dev.drawethree.xprivatemines.gui;

import org.codemc.worldguardwrapper.flag.WrappedState;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupSession;
import dev.drawethree.xprivatemines.utils.gui.XPrivateMinesGui;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.util.List;
import java.util.Map;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SchematicFlagsGUI extends XPrivateMinesGui {
   private static final List<String> FLAGS = List.of(
      "block-break", "block-place", "build", "pvp", "mob-spawning", "tnt", "creeper-explosion", "interact", "use", "entry", "exit"
   );
   private static final int[] MINE_SLOTS = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38};
   private static final int[] REGION_SLOTS = new int[]{14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42};
   private static final int SLOT_MINE_LABEL = 2;
   private static final int SLOT_REGION_LABEL = 6;
   private static final int SLOT_BACK = 49;
   private final XPrivateMines plugin;
   private final SchematicSetupSession session;

   public SchematicFlagsGUI(XPrivateMines plugin, Player player, SchematicSetupSession session) {
      super(player, 6, "&8&lRegion Flags");
      this.plugin = plugin;
      this.session = session;
   }

   @Override
   public void redraw() {
      this.clearItems();
      this.fillWith(this.filler());
      this.setItem(2, this.label("&a&lMine Region", "&7Flags for the mineable inner area."));
      this.setItem(6, this.label("&c&lProtection Region", "&7Flags for the protective outer zone."));

      for (int i = 0; i < FLAGS.size(); i++) {
         String flag = FLAGS.get(i);
         this.setItem(MINE_SLOTS[i], this.flagItem(flag, this.session.getMineFlags()));
         this.setItem(REGION_SLOTS[i], this.flagItem(flag, this.session.getRegionFlags()));
      }

      this.setItem(49, ItemStackBuilder.of(Material.ARROW).name("&e&lBack").lore("&7Return to the setup board.").build(this::close));
   }

   private Item filler() {
      return ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").buildItem().build();
   }

   private Item label(String name, String lore) {
      return ItemStackBuilder.of(Material.PAPER).name(name).lore(lore).buildItem().build();
   }

   private Item flagItem(String flag, Map<String, WrappedState> flags) {
      WrappedState state = flags.get(flag);
      Material material;
      String stateLabel;
      if (state == WrappedState.ALLOW) {
         material = Material.LIME_DYE;
         stateLabel = "&aALLOW";
      } else if (state == WrappedState.DENY) {
         material = Material.RED_DYE;
         stateLabel = "&cDENY";
      } else {
         material = Material.GRAY_DYE;
         stateLabel = "&7NONE (default)";
      }

      return ItemStackBuilder.of(material)
         .name("&f" + flag)
         .lore("&7State: " + stateLabel, "", "&eClick &7to cycle &aALLOW &8» &cDENY &8» &7NONE")
         .build(() -> {
            this.cycle(flag, flags);
            SoundUtils.playClick(this.getPlayer());
            this.redraw();
         });
   }

   private void cycle(String flag, Map<String, WrappedState> flags) {
      WrappedState state = flags.get(flag);
      if (state == null) {
         flags.put(flag, WrappedState.ALLOW);
      } else if (state == WrappedState.ALLOW) {
         flags.put(flag, WrappedState.DENY);
      } else {
         flags.remove(flag);
      }
   }
}
