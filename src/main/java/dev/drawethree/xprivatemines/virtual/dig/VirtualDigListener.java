package dev.drawethree.xprivatemines.virtual.dig;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.virtual.VirtualBreakDispatcher;
import dev.drawethree.xprivatemines.virtual.VirtualMineEngine;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.entity.Player;

public final class VirtualDigListener extends PacketListenerAbstract {
   private static final long ERROR_LOG_THROTTLE_MS = 10000L;
   private final VirtualMineEngine engine;
   private final VirtualBreakDispatcher dispatcher;
   private final AtomicLong lastErrorLog = new AtomicLong();

   public VirtualDigListener(VirtualMineEngine engine, VirtualBreakDispatcher dispatcher) {
      super(PacketListenerPriority.HIGH);
      this.engine = engine;
      this.dispatcher = dispatcher;
   }

   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == Client.PLAYER_DIGGING) {
         try {
            this.handleDigging(event);
         } catch (Throwable var3) {
            this.logThrottled(var3);
         }
      }
   }

   private void handleDigging(PacketReceiveEvent event) {
      WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
      DiggingAction action = wrapper.getAction();
      if (action == DiggingAction.START_DIGGING || action == DiggingAction.FINISHED_DIGGING || action == DiggingAction.CANCELLED_DIGGING) {
         Player player = (Player)event.getPlayer();
         if (player != null && this.engine.isMinesWorld(player.getWorld())) {
            Vector3i pos = wrapper.getBlockPosition();
            VirtualMineStore store = this.engine.storeAt(pos.getX(), pos.getY(), pos.getZ());
            if (store != null) {
               event.setCancelled(true);
               this.dispatcher.enqueueDig(new DigCommand(player, store, pos.getX(), pos.getY(), pos.getZ(), action, wrapper.getSequence()));
            }
         }
      }
   }

   private void logThrottled(Throwable t) {
      long now = System.currentTimeMillis();
      long last = this.lastErrorLog.get();
      if (now - last >= 10000L && this.lastErrorLog.compareAndSet(last, now)) {
         PrivateMinesLogger.warning("Packet-mines dig handling failed: " + t);
         t.printStackTrace();
      }
   }
}
