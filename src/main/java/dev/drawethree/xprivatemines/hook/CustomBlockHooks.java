package dev.drawethree.xprivatemines.hook;

import dev.drawethree.xprivatemines.XPrivateMines;
import java.util.List;

public final class CustomBlockHooks {
   private final ItemsAdderHook itemsAdderHook;
   private final NexoHook nexoHook;
   private final OraxenHook oraxenHook;
   private final List<CustomBlockHook> hooks;

   public CustomBlockHooks(XPrivateMines plugin) {
      this.nexoHook = new NexoHook(plugin);
      this.oraxenHook = new OraxenHook(plugin);
      this.itemsAdderHook = new ItemsAdderHook(plugin);
      this.hooks = List.of(this.nexoHook, this.oraxenHook, this.itemsAdderHook);
   }

   public CustomBlockHook owner(String configId) {
      for (CustomBlockHook hook : this.hooks) {
         if (hook.ownsId(configId)) {
            return hook;
         }
      }

      return this.itemsAdderHook;
   }

   public void registerAll() {
      for (CustomBlockHook hook : this.hooks) {
         hook.register();
      }
   }

   public ItemsAdderHook getItemsAdderHook() {
      return this.itemsAdderHook;
   }

   public NexoHook getNexoHook() {
      return this.nexoHook;
   }

   public OraxenHook getOraxenHook() {
      return this.oraxenHook;
   }
}
