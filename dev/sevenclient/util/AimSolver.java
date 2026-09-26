package dev.sevenclient.util;

public final class AimSolver {
   private static final double SETTLE = 5.8;
   private double velocity;

   public void reset() {
      this.velocity = (double)0.0F;
   }

   public double velocity() {
      return this.velocity;
   }

   public static double fittsTime(double distanceDeg, double widthDeg, double a, double b) {
      double w = Math.max(0.35, widthDeg);
      double id = Math.log((double)2.0F * Math.abs(distanceDeg) / w + (double)1.0F) / 0.6931471805599453;
      return a + b * id;
   }

   public double step(double error, double movementTime, double feedForward, double external, double maxRate, double gain, float dt) {
      double omega = 5.8 / Math.max(0.035, movementTime);
      double accel = omega * omega * error - (double)2.0F * omega * (this.velocity + external - feedForward);
      this.velocity += accel * (double)dt;
      if (error != (double)0.0F && this.velocity != (double)0.0F && Math.signum(this.velocity) != Math.signum(error)) {
         this.velocity *= Math.exp((double)-250.0F * (double)dt);
      }

      if (gain < 0.999) {
         this.velocity *= Math.exp(-((double)1.0F - gain) * (double)9.0F * (double)dt);
      }

      if (this.velocity > maxRate) {
         this.velocity = maxRate;
      }

      if (this.velocity < -maxRate) {
         this.velocity = -maxRate;
      }

      double delta = this.velocity * (double)dt * gain;
      double cap = Math.abs(error) + Math.abs(feedForward * (double)dt) + 0.02;
      if (delta > cap) {
         delta = cap;
      }

      if (delta < -cap) {
         delta = -cap;
      }

      return delta;
   }
}
