package dev.drawethree.xprivatemines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.gui.config.GUIConfigLoader;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.gui.XPrivateMinesGui;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class PrivateMineSettingsGUI extends XPrivateMinesGui {
   private final PrivateMineImpl mine;

   public PrivateMineSettingsGUI(Player player, PrivateMineImpl mine) {
      super(
         player,
         XPrivateMines.getInstance().getGuiConfigLoader().getRows("private-mine-settings-gui"),
         XPrivateMines.getInstance().getGuiConfigLoader().getTitle("private-mine-settings-gui")
      );
      this.mine = mine;
   }

   @Override
   public void redraw() {
      this.clearItems();
      this.fillWith(ItemStackBuilder.of(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).name(" ").buildItem().build());
      GUIConfigLoader config = XPrivateMines.getInstance().getGuiConfigLoader();
      Map<String, String> placeholders = this.createPlaceholders();
      this.setConfigItem(config, "private-mine-settings-gui.items.toggle-open", placeholders, () -> {
         if (this.mine.isOpen()) {
            XPrivateMines.getInstance().getMinesManager().getAccessService().closePrivateMine(this.mine);
            PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("mine-close"));
            SoundUtils.playClose(this.getPlayer());
         } else {
            XPrivateMines.getInstance().getMinesManager().getAccessService().openPrivateMine(this.mine);
            PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("mine-open"));
            SoundUtils.playOpen(this.getPlayer());
         }

         this.redraw();
      });
      this.setConfigItem(config, "private-mine-settings-gui.items.manage-bans", placeholders, () -> {
         this.setFallbackGui(null);
         this.openGui(new ManageBannedPlayersGUI(this.getPlayer(), this.mine));
         SoundUtils.playClick(this.getPlayer());
      });
      this.setConfigItem(
         config,
         "private-mine-settings-gui.items.set-tax",
         placeholders,
         () -> {
            this.setFallbackGui(null);
            this.getPlayer().closeInventory();
            TitleMessage titleMessage = XPrivateMines.getInstance().getMessageConfig().getTitle("tax-percentage");
            XPrivateMines.getInstance()
               .getChatInputManager()
               .waitForDoubleInput(
                  this.getPlayer(),
                  titleMessage,
                  XPrivateMines.getInstance().getPrivateMinesConfig().getMinTax(),
                  XPrivateMines.getInstance().getPrivateMinesConfig().getMaxTax(),
                  value -> {
                     this.mine.setTax(value);
                     PlayerUtils.sendMessage(
                        this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("tax-set").replace("%tax%", String.format("%,.2f", value))
                     );
                     SoundUtils.playSuccess(this.getPlayer());
                     this.redraw();
                     this.open();
                  },
                  () -> PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("invalid-tax"))
               );
         }
      );
      this.setConfigItem(
         config,
         "private-mine-settings-gui.items.set-entry-fee",
         placeholders,
         () -> {
            this.setFallbackGui(null);
            this.getPlayer().closeInventory();
            TitleMessage titleMessage = XPrivateMines.getInstance().getMessageConfig().getTitle("entry-fee");
            XPrivateMines.getInstance()
               .getChatInputManager()
               .waitForDoubleInput(
                  this.getPlayer(),
                  titleMessage,
                  0.0,
                  Double.MAX_VALUE,
                  value -> {
                     this.mine.setEntryFee(value);
                     PlayerUtils.sendMessage(
                        this.getPlayer(),
                        XPrivateMines.getInstance()
                           .getMessageConfig()
                           .getMessage("fee-set")
                           .replace("%fee%", XPrivateMines.getInstance().getEconomyManager().format(this.getPlayer(), this.mine.getEntryFee()))
                     );
                     SoundUtils.playSuccess(this.getPlayer());
                     this.redraw();
                     this.open();
                  },
                  () -> PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("invalid-fee"))
               );
         }
      );
      this.setConfigItem(
         config,
         "private-mine-settings-gui.items.set-reset-percentage",
         placeholders,
         () -> {
            this.setFallbackGui(null);
            this.getPlayer().closeInventory();
            TitleMessage titleMessage = XPrivateMines.getInstance().getMessageConfig().getTitle("reset-percentage");
            XPrivateMines.getInstance()
               .getChatInputManager()
               .waitForIntInput(
                  this.getPlayer(),
                  titleMessage,
                  XPrivateMines.getInstance().getPrivateMinesConfig().getMinResetPercentage(),
                  XPrivateMines.getInstance().getPrivateMinesConfig().getMaxResetPercentage(),
                  value -> {
                     this.mine.setResetPercentage(value);
                     PlayerUtils.sendMessage(
                        this.getPlayer(),
                        XPrivateMines.getInstance().getMessageConfig().getMessage("reset-percentage-set").replace("%reset%", String.format("%,d", value))
                     );
                     SoundUtils.playSuccess(this.getPlayer());
                     this.redraw();
                     this.open();
                  },
                  () -> PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("invalid-reset-percentage"))
               );
         }
      );
   }

   private Map<String, String> createPlaceholders() {
      HashMap<String, String> placeholders = new HashMap<>();
      placeholders.put("status", this.mine.isOpen() ? "&a&lOPEN" : "&c&lCLOSED");
      placeholders.put("tax", String.valueOf(this.mine.getTax()));
      placeholders.put("fee", XPrivateMines.getInstance().getEconomyManager().format(this.getPlayer(), this.mine.getEntryFee()));
      placeholders.put("reset", String.valueOf(this.mine.getResetPercentage()));
      placeholders.put(
         "expand_level", XPrivateMines.getInstance().getMinesManager().isMaxExpand(this.mine) ? "&c&lMAX" : String.valueOf(this.mine.getExpandLevel())
      );
      placeholders.put(
         "expand_cost", XPrivateMines.getInstance().getEconomyManager().format(this.getPlayer(), this.mine.getSchematic().getSettings().getExpandCost())
      );
      return placeholders;
   }

   private void setConfigItem(GUIConfigLoader config, String path, Map<String, String> placeholders, Runnable action) {
      GUIConfigLoader.GUIItemConfig itemCfg = XPrivateMines.getInstance().getGuiConfigLoader().getItem(path);
      if (itemCfg.enabled()) {
         this.setItem(itemCfg.slot(), ItemStackBuilder.of(config.buildFromConfig(itemCfg, placeholders)).build(action));
      }
   }

   private void openGui(XPrivateMinesGui gui) {
      gui.setFallbackGui(player -> this);
      gui.open();
   }
}
