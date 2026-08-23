package dev.drawethree.xprivatemines.gui.addons;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.addons.XPrivateMinesAddonMetadata;
import dev.drawethree.xprivatemines.api.addons.XPrivateMinesAddon;
import dev.drawethree.xprivatemines.utils.item.ItemStackBuilder;
import dev.drawethree.xprivatemines.utils.item.SkullUtils;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.Arrays;
import java.util.List;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddonManagerGUI extends Gui {
   public AddonManagerGUI(Player player) {
      super(player, 5, "X-PrivateMines - Addon Manager");
   }

   public void redraw() {
      this.clearItems();

      for (XPrivateMinesAddon addon : XPrivateMines.getInstance().getAddonManager().getAllAddons()) {
         this.addItem(this.createAddonItem(addon));
      }

      this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lBack").lore("&7Back to main gui.").build(this::close));
      this.setItem(
         44,
         ItemStackBuilder.of(SkullUtils.HELP_SKULL.clone())
            .name("&e&lNeed more help?")
            .lore("&7Right-Click to see plugin's Wiki", "&7Left-Click to join Discord Support.")
            .build((Runnable)(() -> {
               this.close();
               PlayerUtils.sendMessage(this.getPlayer(), " ");
               PlayerUtils.sendMessage(this.getPlayer(), "&eX-PrivateMines - Wiki");
               PlayerUtils.sendMessage(this.getPlayer(), "&7https://github.com/Drawethree/X-PrivateMines/wiki");
               PlayerUtils.sendMessage(this.getPlayer(), " ");
            }), () -> {
               this.close();
               PlayerUtils.sendMessage(this.getPlayer(), " ");
               PlayerUtils.sendMessage(this.getPlayer(), "&eX-PrivateMines - Discord");
               PlayerUtils.sendMessage(this.getPlayer(), "&7https://discord.gg/ZeSkmEC6mG");
               PlayerUtils.sendMessage(this.getPlayer(), " ");
            })
      );
   }

   private boolean isAddonEnabled(XPrivateMinesAddon addon) {
      return XPrivateMines.getInstance().getAddonManager().isAddonEnabled(addon);
   }

   private Item createAddonItem(XPrivateMinesAddon addon) {
      XPrivateMinesAddonMetadata addonMetadata = XPrivateMines.getInstance().getAddonManager().getMetadata(addon);
      return ItemStackBuilder.of(XMaterial.COMMAND_BLOCK.get()).name("&e&l" + addonMetadata.getName()).lore(this.getLore(addon)).build(() -> {
         if (this.isAddonEnabled(addon)) {
            XPrivateMines.getInstance().getAddonManager().disableAddon(addon);
         } else {
            XPrivateMines.getInstance().getAddonManager().enableAddon(addon);
         }

         this.redraw();
      });
   }

   private List<String> getLore(XPrivateMinesAddon addon) {
      XPrivateMinesAddonMetadata addonMetadata = XPrivateMines.getInstance().getAddonManager().getMetadata(addon);
      return Arrays.asList(
         "",
         "&7Name: &f" + addonMetadata.getName(),
         "&7Version: &f" + addonMetadata.getVersion(),
         "&7Author: &f" + addonMetadata.getAuthor(),
         "",
         "&7Status: " + (this.isAddonEnabled(addon) ? "&aEnabled" : "&cDisabled"),
         "",
         "&7Description:",
         "&f" + addonMetadata.getDescription(),
         "",
         "&7Click actions:",
         "&eClick to enable/disable",
         ""
      );
   }
}
