package dev.sevenclient.util;

public final class SmoothNoise {
   private final long seed;

   public SmoothNoise(long seed) {
      this.seed = seed;
   }

   private double hash(long i) {
      long h = i * -7046029254386353131L + this.seed;
      h ^= h >>> 30;
      h *= -4658895280553007687L;
      h ^= h >>> 27;
      h *= -7723592293110705685L;
      h ^= h >>> 31;
      return (double)(h >>> 11) / (double)9.007199E15F * (double)2.0F - (double)1.0F;
   }

   public double at(double x) {
      long i = (long)Math.floor(x);
      double f = x - (double)i;
      double a = this.hash(i);
      double b = this.hash(i + 1L);
      double t = f * f * ((double)3.0F - (double)2.0F * f);
      return a + (b - a) * t;
   }

   public double fbm(double x) {
      return this.at(x) * 0.6 + this.at(x * 2.17 + 11.3) * 0.28 + this.at(x * 4.31 + 29.7) * 0.12;
   }
}
