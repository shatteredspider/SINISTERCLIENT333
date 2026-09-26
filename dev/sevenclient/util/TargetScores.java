package dev.sevenclient.util;

import dev.sevenclient.SevenClient;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class TargetScores {
   private TargetScores() {
   }

   public static double yawScore(class_746 var0, class_243 var1, class_1309 var2) {
      float[] var3 = Rotations.to(var1, TargetUtil.nearestPoint(var1, var2));
      return (double)Math.abs(Rotations.wrap(var3[0] - var0.method_36454()));
   }

   public static class_1309 findYaw(double var0, boolean var2, boolean var3) {
      class_310 var4 = class_310.method_1551();
      if (var4.field_1724 != null && var4.field_1687 != null) {
         class_243 var5 = var4.field_1724.method_33571();
         float var6 = var4.field_1724.method_36454();
         class_1309 var7 = null;
         double var8 = Double.MAX_VALUE;

         for(class_1297 var11 : var4.field_1687.method_18112()) {
            if (var11 instanceof class_1309) {
               class_1309 var12 = (class_1309)var11;
               class_1657 var13;
               if (var11 != var4.field_1724 && var11.method_5805() && !var11.method_31481() && (!var12.method_5767() || var11 instanceof class_1657) && (!var3 || var11 instanceof class_1657) && (!(var11 instanceof class_1657) || !(var13 = (class_1657)var11).method_7325()) && (!var2 || SevenClient.get().enemies.is(var11)) && !(var5.method_1022(var11.method_33571()) > var0)) {
                  float[] var14 = Rotations.to(var5, TargetUtil.nearestPoint(var5, var11));
                  double var15 = (double)Math.abs(Rotations.wrap(var14[0] - var6));
                  if (var15 < var8) {
                     var8 = var15;
                     var7 = var12;
                  }
               }
            }
         }

         return var7;
      } else {
         return null;
      }
   }
}
