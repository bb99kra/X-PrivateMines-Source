package dev.drawethree.xprivatemines.virtual.dig;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import dev.drawethree.xprivatemines.virtual.VirtualMineStore;
import org.bukkit.entity.Player;

public record DigCommand(Player player, VirtualMineStore store, int x, int y, int z, DiggingAction action, int sequence) {
}
