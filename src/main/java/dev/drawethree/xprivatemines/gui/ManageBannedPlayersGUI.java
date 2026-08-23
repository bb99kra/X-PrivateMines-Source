package dev.drawethree.xprivatemines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.gui.config.GUIBannedPlayersConfig;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.chat.ChatInputManager;
import dev.drawethree.xprivatemines.utils.gui.XPrivateMinesGui;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.text.TitleMessage;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ManageBannedPlayersGUI extends XPrivateMinesGui {
   private final PrivateMineImpl mine;
   private final GUIBannedPlayersConfig config;

   public ManageBannedPlayersGUI(Player player, PrivateMineImpl mine) {
      super(
         player,
         XPrivateMines.getInstance().getGuiConfigLoader().getBannedPlayersGuiConfig().getRows(),
         XPrivateMines.getInstance().getGuiConfigLoader().getBannedPlayersGuiConfig().getTitle()
      );
      this.mine = mine;
      this.config = XPrivateMines.getInstance().getGuiConfigLoader().getBannedPlayersGuiConfig();
   }

   @Override
   public void redraw() {
      this.clearItems();
      int slot = 0;

      for (OfflinePlayer offlinePlayer : this.mine.getBannedPlayers()) {
         String playerName = offlinePlayer.getName();
         if (playerName == null) {
            playerName = "Unknown";
         }

         this.setItem(
            slot++,
            ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
               .name(this.config.getBannedPlayerName(playerName))
               .lore(this.config.getBannedPlayerLore())
               .build(
                  () -> {
                     XPrivateMines.getInstance().getMinesManager().unbanPlayer(this.mine, offlinePlayer);
                     PlayerUtils.sendMessage(
                        this.getPlayer(),
                        XPrivateMines.getInstance().getMessageConfig().getMessage("player-unbanned").replace("%player%", offlinePlayer.getName())
                     );
                     this.redraw();
                  }
               )
         );
      }

      if (this.config.isBanButtonEnabled()) {
         this.setItem(
            this.config.getBanButtonSlot(),
            ItemStackBuilder.of(XMaterial.matchXMaterial(this.config.getBanButtonMaterial()).<Material>map(XMaterial::parseMaterial).orElse(Material.ANVIL))
               .name(this.config.getBanButtonName())
               .lore(this.config.getBanButtonLore())
               .build(
                  () -> {
                     Function<Player, XPrivateMinesGui> savedFallback = this.getFallbackGui();
                     this.setFallbackGui(null);
                     this.getPlayer().closeInventory();
                     ChatInputManager inputManager = XPrivateMines.getInstance().getChatInputManager();
                     TitleMessage titleMessage = XPrivateMines.getInstance().getMessageConfig().getTitle("ban-player");
                     inputManager.waitForInput(
                        this.getPlayer(),
                        titleMessage,
                        input -> {
                           OfflinePlayer target = Bukkit.getOfflinePlayer(input);
                           if (target.getUniqueId().equals(this.getPlayer().getUniqueId())) {
                              PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("cant-ban-yourself"));
                              SoundUtils.playError(this.getPlayer());
                           } else if (this.mine.getBannedPlayers().contains(target.getUniqueId())) {
                              PlayerUtils.sendMessage(this.getPlayer(), XPrivateMines.getInstance().getMessageConfig().getMessage("player-already-banned"));
                              SoundUtils.playError(this.getPlayer());
                           } else {
                              XPrivateMines.getInstance().getMinesManager().banPlayer(this.mine, target);
                              PlayerUtils.sendMessage(
                                 this.getPlayer(),
                                 XPrivateMines.getInstance().getMessageConfig().getMessage("player-banned").replace("%player%", target.getName())
                              );
                              SoundUtils.playSuccess(this.getPlayer());
                              this.setFallbackGui(savedFallback);
                              this.redraw();
                              this.open();
                           }
                        }
                     );
                  }
               )
         );
      }
   }
}
