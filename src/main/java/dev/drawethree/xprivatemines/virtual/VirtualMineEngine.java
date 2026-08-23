package dev.drawethree.xprivatemines.virtual;

import com.github.retrooper.packetevents.PacketEvents;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.log.PrivateMinesLogger;
import dev.drawethree.xprivatemines.virtual.bridge.BreakIntegration;
import dev.drawethree.xprivatemines.virtual.bridge.XPrisonBridgeActivator;
import dev.drawethree.xprivatemines.virtual.dig.VirtualDigListener;
import dev.drawethree.xprivatemines.virtual.render.BlockStateCache;
import dev.drawethree.xprivatemines.virtual.render.ChunkOverlayListener;
import dev.drawethree.xprivatemines.virtual.render.PacketEventsRenderer;
import dev.drawethree.xprivatemines.virtual.render.ViewerTracker;
import dev.drawethree.xprivatemines.virtual.render.VirtualRenderer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;

public final class VirtualMineEngine {
   private final XPrivateMines plugin;
   private final ViewerTracker tracker = new ViewerTracker();
   private final BlockStateCache stateCache = new BlockStateCache();
   private final VirtualRenderer renderer;
   private final VirtualBreakDispatcher dispatcher;
   private final Map<UUID, VirtualMineStore> storesByMine = new ConcurrentHashMap<>();
   private final Map<Long, List<VirtualMineStore>> chunkIndex = new ConcurrentHashMap<>();
   private final Map<UUID, VirtualMineStore> previousStores = new ConcurrentHashMap<>();
   private volatile boolean bridgeActive;
   private volatile int worldMinY;
   private volatile World minesWorld;
   private static final int MAX_OVERLAP_COLUMNS = 4096;

   public VirtualMineEngine(XPrivateMines plugin) {
      this.plugin = plugin;
      this.renderer = new PacketEventsRenderer(this.tracker, this.stateCache);
      this.dispatcher = new VirtualBreakDispatcher(plugin, this.renderer, this::getMinesWorld);
   }

   public void init() {
      this.minesWorld = this.plugin.getPrivateMinesConfig().getMinesWorld();
      this.worldMinY = this.minesWorld != null ? this.minesWorld.getMinHeight() : 0;
      PacketEvents.getAPI().getEventManager().registerListener(new ChunkOverlayListener(this, this.tracker, this.stateCache));
      PacketEvents.getAPI().getEventManager().registerListener(new VirtualDigListener(this, this.dispatcher));
      Events.subscribe(PlayerQuitEvent.class).handler(e -> {
         this.tracker.onPlayerRemoved(e.getPlayer().getUniqueId());
         this.dispatcher.onPlayerRemoved(e.getPlayer().getUniqueId());
      }).bindWith(this.plugin);
      Events.subscribe(PlayerChangedWorldEvent.class).handler(e -> {
         this.tracker.onPlayerRemoved(e.getPlayer().getUniqueId());
         this.dispatcher.onPlayerRemoved(e.getPlayer().getUniqueId());
      }).bindWith(this.plugin);
      Events.subscribe(WorldLoadEvent.class).filter(e -> e.getWorld().getName().equals(this.plugin.getPrivateMinesConfig().getMinesWorldName())).handler(e -> {
         this.minesWorld = e.getWorld();
         this.worldMinY = e.getWorld().getMinHeight();
      }).bindWith(this.plugin);
      int digCap = this.plugin.getPrivateMinesConfig().getPacketMaxDigsPerTick();
      Schedulers.sync().runRepeating(() -> this.dispatcher.drainDigs(digCap), 1L, 1L).bindWith(this.plugin);
      this.checkAllowFlight();
      this.startEvictionTaskIfConfigured();
      if (this.isXPrisonVirtualApiPresent()) {
         XPrisonBridgeActivator.activate(this.plugin, this);
      }

      PrivateMinesLogger.info("Packet-mines engine initialized (world: " + this.plugin.getPrivateMinesConfig().getMinesWorldName() + ").");
   }

   public void shutdown() {
      if (this.bridgeActive) {
         XPrisonBridgeActivator.deactivate();
         this.bridgeActive = false;
      }

      this.dispatcher.clearQueue();
      this.storesByMine.clear();
      this.chunkIndex.clear();
      this.tracker.clear();
   }

   public VirtualMineStore getOrCreateStore(PrivateMineImpl mine) {
      VirtualMineStore existing = this.storesByMine.get(mine.getUuid());
      if (existing != null) {
         return existing;
      } else {
         synchronized (this) {
            existing = this.storesByMine.get(mine.getUuid());
            if (existing != null) {
               return existing;
            } else if (mine.getMineImpl() != null && mine.getMineImpl().getRegion() != null) {
               ICuboidSelection selection = (ICuboidSelection)mine.getMineImpl().getRegion().getSelection();
               Location min = selection.getMinimumPoint();
               Location max = selection.getMaximumPoint();
               VirtualMineStore store = new VirtualMineStore(
                  mine, min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ()
               );
               this.storesByMine.put(mine.getUuid(), store);
               this.indexStore(store);
               mine.getMineImpl().setVirtualStore(store);
               this.plugin
                  .debug(
                     "Created virtual store for mine "
                        + mine.getUuid()
                        + " ("
                        + store.volume()
                        + " blocks, "
                        + estimateKiloBytes(store)
                        + " KB when materialized)"
                  );
               return store;
            } else {
               return null;
            }
         }
      }
   }

   public void onMinesLoaded(Collection<PrivateMineImpl> mines) {
      for (PrivateMineImpl mine : mines) {
         this.getOrCreateStore(mine);
      }

      PrivateMinesLogger.info("Packet-mines: registered " + this.storesByMine.size() + " virtual mine stores.");
   }

   public void onRegionChanged(PrivateMineImpl mine) {
      VirtualMineStore store = this.storesByMine.remove(mine.getUuid());
      if (store != null) {
         this.deindexStore(store);
         this.previousStores.put(mine.getUuid(), store);
         if (mine.getMineImpl() != null) {
            mine.getMineImpl().setVirtualStore(null);
         }
      }
   }

   public void clearPreviousFootprint(VirtualMineStore newStore) {
      VirtualMineStore old = this.previousStores.remove(newStore.getMine().getUuid());
      if (old != null) {
         this.renderer.clearArea(newStore, old.getMinX(), old.getMinY(), old.getMinZ(), old.getMaxX(), old.getMaxY(), old.getMaxZ());
      }
   }

   public VirtualMineStore storeAt(int x, int y, int z) {
      List<VirtualMineStore> stores = this.chunkIndex.get(chunkKey(x >> 4, z >> 4));
      if (stores == null) {
         return null;
      } else {
         for (VirtualMineStore store : stores) {
            if (store.contains(x, y, z)) {
               return store;
            }
         }

         return null;
      }
   }

   public VirtualMineStore storeAt(Location location) {
      return location.getWorld() != null && this.isMinesWorld(location.getWorld())
         ? this.storeAt(location.getBlockX(), location.getBlockY(), location.getBlockZ())
         : null;
   }

   public VirtualMineStore storeOverlapping(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      VirtualMineStore centre = this.storeAt(minX + maxX >> 1, minY + maxY >> 1, minZ + maxZ >> 1);
      if (centre != null) {
         return centre;
      } else {
         int minChunkX = minX >> 4;
         int maxChunkX = maxX >> 4;
         int minChunkZ = minZ >> 4;
         int maxChunkZ = maxZ >> 4;
         long columns = (long)(maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
         if (columns > 4096L) {
            return null;
         } else {
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
               for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                  for (VirtualMineStore store : this.storesInChunk(cx, cz)) {
                     if (store.getMinX() <= maxX
                        && store.getMaxX() >= minX
                        && store.getMinY() <= maxY
                        && store.getMaxY() >= minY
                        && store.getMinZ() <= maxZ
                        && store.getMaxZ() >= minZ) {
                        return store;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public List<VirtualMineStore> storesInChunk(int chunkX, int chunkZ) {
      List<VirtualMineStore> stores = this.chunkIndex.get(chunkKey(chunkX, chunkZ));
      return stores != null ? stores : List.of();
   }

   public boolean isMinesWorld(World world) {
      if (world == null) {
         return false;
      } else {
         World cached = this.minesWorld;
         return cached != null ? world == cached : world.getName().equals(this.plugin.getPrivateMinesConfig().getMinesWorldName());
      }
   }

   public World getMinesWorld() {
      World cached = this.minesWorld;
      return cached != null ? cached : this.plugin.getPrivateMinesConfig().getMinesWorld();
   }

   public int getWorldMinY() {
      return this.worldMinY;
   }

   public VirtualRenderer getRenderer() {
      return this.renderer;
   }

   public VirtualBreakDispatcher getDispatcher() {
      return this.dispatcher;
   }

   public ViewerTracker getTracker() {
      return this.tracker;
   }

   public boolean isBridgeActive() {
      return this.bridgeActive;
   }

   private boolean isXPrisonVirtualApiPresent() {
      if (!this.plugin.isXPrisonEnabled()) {
         return false;
      } else {
         try {
            Class.forName("dev.drawethree.xprison.api.virtualblocks.VirtualBlockProviders");
            return true;
         } catch (ClassNotFoundException var2) {
            PrivateMinesLogger.warning(
               "X-Prison is installed but too old for packet-mines integration (X-Prison 2026.3.0.0+ with the virtualblocks API is required). Enchants/autosell will NOT trigger on virtual mine blocks; falling back to standalone drops."
            );
            return false;
         }
      }
   }

   public void activateBridge(BreakIntegration integration) {
      this.dispatcher.setIntegration(integration);
      this.bridgeActive = integration != null && integration.isActive();
   }

   public void warmUp(VirtualPalette palette) {
      this.stateCache.warmUp(palette.blocks());
   }

   private void indexStore(VirtualMineStore store) {
      for (int cx = store.getMinX() >> 4; cx <= store.getMaxX() >> 4; cx++) {
         for (int cz = store.getMinZ() >> 4; cz <= store.getMaxZ() >> 4; cz++) {
            this.chunkIndex.computeIfAbsent(chunkKey(cx, cz), key -> new CopyOnWriteArrayList<>()).add(store);
         }
      }
   }

   private void deindexStore(VirtualMineStore store) {
      for (int cx = store.getMinX() >> 4; cx <= store.getMaxX() >> 4; cx++) {
         for (int cz = store.getMinZ() >> 4; cz <= store.getMaxZ() >> 4; cz++) {
            long key = chunkKey(cx, cz);
            List<VirtualMineStore> stores = this.chunkIndex.get(key);
            if (stores != null) {
               stores.remove(store);
               if (stores.isEmpty()) {
                  this.chunkIndex.remove(key, stores);
               }
            }
         }
      }
   }

   private void checkAllowFlight() {
      if (!Bukkit.getAllowFlight()) {
         PrivateMinesLogger.warning("================================================================");
         PrivateMinesLogger.warning("packet-mines is enabled but 'allow-flight' is FALSE in");
         PrivateMinesLogger.warning("server.properties. Players standing on virtual mine blocks WILL");
         PrivateMinesLogger.warning("be kicked for flying. Set allow-flight=true and restart.");
         PrivateMinesLogger.warning("================================================================");
      }
   }

   private void startEvictionTaskIfConfigured() {
      int minutes = this.plugin.getPrivateMinesConfig().getPacketStoreEvictionMinutes();
      if (minutes > 0) {
         Schedulers.async().runRepeating(() -> {
            for (VirtualMineStore store : this.storesByMine.values()) {
               if (store.isMaterialized() && !this.tracker.hasViewers(store)) {
                  store.evict();
                  this.plugin.debug("Evicted virtual store of viewer-less mine " + store.getMine().getUuid());
               }
            }
         }, minutes * 60L * 20L, minutes * 60L * 20L).bindWith(this.plugin);
         PrivateMinesLogger.info("Packet-mines store eviction enabled: every " + minutes + " minute(s) for viewer-less mines.");
      }
   }

   private static long chunkKey(int chunkX, int chunkZ) {
      return (long)chunkX << 32 | chunkZ & 4294967295L;
   }

   private static long estimateKiloBytes(VirtualMineStore store) {
      return store.volume() / 1024L;
   }
}
