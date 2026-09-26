package dev.sevenclient.util;

import net.minecraft.class_310;

public final class SprintGuard {
   private static final long MIN_GAP_MS = 320L;
   private static long holdUntil = 0L;
   private static long lastRelease = 0L;
   public static volatile long holds = 0L;
   public static volatile long refused = 0L;

   private SprintGuard() {
   }

   public static boolean hold(long now) {
      if (now < holdUntil) {
         return true;
      } else if (now - lastRelease < 320L) {
         ++refused;
         return false;
      } else if (!ActionBudget.claim("sprint", 320L, now)) {
         ++refused;
         return false;
      } else {
         holdUntil = now + (long)Math.max((double)90.0F, Math.min((double)220.0F, Human.exGauss((double)120.0F, (double)22.0F, (double)30.0F)));
         ++holds;
         return true;
      }
   }

   public static void tick(long now) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         if (now < holdUntil) {
            if (mc.field_1724.method_5624()) {
               mc.field_1724.method_5728(false);
            }
         } else if (holdUntil != 0L) {
            holdUntil = 0L;
            lastRelease = now;
         }

      }
   }

   public static boolean holding(long now) {
      return now < holdUntil;
   }

   public static void reset() {
      holdUntil = 0L;
      lastRelease = 0L;
   }

   public static String status() {
      return holds + " holds / " + refused + " refused";
   }
}
