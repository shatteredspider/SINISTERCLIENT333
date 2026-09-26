package dev.sevenclient.util;

import dev.sevenclient.SevenClient;
import java.util.Locale;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_7923;

public final class TargetUtil {
   private TargetUtil() {
   }

   public static class_1309 find(double range, boolean enemiesOnly, boolean playersOnly) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 eye = mc.field_1724.method_33571();
         class_1309 best = null;
         double bestScore = Double.MAX_VALUE;

         for(class_1297 e : mc.field_1687.method_18112()) {
            if (e instanceof class_1309) {
               class_1309 living = (class_1309)e;
               if (e != mc.field_1724 && e.method_5805() && !e.method_31481() && (!living.method_5767() || e instanceof class_1657) && (!playersOnly || e instanceof class_1657)) {
                  if (e instanceof class_1657) {
                     class_1657 p = (class_1657)e;
                     if (p.method_7325()) {
                        continue;
                     }
                  }

                  if (!enemiesOnly || SevenClient.get().enemies.is(e)) {
                     double dist = eye.method_1022(e.method_33571());
                     if (!(dist > range)) {
                        float[] rot = Rotations.to(eye, nearestPoint(eye, e));
                        double dYaw = (double)Math.abs(Rotations.wrap(rot[0] - mc.field_1724.method_36454()));
                        double dPitch = (double)Math.abs(rot[1] - mc.field_1724.method_36455());
                        double angle = Math.sqrt(dYaw * dYaw + dPitch * dPitch);
                        double score = dist * (double)1.0F + angle * 0.045;
                        if (score < bestScore) {
                           bestScore = score;
                           best = living;
                        }
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static class_243 nearestPoint(class_243 eye, class_1297 e) {
      class_238 box = e.method_5829();
      return new class_243(clamp(eye.field_1352, box.field_1323, box.field_1320), clamp(eye.field_1351, box.field_1322, box.field_1325), clamp(eye.field_1350, box.field_1321, box.field_1324));
   }

   private static double clamp(double v, double lo, double hi) {
      return v < lo ? lo : (v > hi ? hi : v);
   }

   public static class_1297 crosshairEntity() {
      class_310 mc = class_310.method_1551();
      class_239 var2 = mc.field_1765;
      class_1297 var10000;
      if (var2 instanceof class_3966 ehr) {
         var10000 = ehr.method_17782();
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public static boolean isWeapon(class_1799 stack, String extraKeywords) {
      if (stack != null && !stack.method_7960()) {
         class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
         String path = id.method_12832().toLowerCase(Locale.ROOT);
         if (!path.endsWith("_sword") && (!path.endsWith("_axe") || path.endsWith("_pickaxe")) && !path.equals("trident") && !path.equals("mace") && !path.equals("bow") && !path.equals("crossbow")) {
            if (extraKeywords != null && !extraKeywords.isBlank()) {
               for(String kw : extraKeywords.split(",")) {
                  String k = kw.trim().toLowerCase(Locale.ROOT);
                  if (!k.isEmpty() && path.contains(k)) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean shieldUp(class_1297 e, int minUseTicks) {
      if (e instanceof class_1309 living) {
         if (!living.method_6039()) {
            return false;
         } else if (minUseTicks <= 0) {
            return true;
         } else {
            try {
               return living.method_75120(0.0F) >= (float)minUseTicks;
            } catch (Throwable var4) {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean isAxe(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         String path = class_7923.field_41178.method_10221(stack.method_7909()).method_12832().toLowerCase(Locale.ROOT);
         return path.endsWith("_axe") && !path.endsWith("_pickaxe");
      } else {
         return false;
      }
   }

   public static boolean isSword(class_1799 stack) {
      return stack != null && !stack.method_7960() ? class_7923.field_41178.method_10221(stack.method_7909()).method_12832().toLowerCase(Locale.ROOT).endsWith("_sword") : false;
   }
}
