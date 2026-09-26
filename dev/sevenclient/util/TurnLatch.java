package dev.sevenclient.util;

public final class TurnLatch {
   public static final double ON = (double)115.0F;
   public static final double OFF = (double)40.0F;
   public static final double DWELL_MS = (double)90.0F;
   public static final double MAX_TRAVEL = (double)260.0F;
   public static final double BAIL = (double)300.0F;
   private int direction = 0;
   private boolean behind = false;
   private long behindSince = 0L;

   public int direction() {
      return this.direction;
   }

   public void reset() {
      this.direction = 0;
      this.behind = false;
      this.behindSince = 0L;
   }

   public double apply(double shortestYaw, double humanVelYaw, double targetDrift, double spin, long nowMs) {
      if (Math.abs(shortestYaw) > (double)115.0F) {
         if (!this.behind) {
            this.behind = true;
            this.behindSince = nowMs;
         }
      } else {
         this.behind = false;
         this.behindSince = 0L;
      }

      boolean sustained = this.behind && nowMs - this.behindSince >= 90L;
      boolean spinning = spin > 0.3 && Math.abs(humanVelYaw) > (double)150.0F && Math.abs(shortestYaw) > (double)100.0F;
      if (this.direction == 0 && (sustained || spinning)) {
         double d;
         if (Math.abs(humanVelYaw) > (double)45.0F) {
            d = Math.signum(humanVelYaw);
         } else if (Math.abs(targetDrift) > (double)30.0F) {
            d = Math.signum(targetDrift);
         } else {
            d = Math.signum(shortestYaw);
         }

         if (d == (double)0.0F) {
            d = (double)1.0F;
         }

         double travel = shortestYaw;
         if (d > (double)0.0F && shortestYaw < (double)0.0F) {
            travel = shortestYaw + (double)360.0F;
         }

         if (d < (double)0.0F && shortestYaw > (double)0.0F) {
            travel = shortestYaw - (double)360.0F;
         }

         if (Math.abs(travel) <= (double)260.0F) {
            this.direction = (int)d;
         }
      }

      if (this.direction == 0) {
         return shortestYaw;
      } else {
         double err = shortestYaw;
         if (this.direction > 0 && shortestYaw < (double)0.0F) {
            err = shortestYaw + (double)360.0F;
         }

         if (this.direction < 0 && err > (double)0.0F) {
            err -= (double)360.0F;
         }

         if (Math.abs(err) > (double)300.0F) {
            this.direction = 0;
            return shortestYaw;
         } else {
            if (Math.abs(err) < (double)40.0F) {
               this.direction = 0;
            }

            return err;
         }
      }
   }
}
