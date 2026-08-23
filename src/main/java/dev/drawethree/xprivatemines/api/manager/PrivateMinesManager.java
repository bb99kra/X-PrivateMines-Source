package dev.drawethree.xprivatemines.api.manager;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprivatemines.api.model.MineTier;
import dev.drawethree.xprivatemines.api.model.MinesSchematic;
import dev.drawethree.xprivatemines.api.model.PrivateMine;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface PrivateMinesManager {
   CompletableFuture<PrivateMine> createPrivateMine(OfflinePlayer var1, MinesSchematic var2);

   void deleteMine(CommandSender var1, PrivateMine var2);

   PrivateMine getPrivateMine(OfflinePlayer var1);

   PrivateMine getMineById(UUID var1);

   PrivateMine getMineByOwner(UUID var1);

   PrivateMine getPrivateMineAtLocation(Location var1);

   Collection<PrivateMine> getAll();

   boolean isMinesReady();

   Collection<MinesSchematic> getAllSchematics();

   boolean forceExpand(CommandSender var1, PrivateMine var2, int var3);

   boolean isMaxExpand(PrivateMine var1);

   boolean forceUpgrade(CommandSender var1, PrivateMine var2, MineTier var3);

   boolean isMaxTier(PrivateMine var1);

   double getNextUpgradeCost(PrivateMine var1);

   boolean shouldReset(PrivateMine var1);

   void refill(PrivateMine var1);

   void pregen(CommandSender var1, MinesSchematic var2, int var3);

   boolean isPregenRunning();

   void stopPregen();

   int getPregenCompleted();

   int getPregenTotal();

   void banPlayer(PrivateMine var1, OfflinePlayer var2);

   void unbanPlayer(PrivateMine var1, OfflinePlayer var2);

   void kickPlayer(Player var1);

   void reassignMine(PrivateMine var1, OfflinePlayer var2);

   boolean upgradeMine(PrivateMine var1, Player var2);

   boolean expandMine(PrivateMine var1, Player var2);

   boolean setBlock(PrivateMine var1, XMaterial var2);

   boolean setBlock(PrivateMine var1, String var2);
}
