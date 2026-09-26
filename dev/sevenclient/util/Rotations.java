package dev.sevenclient.util;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public final class Rotations {
   private Rotations() {
   }

   public static double gcd() {
      double sens = (Double)class_310.method_1551().field_1690.method_42495().method_41753();
      double f = sens * 0.6 + 0.2;
      return f * f * f * (double)8.0F * 0.15;
   }

   public static float snap(double delta, double[] residual, int axis) {
      double step = gcd();
      if (step <= (double)0.0F) {
         return (float)delta;
      } else {
         double wanted = delta + residual[axis];
         double counts = Math.floor(Math.abs(wanted) / step) * Math.signum(wanted);
         double applied = counts * step;
         residual[axis] = wanted - applied;
         double cap = step * (double)8.0F;
         if (residual[axis] > cap) {
            residual[axis] = cap;
         }

         if (residual[axis] < -cap) {
            residual[axis] = -cap;
         }

         return (float)applied;
      }
   }

   public static float wrap(float degrees) {
      return class_3532.method_15393(degrees);
   }

   public static float[] to(class_243 from, class_243 to) {
      double dx = to.field_1352 - from.field_1352;
      double dy = to.field_1351 - from.field_1351;
      double dz = to.field_1350 - from.field_1350;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - (double)90.0F);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizontal)));
      return new float[]{wrap(yaw), class_3532.method_15363(pitch, -90.0F, 90.0F)};
   }

   public static float[] angularWindow(class_243 eye, class_1297 target, double inset, float currentYaw) {
      return angularWindow(eye, target.method_5829(), inset, currentYaw);
   }

   public static float[] angularWindow(class_243 eye, class_238 raw, double inset, float currentYaw) {
      class_238 box = raw.method_1014(-inset);
      if (box.method_17939() <= (double)0.0F || box.method_17940() <= (double)0.0F || box.method_17941() <= (double)0.0F) {
         box = raw;
      }

      float minYaw = Float.MAX_VALUE;
      float maxYaw = -Float.MAX_VALUE;
      float minPitch = Float.MAX_VALUE;
      float maxPitch = -Float.MAX_VALUE;
      double[] xs = new double[]{box.field_1323, box.field_1320};
      double[] ys = new double[]{box.field_1322, box.field_1325};
      double[] zs = new double[]{box.field_1321, box.field_1324};

      for(double x : xs) {
         for(double y : ys) {
            for(double z : zs) {
               float[] rot = to(eye, new class_243(x, y, z));
               float relYaw = currentYaw + wrap(rot[0] - currentYaw);
               minYaw = Math.min(minYaw, relYaw);
               maxYaw = Math.max(maxYaw, relYaw);
               minPitch = Math.min(minPitch, rot[1]);
               maxPitch = Math.max(maxPitch, rot[1]);
            }
         }
      }

      return new float[]{minYaw, maxYaw, minPitch, maxPitch};
   }
}
