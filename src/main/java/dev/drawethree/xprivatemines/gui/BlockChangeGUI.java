package dev.drawethree.xprivatemines.gui;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XItemFlag;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.gui.config.GUIBlockChangeConfig;
import dev.drawethree.xprivatemines.manager.CooldownManager;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.mines.model.block.MineBlock;
import dev.drawethree.xprivatemines.utils.cooldown.CooldownType;
import dev.drawethree.xprivatemines.utils.gui.XPrivateMinesGui;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.util.List;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class BlockChangeGUI extends XPrivateMinesGui {
   private final PrivateMineImpl mine;
   private final GUIBlockChangeConfig config;

   public BlockChangeGUI(Player player, PrivateMineImpl mine) {
      super(
         player,
         XPrivateMines.getInstance().getGuiConfigLoader().getBlockChangeGuiConfig().getRows(),
         XPrivateMines.getInstance().getGuiConfigLoader().getBlockChangeGuiConfig().getTitle()
      );
      this.mine = mine;
      this.config = XPrivateMines.getInstance().getGuiConfigLoader().getBlockChangeGuiConfig();
   }

   @Override
   public void redraw() {
      this.clearItems();

      for (MineBlock block : XPrivateMines.getInstance().getPrivateMinesConfig().getBlocks()) {
         this.addItem(this.createGuiItem(block));
      }

      int backSlot = this.getHandle().getSize() / 9 * 9 - 1;
      this.setItem(backSlot, ItemStackBuilder.of(Material.BARRIER).name("<red>← Back").build(this::close));
   }

   private Item createGuiItem(MineBlock block) {
      Player player = this.getPlayer();
      boolean isSelected = block.matches(this.mine.getMineImpl().getSelectedBlock());
      boolean hasPermission = this.canChangeTo(block);
      String formattedName = block.displayName();
      ItemStackBuilder builder = ItemStackBuilder.of(block.toIcon()).name("&e&l" + formattedName);
      List<String> lore;
      if (isSelected) {
         lore = this.replaceLore(this.config.getSelectedLore(), block);
         builder.enchant((Enchantment)XEnchantment.UNBREAKING.get()).flag(XItemFlag.HIDE_ENCHANTS.get());
      } else if (hasPermission) {
         lore = this.replaceLore(this.config.getUnlockedLore(), block);
      } else {
         lore = this.replaceLore(this.config.getLockedLore(), block);
      }

      builder.lore(lore);
      return builder.build(
         () -> {
            if (hasPermission && !isSelected) {
               if (CooldownManager.INSTANCE.hasCooldown(CooldownType.BLOCK_CHANGE, player)) {
                  PlayerUtils.sendMessage(player, XPrivateMines.getInstance().getMessageConfig().getMessage("cant-change-block"));
                  SoundUtils.playError(player);
               } else {
                  this.mine.getMineImpl().setSelectedBlock(block);
                  XPrivateMines.getInstance().getMinesManager().refill(this.mine, player);
                  SoundUtils.playSuccess(player);
                  PlayerUtils.sendMessage(
                     this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("mine-block-changed").replace("%block%", formattedName)
                  );
                  this.redraw();
               }
            }
         }
      );
   }

   private List<String> replaceLore(List<String> lore, MineBlock block) {
      String formattedName = block.displayName();
      String perm = block.permissionNode();
      return lore.stream().map(line -> line.replace("%block%", formattedName).replace("%perm%", perm)).toList();
   }

   private boolean canChangeTo(MineBlock block) {
      return this.getPlayer().hasPermission("xprivatemines.block." + block.permissionNode());
   }
}
