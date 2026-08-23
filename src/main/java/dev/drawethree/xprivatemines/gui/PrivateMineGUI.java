package dev.drawethree.xprivatemines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.gui.config.GUIConfigLoader;
import dev.drawethree.xprivatemines.manager.CooldownManager;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.cooldown.CooldownType;
import dev.drawethree.xprivatemines.utils.gui.XPrivateMinesGui;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PrivateMineGUI extends XPrivateMinesGui {
   private final PrivateMineImpl mine;

   public PrivateMineGUI(Player player, PrivateMineImpl mine) {
      super(
         player,
         XPrivateMines.getInstance().getGuiConfigLoader().getRows("private-mine-gui"),
         XPrivateMines.getInstance().getGuiConfigLoader().getTitle("private-mine-gui")
      );
      this.mine = mine;
   }

   @Override
   public void redraw() {
      this.clearItems();
      this.fillWith(ItemStackBuilder.of(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).name(" ").buildItem().build());
      this.setConfigItem("private-mine-gui.items.change-block", () -> {
         this.openGui(new BlockChangeGUI(this.getPlayer(), this.mine));
         SoundUtils.playClick(this.getPlayer());
      });
      this.setConfigItem("private-mine-gui.items.claim-tax", () -> {
         this.close();
         Bukkit.dispatchCommand(this.getPlayer(), "pmine claim");
         SoundUtils.playSuccess(this.getPlayer());
      });
      this.setConfigItem("private-mine-gui.items.settings", () -> {
         this.openGui(new PrivateMineSettingsGUI(this.getPlayer(), this.mine));
         SoundUtils.playClick(this.getPlayer());
      });
      this.setConfigItem("private-mine-gui.items.expand", () -> {
         if (XPrivateMines.getInstance().getMinesManager().expandMine(this.mine, this.getPlayer())) {
            SoundUtils.playSuccess(this.getPlayer());
         } else {
            SoundUtils.playError(this.getPlayer());
         }

         this.close();
      });
      this.setConfigItem("private-mine-gui.items.upgrade", () -> {
         if (XPrivateMines.getInstance().getMinesManager().upgradeMine(this.mine, this.getPlayer())) {
            SoundUtils.playSuccess(this.getPlayer());
         } else {
            SoundUtils.playError(this.getPlayer());
         }

         this.close();
      });
      this.setConfigItem(
         "private-mine-gui.items.reset",
         () -> {
            if (CooldownManager.INSTANCE.hasCooldown(CooldownType.MINE_RESET, this.getPlayer())) {
               PlayerUtils.sendMessage(
                  this.getPlayer(),
                  XPrivateMines.getInstance()
                     .getMessageConfig()
                     .getMessage("mine-reset-cooldown")
                     .replace("%time%", String.valueOf(CooldownManager.INSTANCE.getRemainingTime(CooldownType.MINE_RESET, this.getPlayer())))
               );
               SoundUtils.playError(this.getPlayer());
               this.close();
            } else {
               XPrivateMines.getInstance().getMinesManager().refill(this.mine, this.getPlayer());
               SoundUtils.playSuccess(this.getPlayer());
               this.close();
            }
         }
      );
      this.setConfigItem("private-mine-gui.items.teleport", () -> {
         this.close();
         this.mine.teleport(this.getPlayer());
         SoundUtils.playTeleport(this.getPlayer());
      });
   }

   private void setConfigItem(String path, Runnable action) {
      GUIConfigLoader.GUIItemConfig itemCfg = XPrivateMines.getInstance().getGuiConfigLoader().getItem(path);
      if (itemCfg.enabled()) {
         MineBlock iconBlock = MineBlock.parse(itemCfg.material());
         ItemStack icon = iconBlock != null ? iconBlock.toIcon() : XMaterial.STONE.parseItem();
         List<String> lore = new ArrayList<>(itemCfg.lore());
         lore.replaceAll(s -> s.replace("%upgrade_level%", this.mine.getTier().getName()));
         lore.replaceAll(
            s -> s.replace(
               "%upgrade_cost%",
               XPrivateMines.getInstance()
                  .getEconomyManager()
                  .format(this.getPlayer(), XPrivateMines.getInstance().getMinesManager().getNextUpgradeCost(this.mine))
            )
         );
         lore.replaceAll(
            s -> s.replace(
               "%expand_level%", XPrivateMines.getInstance().getMinesManager().isMaxExpand(this.mine) ? "&c&lMAX" : String.valueOf(this.mine.getExpandLevel())
            )
         );
         lore.replaceAll(
            s -> s.replace(
               "%expand_cost%",
               XPrivateMines.getInstance().getEconomyManager().format(this.getPlayer(), this.mine.getSchematic().getSettings().getExpandCost())
            )
         );
         this.setItem(itemCfg.slot(), ItemStackBuilder.of(icon).name(itemCfg.name()).lore(lore).build(action));
      }
   }

   private void openGui(XPrivateMinesGui gui) {
      gui.setFallbackGui(player -> this);
      gui.open();
   }
}
