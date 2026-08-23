package dev.drawethree.xprivatemines.gui.confirmation;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;

public class DeleteMineGui extends ConfirmationGui {
   private final PrivateMine mine;

   public DeleteMineGui(Player player, PrivateMine mine) {
      super(player, "Delete " + mine.getOfflineOwner().getName() + "'s Private Mine ?");
      this.mine = mine;
   }

   @Override
   public void confirm(boolean confirm) {
      if (confirm) {
         XPrivateMines.getInstance().getMinesManager().deleteMine(this.getPlayer(), this.mine);
         SoundUtils.playSuccess(this.getPlayer());
         if (this.getFallbackGui() != null) {
            ((Gui)this.getFallbackGui().apply(this.getPlayer())).redraw();
         }
      }

      this.close();
   }
}
