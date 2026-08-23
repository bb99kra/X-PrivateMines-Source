package dev.drawethree.xprivatemines.listener;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.setup.SchematicSetupSession;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import dev.drawethree.xprivatemines.utils.wand.WandUtil;
import me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WandListener {
   private final XPrivateMines plugin;

   public WandListener(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void register() {
      Events.subscribe(PlayerInteractEvent.class, EventPriority.LOW)
         .filter(e -> e.getHand() == EquipmentSlot.HAND)
         .filter(e -> WandUtil.isWand(e.getItem()))
         .filter(e -> e.getClickedBlock() != null)
         .handler(this::handleInteract)
         .bindWith(this.plugin);
      Events.subscribe(BlockBreakEvent.class, EventPriority.LOW)
         .filter(e -> WandUtil.isWand(e.getPlayer().getInventory().getItemInMainHand()))
         .handler(e -> e.setCancelled(true))
         .bindWith(this.plugin);
      Events.subscribe(PlayerQuitEvent.class).handler(e -> this.plugin.getSchematicSetupManager().remove(e.getPlayer())).bindWith(this.plugin);
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void handleInteract(PlayerInteractEvent event) {
      event.setCancelled(true);
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      if (block != null) {
         SchematicSetupSession session = this.plugin.getSchematicSetupManager().getOrCreate(player);
         Location location = block.getLocation();
         switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
               session.setPos1(location);
               PlayerUtils.sendMessage(player, "&6[Wand] &aCorner 1 &7set to &f" + this.format(location) + " &8(" + location.getWorld().getName() + ")");
               this.announce(player, session);
               SoundUtils.playClick(player);
               break;
            case RIGHT_CLICK_BLOCK:
               session.setPos2(location);
               PlayerUtils.sendMessage(player, "&6[Wand] &aCorner 2 &7set to &f" + this.format(location) + " &8(" + location.getWorld().getName() + ")");
               this.announce(player, session);
               SoundUtils.playClick(player);
         }
      }
   }

   private void announce(Player player, SchematicSetupSession session) {
      if (session.hasActiveSelection()) {
         PlayerUtils.sendMessage(player, "&7Selection ready &8» &7open &e/pmine schematic &7and click a slot (Build / Mine / Region).");
      }
   }

   private String format(Location location) {
      return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
   }
}
