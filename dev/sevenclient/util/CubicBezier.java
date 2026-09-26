package dev.sevenclient.util;

public final class CubicBezier {
   private final double x1;
   private final double y1;
   private final double x2;
   private final double y2;
   public static final CubicBezier AIM = new CubicBezier(0.22, 0.61, 0.36, (double)1.0F);
   public static final CubicBezier SMOOTH = new CubicBezier(0.45, 0.05, 0.55, 0.95);

   public CubicBezier(double x1, double y1, double x2, double y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
   }

   private static double curve(double t, double a1, double a2) {
      double inv = (double)1.0F - t;
      return (double)3.0F * inv * inv * t * a1 + (double)3.0F * inv * t * t * a2 + t * t * t;
   }

   private static double slope(double t, double a1, double a2) {
      double inv = (double)1.0F - t;
      return (double)3.0F * inv * inv * a1 + (double)6.0F * inv * t * (a2 - a1) + (double)3.0F * t * t * ((double)1.0F - a2);
   }

   public double ease(double x) {
      if (x <= (double)0.0F) {
         return (double)0.0F;
      } else if (x >= (double)1.0F) {
         return (double)1.0F;
      } else {
         double t = x;

         for(int i = 0; i < 6; ++i) {
            double err = curve(t, this.x1, this.x2) - x;
            if (Math.abs(err) < 1.0E-6) {
               return curve(t, this.y1, this.y2);
            }

            double d = slope(t, this.x1, this.x2);
            if (Math.abs(d) < 1.0E-6) {
               break;
            }

            t -= err / d;
         }

         double lo = (double)0.0F;
         double hi = (double)1.0F;
         t = x;

         for(int i = 0; i < 24; ++i) {
            double v = curve(t, this.x1, this.x2);
            if (Math.abs(v - x) < 1.0E-6) {
               break;
            }

            if (v > x) {
               hi = t;
            } else {
               lo = t;
            }

            t = (lo + hi) * (double)0.5F;
         }

         return curve(t, this.y1, this.y2);
      }
   }
}
