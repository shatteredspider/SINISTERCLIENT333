package dev.sevenclient.util;

import java.util.Random;

public final class HumanRandom {
   private static final Random RNG = new Random();

   private HumanRandom() {
   }

   public static double uniform(double min, double max) {
      return min + RNG.nextDouble() * (max - min);
   }

   public static int uniformInt(int min, int max) {
      return min + RNG.nextInt(Math.max(1, max - min + 1));
   }

   public static boolean chance(double percent) {
      return RNG.nextDouble() * (double)100.0F < percent;
   }

   public static double gauss(double sigma) {
      double g = RNG.nextGaussian();
      if (g > (double)3.0F) {
         g = (double)3.0F;
      }

      if (g < (double)-3.0F) {
         g = (double)-3.0F;
      }

      return g * sigma;
   }

   public static long reaction(double minMs, double maxMs) {
      if (maxMs <= minMs) {
         return (long)minMs;
      } else {
         double span = maxMs - minMs;
         double sample = Math.exp(RNG.nextGaussian() * 0.55) - (double)1.0F;
         double t = sample / (double)3.0F;
         if (t < (double)0.0F) {
            t = (double)0.0F;
         }

         if (t > (double)1.0F) {
            t = (double)1.0F;
         }

         return (long)(minMs + span * t);
      }
   }

   public static double tremor(double timeSeconds) {
      return Math.sin(timeSeconds * 6.7) * 0.55 + Math.sin(timeSeconds * 11.3 + 1.7) * 0.3 + Math.sin(timeSeconds * 19.1 + 0.4) * 0.15;
   }
}
