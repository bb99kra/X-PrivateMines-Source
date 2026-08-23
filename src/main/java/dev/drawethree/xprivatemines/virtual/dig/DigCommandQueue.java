package dev.drawethree.xprivatemines.virtual.dig;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class DigCommandQueue {
   private final ConcurrentLinkedQueue<DigCommand> queue = new ConcurrentLinkedQueue<>();
   private final AtomicInteger pending = new AtomicInteger();
   private final DigCommandQueue.Handler handler;
   private final int maxSize;

   public DigCommandQueue(DigCommandQueue.Handler handler) {
      this(handler, 0);
   }

   public DigCommandQueue(DigCommandQueue.Handler handler, int maxSize) {
      this.handler = handler;
      this.maxSize = maxSize;
   }

   public void offer(DigCommand command) {
      this.queue.offer(command);
      int current = this.pending.incrementAndGet();
      if (this.maxSize > 0 && current > this.maxSize && this.queue.poll() != null) {
         this.pending.decrementAndGet();
      }
   }

   public int drain(int cap) {
      int processed;
      DigCommand command;
      for (processed = 0; (cap <= 0 || processed < cap) && (command = this.queue.poll()) != null; processed++) {
         this.pending.decrementAndGet();
         this.handler.handle(command);
      }

      return processed;
   }

   public void clear() {
      while (this.queue.poll() != null) {
         this.pending.decrementAndGet();
      }
   }

   public boolean isEmpty() {
      return this.queue.isEmpty();
   }

   public int size() {
      return this.pending.get();
   }

   @FunctionalInterface
   public interface Handler {
      void handle(DigCommand var1);
   }
}
