package dev.sevenclient.module.impl;

import dev.sevenclient.util.RotationSync;
import java.util.Random;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_746;

public final class SilentAimGlide {
   private SilentAimGlide() {
   }

   public static void approach(class_746 var0, double var1, double var3, double var5) {
      double var7 = var5;
      if (Double.isNaN(var5)) {
         var7 = 0.05;
      }

      var7 = Math.max(0.05, Math.min((double)1.0F, var7));
      RotationSync.applyHidden(var0, var1 * var7, var3 * var7);
   }

   public static double[] jitteredPoint(Random var0, class_243 var1, class_238 var2, double var3, double var5) {
      double var7 = var2.field_1322;
      double var9 = var2.field_1325;
      double var11 = var1.field_1352;
      double var13 = var1.field_1351;
      double var15 = var1.field_1350;
      double var17 = clamp(var11, var2.field_1323, var2.field_1320) + (var0.nextDouble() * (double)2.0F - (double)1.0F) * var5;
      double var19 = clamp(var15, var2.field_1321, var2.field_1324) + (var0.nextDouble() * (double)2.0F - (double)1.0F) * var5;
      double var21 = (var0.nextDouble() * (double)2.0F - (double)1.0F) * var5;
      double var23 = clamp(var13, var7, var9);
      double var25;
      if (var13 < var23) {
         var25 = var23 + var21 * (double)0.5F;
      } else {
         var25 = Math.min(var13, var9) - 0.275 + var21;
      }

      if (var25 < var7) {
         var25 = var7;
      } else if (var25 > var9) {
         var25 = var9;
      }

      double var27 = var17 - var11;
      double var29 = var25 - var13;
      double var31 = var19 - var15;
      double var33 = Math.sqrt(var27 * var27 + var29 * var29 + var31 * var31);
      if (var33 > var3 && var33 > 1.0E-6) {
         double var35 = var3 / var33;
         var17 = var11 + var27 * var35;
         var25 = var13 + var29 * var35;
         var19 = var15 + var31 * var35;
      }

      return new double[]{var17, var25, var19};
   }

   private static double clamp(double var0, double var2, double var4) {
      return var0 < var2 ? var2 : (var0 > var4 ? var4 : var0);
   }
}
