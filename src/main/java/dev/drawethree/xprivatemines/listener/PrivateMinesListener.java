package dev.drawethree.xprivatemines.listener;

import dev.drawethree.xprison.api.bombs.events.BombExplodeEvent;
import dev.drawethree.xprison.api.currency.enums.ReceiveCause;
import dev.drawethree.xprison.api.currency.event.PlayerCurrencyReceiveEvent;
import dev.drawethree.xprison.api.shared.events.XPrisonBlockBreakEvent;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.api.events.MineBlockBreakEvent;
import dev.drawethree.xprivatemines.api.events.MineEnterEvent;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import dev.drawethree.xprivatemines.manager.CooldownManager;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.cooldown.CooldownType;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import dev.drawethree.xprivatemines.utils.player.SoundUtils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PrivateMinesListener {
   private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100L);
   private final XPrivateMines plugin;
   private final BreakLedger breakLedger = new BreakLedger();

   public PrivateMinesListener(XPrivateMines plugin) {
      this.plugin = plugin;
   }

   public void register() {
      this.subscribeToBlockBreakEvent();
      this.subscribeToCurrencyReceiveEvent();
      this.subscribeToBombExplodeEvent();
      this.subscribeToAreaEnchantBlockBreakEvent();
      this.subscribeToMineEnterEvent();
      this.subscribeToResetHotkey();
   }

   private void subscribeToResetHotkey() {
      Events.subscribe(PlayerDropItemEvent.class, EventPriority.LOWEST)
         .handler(
            e -> {
               if (this.plugin.getPrivateMinesConfig().isResetHotkeyEnabled()) {
                  Player player = e.getPlayer();
                  if (!this.plugin.getPrivateMinesConfig().isResetHotkeyRequireSneak() || player.isSneaking()) {
                     ItemStack dropped = e.getItemDrop().getItemStack();
                     if (dropped != null && dropped.getType().name().endsWith("_PICKAXE")) {
                        PrivateMineImpl mine = this.plugin.getMinesManager().getPrivateMineInternal(player);
                        if (mine != null) {
                           if (mine.isInPrivateMine(player.getLocation()) || mine.isInMine(player.getLocation())) {
                              if (!player.hasPermission("xprivatemines.command.reset")) {
                                 PlayerUtils.sendMessage(player, this.plugin.getMessageConfig().getMessage("no-perm"));
                              } else if (CooldownManager.INSTANCE.hasCooldown(CooldownType.MINE_RESET, player)) {
                                 e.setCancelled(true);
                                 PlayerUtils.sendMessage(
                                    player,
                                    this.plugin
                                       .getMessageConfig()
                                       .getMessage("mine-reset-cooldown")
                                       .replace("%time%", String.valueOf(CooldownManager.INSTANCE.getRemainingTime(CooldownType.MINE_RESET, player)))
                                 );
                                 SoundUtils.playError(player);
                              } else {
                                 e.setCancelled(true);
                                 this.plugin.getMinesManager().refill(mine, player);
                              }
                           }
                        }
                     }
                  }
               }
            }
         )
         .bindWith(this.plugin);
   }

   private void subscribeToMineEnterEvent() {
      Events.subscribe(MineEnterEvent.class, EventPriority.MONITOR).filter(e -> !e.isCancelled()).handler(e -> {
         PrivateMine mine = e.getMine();
         String motd = mine.getMineMotd();
         if (motd != null && !motd.isBlank()) {
            Player player = e.getPlayer();
            if (!mine.isOwner(player) || this.plugin.getPrivateMinesConfig().isMotdShownToOwner()) {
               String header = this.plugin.getMessageConfig().getMessage("mine-motd-header");
               PlayerUtils.sendMessage(player, header + motd);
            }
         }
      }).bindWith(this.plugin);
   }

   private void subscribeToCurrencyReceiveEvent() {
      if (this.plugin.isXPrisonEnabled()) {
         Events.subscribe(PlayerCurrencyReceiveEvent.class, EventPriority.LOW)
            .filter(EventFilters.ignoreCancelled())
            .filter(e -> e.getPlayerOnline() != null)
            .filter(e -> e.getCause() == ReceiveCause.MINING)
            .handler(e -> {
               Player player = e.getPlayerOnline();
               PrivateMine mine = this.plugin.getMinesManager().getPrivateMineAtLocation(player.getLocation());
               if (mine != null && !mine.isOwner(player)) {
                  double taxPercentage = mine.getTax();
                  if (!(taxPercentage <= 0.0)) {
                     BigDecimal originAmount = e.getAmountExact();
                     if (originAmount != null && originAmount.signum() > 0) {
                        BigDecimal taxAmount = originAmount.multiply(BigDecimal.valueOf(taxPercentage)).divide(ONE_HUNDRED, MathContext.DECIMAL128);
                        mine.setUnclaimedMoney(mine.getUnclaimedMoney() + taxAmount.doubleValue());
                        e.setAmountExact(originAmount.subtract(taxAmount));
                     }
                  }
               }
            })
            .bindWith(this.plugin);
      }
   }

   private void subscribeToBombExplodeEvent() {
      if (this.plugin.isXPrisonEnabled()) {
         Events.subscribe(BombExplodeEvent.class, EventPriority.LOW)
            .filter(EventFilters.ignoreCancelled())
            .handler(
               e -> {
                  List<Block> originalBlocks = e.getOriginalBlocks();
                  if (!originalBlocks.isEmpty()) {
                     List<Block> blocks = new ArrayList<>();
                     PrivateMine mine = null;

                     for (Block block : originalBlocks) {
                        if (mine == null) {
                           mine = this.plugin.getMinesManager().getPrivateMineAtLocation(block.getLocation());
                           if (mine == null) {
                              continue;
                           }

                           this.plugin
                              .debug(
                                 "PrivateMinesListener#subscribeToBombExplodeEvent (LOW event prio): Bomb explosion is happening in  PrivateMine "
                                    + mine.getUuid().toString()
                                    + " (owned by "
                                    + mine.getOfflineOwner().getName()
                                    + ")"
                              );
                        }

                        if (mine.isInMine(block.getLocation())) {
                           blocks.add(block);
                        }
                     }

                     if (mine == null) {
                        this.plugin
                           .debug(
                              "PrivateMinesListener#subscribeToBombExplodeEvent (LOW event prio): Explosion is not happening in any mine, no blocks will be affected."
                           );
                     } else {
                        e.addAffectedBlocks(blocks);
                        this.plugin
                           .debug(
                              "PrivateMinesListener#subscribeToBombExplodeEvent (LOW event prio): Adding "
                                 + blocks.size()
                                 + " to affected blocks, explosion is happening in private mine "
                                 + mine.getUuid().toString()
                                 + " (owned by "
                                 + mine.getOfflineOwner().getName()
                                 + ")"
                           );
                        this.countBlocksBroken(mine, blocks);
                     }
                  }
               }
            )
            .bindWith(this.plugin);
      }
   }

   private void subscribeToAreaEnchantBlockBreakEvent() {
      if (this.plugin.isXPrisonEnabled()) {
         Events.subscribe(XPrisonBlockBreakEvent.class, EventPriority.MONITOR).filter(e -> !e.isCancelled()).handler(e -> {
            if (!this.plugin.isPacketMinesActive()) {
               List<Block> blocks = e.getBlocks();
               if (blocks != null && !blocks.isEmpty()) {
                  if (blocks.size() != 1 || !this.breakLedger.isCounted(blocks.get(0))) {
                     PrivateMine mine = null;
                     List<Block> inMine = new ArrayList<>(blocks.size());

                     for (Block block : blocks) {
                        if (mine == null) {
                           mine = this.plugin.getMinesManager().getPrivateMineAtLocation(block.getLocation());
                           if (mine == null) {
                              continue;
                           }
                        }

                        if (mine.isInMine(block.getLocation())) {
                           inMine.add(block);
                        }
                     }

                     if (mine != null && !inMine.isEmpty()) {
                        this.countBlocksBroken(mine, inMine);
                     }
                  }
               }
            }
         }).bindWith(this.plugin);
      }
   }

   private void subscribeToBlockBreakEvent() {
      Events.subscribe(BlockBreakEvent.class, EventPriority.HIGH).filter(EventFilters.ignoreCancelled()).handler(event -> {
         PrivateMine mine = this.plugin.getMinesManager().getPrivateMineAtLocation(event.getBlock().getLocation());
         if (mine != null) {
            if (mine.isInMine(event.getBlock().getLocation())) {
               MineBlockBreakEvent mineBlockBreakEvent = new MineBlockBreakEvent(event.getPlayer(), mine, event.getBlock());
               Events.callSync(mineBlockBreakEvent);
               if (mineBlockBreakEvent.isCancelled()) {
                  event.setCancelled(true);
               } else {
                  this.countBlocksBroken(mine, Collections.singletonList(event.getBlock()));
               }
            }
         }
      }).bindWith(this.plugin);
   }

   private void countBlocksBroken(PrivateMine mine, List<Block> blocks) {
      List<Block> uncounted = this.breakLedger.takeUncounted(blocks);
      if (!uncounted.isEmpty()) {
         mine.getMine().handleBlockBreak(uncounted);
      }
   }
}
