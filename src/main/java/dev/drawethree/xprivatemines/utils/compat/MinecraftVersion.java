package dev.drawethree.xprivatemines.utils.compat;

import lombok.Generated;
import org.bukkit.Bukkit;

public final class MinecraftVersion {
   private static final String serverVersion;
   private static final MinecraftVersion.V current;
   private static final int subversion;

   public static boolean equals(MinecraftVersion.V version) {
      return compareWith(version) == 0;
   }

   public static boolean olderThan(MinecraftVersion.V version) {
      return compareWith(version) < 0;
   }

   public static boolean newerThan(MinecraftVersion.V version) {
      return compareWith(version) > 0;
   }

   public static boolean atLeast(MinecraftVersion.V version) {
      return equals(version) || newerThan(version);
   }

   private static int compareWith(MinecraftVersion.V version) {
      try {
         return getCurrent().minorVersionNumber - version.minorVersionNumber;
      } catch (Throwable var2) {
         var2.printStackTrace();
         return 0;
      }
   }

   public static String getFullVersion() {
      return current.toString() + (subversion > 0 ? "." + subversion : "");
   }

   @Deprecated
   public static String getServerVersion() {
      return serverVersion.equals("craftbukkit") ? "" : serverVersion;
   }

   @Generated
   public static MinecraftVersion.V getCurrent() {
      return current;
   }

   @Generated
   public static int getSubversion() {
      return subversion;
   }

   static {
      String packageName = Bukkit.getServer() == null ? "" : Bukkit.getServer().getClass().getPackage().getName();
      String curr = packageName.substring(packageName.lastIndexOf(46) + 1);
      serverVersion = !"craftbukkit".equals(curr) && !"".equals(packageName) ? curr : "";
      String bukkitVersion = Bukkit.getServer().getBukkitVersion();
      String versionString = bukkitVersion.split("\\-")[0];
      String[] versions = versionString.split("\\.");
      int version = Integer.parseInt(versions[1]);
      current = version < 3 ? MinecraftVersion.V.v1_3_AND_BELOW : MinecraftVersion.V.parse(version);
      subversion = versions.length == 3 ? Integer.parseInt(versions[2]) : 0;
   }

   public static enum V {
      v1_22(22),
      v1_21(21),
      v1_20(20),
      v1_19(19),
      v1_18(18),
      v1_17(17),
      v1_16(16),
      v1_15(15),
      v1_14(14),
      v1_13(13),
      v1_12(12),
      v1_11(11),
      v1_10(10),
      v1_9(9),
      v1_8(8),
      v1_7(7),
      v1_6(6),
      v1_5(5),
      v1_4(4),
      v1_3_AND_BELOW(3);

      private final int minorVersionNumber;

      private V(int version) {
         this.minorVersionNumber = version;
      }

      private static MinecraftVersion.V parse(int number) {
         for (MinecraftVersion.V v : values()) {
            if (v.minorVersionNumber == number) {
               return v;
            }
         }

         return null;
      }

      @Override
      public String toString() {
         return "1." + this.minorVersionNumber;
      }
   }
}
