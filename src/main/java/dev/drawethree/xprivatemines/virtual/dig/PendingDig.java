package dev.drawethree.xprivatemines.virtual.dig;

public record PendingDig(int x, int y, int z, long startNanos, int requiredTicks, int generation) {
   private static final long NANOS_PER_TICK = 50000000L;

   public boolean matchesPosition(int x, int y, int z) {
      return this.x == x && this.y == y && this.z == z;
   }

   public boolean isElapsed(double leniency) {
      if (this.requiredTicks == Integer.MAX_VALUE) {
         return false;
      } else {
         long elapsedNanos = System.nanoTime() - this.startNanos;
         return elapsedNanos >= (long)(this.requiredTicks * leniency * 5.0E7);
      }
   }
}
