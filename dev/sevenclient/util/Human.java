package dev.sevenclient.util;

import java.util.Random;

public final class Human {
   private static final Random RNG = new Random();
   public static final double SIG_SPEED;
   public static final double SIG_VAR;
   private static double arousal;
   private static double focus;
   private static long actEnd;
   private static long lastDecision;
   private static long lastTickMs;
   private static final double PRP_PEAK = (double)165.0F;
   private static final double PRP_TAU = (double)230.0F;
   private static final double AROUSAL_THETA = 0.08;
   private static final double AROUSAL_SIGMA = 0.3;
   private static final double FOCUS_THETA = 0.025;
   private static final double FOCUS_SIGMA = 0.15;

   private Human() {
   }

   public static void tick() {
      long now = System.currentTimeMillis();
      if (lastTickMs == 0L) {
         lastTickMs = now;
         lastDecision = now;
      } else {
         double dt = (double)(now - lastTickMs) / (double)1000.0F;
         lastTickMs = now;
         if (!(dt <= (double)0.0F)) {
            if (dt > (double)0.25F) {
               dt = (double)0.25F;
            }

            arousal = arousal + -0.08 * arousal * dt + 0.3 * Math.sqrt(dt) * RNG.nextGaussian();
            focus = focus + -0.025 * focus * dt + 0.15 * Math.sqrt(dt) * RNG.nextGaussian();
            if (arousal > (double)2.5F) {
               arousal = (double)2.5F;
            }

            if (arousal < (double)-2.5F) {
               arousal = (double)-2.5F;
            }

            if (focus > (double)2.5F) {
               focus = (double)2.5F;
            }

            if (focus < (double)-2.5F) {
               focus = (double)-2.5F;
            }
         }
      }

   }

   public static double arousal() {
      return arousal;
   }

   public static double focus01() {
      return (double)1.0F / ((double)1.0F + Math.exp(-focus));
   }

   public static double exGauss(double mu, double sigma, double tau) {
      double g = mu + sigma * RNG.nextGaussian();
      double e = -tau * Math.log((double)1.0F - RNG.nextDouble());
      return g + e;
   }

   public static long reaction(double mu, double sigma, double tau) {
      double muEff = mu * SIG_SPEED * ((double)1.0F + 0.18 * arousal);
      double sd = sigma * SIG_VAR;
      double tl = tau * SIG_VAR;
      double v = exGauss(muEff, sd, tl) + refractoryMs();
      double floor = mu * 0.45;
      return (long)Math.max(floor, v);
   }

   public static long motor() {
      return reaction((double)52.0F, (double)11.0F, (double)16.0F);
   }

   public static long tapHold() {
      double v = exGauss((double)58.0F * SIG_SPEED, (double)13.0F * SIG_VAR, (double)22.0F * SIG_VAR);
      return (long)Math.max((double)25.0F, Math.min((double)240.0F, v));
   }

   public static double refractoryMs() {
      long now = System.currentTimeMillis();
      if (now < actEnd) {
         return (double)165.0F;
      } else {
         long since = now - lastDecision;
         return since <= 0L ? (double)165.0F : (double)165.0F * Math.exp((double)(-since) / (double)230.0F);
      }
   }

   public static boolean busy() {
      return System.currentTimeMillis() < actEnd;
   }

   public static void act(long durationMs) {
      long now = System.currentTimeMillis();
      lastDecision = now;
      actEnd = Math.max(actEnd, now + Math.max(0L, durationMs));
   }

   public static boolean roll(double pct) {
      double load = refractoryMs() / (double)165.0F;
      double p = pct / (double)100.0F * ((double)1.0F - 0.35 * load) * ((double)1.0F - 0.1 * Math.max((double)0.0F, arousal));
      return RNG.nextDouble() < p;
   }

   public static boolean flat(double pct) {
      return RNG.nextDouble() * (double)100.0F < pct;
   }

   public static int toTicks(double ms) {
      double t = ms / (double)50.0F;
      int base = (int)Math.floor(t);
      double frac = t - (double)base;
      return RNG.nextDouble() < frac ? base + 1 : base;
   }

   public static int lateness() {
      double sharp = 0.62 - 0.1 * arousal;
      double one = 0.3;
      double r = RNG.nextDouble();
      if (r < sharp) {
         return 0;
      } else {
         return r < sharp + one ? 1 : 2;
      }
   }

   public static double uniform(double lo, double hi) {
      return lo + RNG.nextDouble() * (hi - lo);
   }

   public static double gauss(double sd) {
      return RNG.nextGaussian() * sd;
   }

   public static Random rng() {
      return RNG;
   }

   static {
      SIG_SPEED = 0.9 + RNG.nextDouble() * 0.22;
      SIG_VAR = 0.85 + RNG.nextDouble() * 0.35;
      arousal = (double)0.0F;
      focus = (double)0.0F;
      actEnd = 0L;
      lastDecision = 0L;
      lastTickMs = 0L;
   }
}
