package dev.drawethree.xprivatemines.api.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PrivateMine {
   UUID getUuid();

   UUID getOwner();

   OfflinePlayer getOfflineOwner();

   Mine getMine();

   boolean isBanned(OfflinePlayer var1);

   List<OfflinePlayer> getBannedPlayers();

   Set<UUID> getBannedPlayersUUID();

   void setOwner(UUID var1);

   void teleport(Player var1);

   boolean isInMine(Location var1);

   boolean isInPrivateMine(Location var1);

   boolean isOwner(OfflinePlayer var1);

   boolean isOpen();

   boolean isOpenTo(OfflinePlayer var1);

   void setOpen(boolean var1);

   double getEntryFee();

   void setEntryFee(double var1);

   double getTax();

   void setTax(double var1);

   int getExpandLevel();

   void setExpandLevel(int var1);

   double getResetPercentage();

   void setResetPercentage(int var1);

   double getUnclaimedMoney();

   void setUnclaimedMoney(double var1);

   double getXOffset();

   double getZOffset();

   MineTier getTier();

   void setTier(MineTier var1);

   MinesSchematic getSchematic();

   Location getSpawnLocation();

   int getMineSize();

   List<Player> getPlayersInMine();

   List<Player> getPlayersInPrivateMine();

   String getMineName();

   void setMineName(String var1);

   String getMineMotd();

   void setMineMotd(String var1);

   default String getDisplayName() {
      String n = this.getMineName();
      if (n != null && !n.isBlank()) {
         return n;
      } else {
         OfflinePlayer o = this.getOfflineOwner();
         return o != null && o.getName() != null ? o.getName() + "'s Mine" : "Unknown Mine";
      }
   }
}
