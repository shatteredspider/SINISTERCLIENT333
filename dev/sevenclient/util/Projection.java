package dev.sevenclient.util;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public final class Projection {
   private Projection() {
   }

   public static View capture() {
      View v = new View();
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         v.sw = mc.method_22683().method_4486();
         v.sh = mc.method_22683().method_4502();

         try {
            v.fov = (double)(Integer)mc.field_1690.method_41808().method_41753();
         } catch (Throwable var3) {
            v.fov = (double)70.0F;
         }

         v.tickDelta = mc.method_61966().method_60637(false);
         v.origin = mc.field_1724.method_5836(v.tickDelta);
         v.yaw = mc.field_1724.method_36454();
         v.pitch = mc.field_1724.method_36455();
         v.valid = true;
         return v;
      } else {
         return v;
      }
   }

   public static double[] project(class_243 pos, View v) {
      if (!v.valid) {
         return null;
      } else {
         class_243 d = pos.method_1020(v.origin);
         class_243 f = class_243.method_1030(v.pitch, v.yaw).method_1029();
         class_243 r = class_243.method_1030(0.0F, v.yaw + 90.0F).method_1029();
         class_243 u = r.method_1036(f).method_1029();
         double zc = d.method_1026(f);
         if (zc < 0.05) {
            return null;
         } else {
            double xc = d.method_1026(r);
            double yc = d.method_1026(u);
            double tanHalf = Math.tan(Math.toRadians(v.fov) * (double)0.5F);
            double aspect = (double)v.sw / (double)Math.max(1, v.sh);
            return new double[]{(double)v.sw * (double)0.5F * ((double)1.0F + xc / zc / (tanHalf * aspect)), (double)v.sh * (double)0.5F * ((double)1.0F - yc / zc / tanHalf)};
         }
      }
   }

   public static class_238 lerpedBox(class_1297 e, View v) {
      class_238 box = e.method_5829();

      try {
         class_243 feet = new class_243(e.method_23317(), e.method_23318(), e.method_23321());
         return box.method_997(e.method_30950(v.tickDelta).method_1020(feet));
      } catch (Throwable var4) {
         return box;
      }
   }

   public static float[] bounds(class_1297 e, View v) {
      class_238 b = lerpedBox(e, v);
      double minX = Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double maxX = -Double.MAX_VALUE;
      double maxY = -Double.MAX_VALUE;
      boolean any = false;

      for(int i = 0; i < 8; ++i) {
         class_243 c = new class_243((i & 1) == 0 ? b.field_1323 : b.field_1320, (i & 2) == 0 ? b.field_1322 : b.field_1325, (i & 4) == 0 ? b.field_1321 : b.field_1324);
         double[] p = project(c, v);
         if (p != null) {
            any = true;
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
         }
      }

      if (any && !(maxX < (double)0.0F) && !(maxY < (double)0.0F) && !(minX > (double)v.sw) && !(minY > (double)v.sh)) {
         return new float[]{(float)minX, (float)minY, (float)maxX, (float)maxY};
      } else {
         return null;
      }
   }

   public static boolean occluded(class_1297 e) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 from = mc.field_1724.method_33571();
         class_243 to = e.method_33571();

         try {
            class_239 hit = mc.field_1687.method_17742(new class_3959(from, to, class_3960.field_17558, class_242.field_1348, class_3726.method_16194()));
            return hit != null && hit.method_17783() != class_240.field_1333;
         } catch (Throwable var5) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int hsb(float hue, float sat, float bri, int alpha) {
      float h = hue % 360.0F / 60.0F;
      int i = (int)Math.floor((double)h);
      float f = h - (float)i;
      float p = bri * (1.0F - sat);
      float q = bri * (1.0F - sat * f);
      float t = bri * (1.0F - sat * (1.0F - f));
      float r;
      float g;
      float b;
      switch (i % 6) {
         case 0:
            r = bri;
            g = t;
            b = p;
            break;
         case 1:
            r = q;
            g = bri;
            b = p;
            break;
         case 2:
            r = p;
            g = bri;
            b = t;
            break;
         case 3:
            r = p;
            g = q;
            b = bri;
            break;
         case 4:
            r = t;
            g = p;
            b = bri;
            break;
         default:
            r = bri;
            g = p;
            b = q;
      }

      return (alpha & 255) << 24 | (int)(r * 255.0F) << 16 | (int)(g * 255.0F) << 8 | (int)(b * 255.0F);
   }

   public static final class View {
      public class_243 origin;
      public float yaw;
      public float pitch;
      public int sw;
      public int sh;
      public double fov;
      public float tickDelta;
      public boolean valid;

      public View() {
         this.origin = class_243.field_1353;
         this.fov = (double)70.0F;
      }
   }
}
