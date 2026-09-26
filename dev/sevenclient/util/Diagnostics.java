package dev.sevenclient.util;

public final class Diagnostics {
   public static volatile long ticks = 0L;
   public static volatile long frames = 0L;
   public static volatile boolean frameSourceIsMixin = false;
   public static volatile float lastDt = 0.0F;
   public static volatile long attackEvents = 0L;
   public static volatile long hitFlickArmed = 0L;
   public static volatile String aimTarget = "none";
   public static volatile double lastYawStep = (double)0.0F;
   public static volatile double aimAuthority = (double)0.0F;
   public static volatile double aimFlick = (double)0.0F;
   public static volatile double aimSpin = (double)0.0F;
   public static volatile double aimError = (double)0.0F;
   public static volatile double aimUrgency = (double)1.0F;
   public static volatile double aimCoverage = (double)0.0F;
   public static volatile String lastError = "none";
   public static volatile String lastErrorModule = "";
   private static long windowStart = 0L;
   private static long windowFrames = 0L;
   public static volatile double fps = (double)0.0F;

   private Diagnostics() {
   }

   public static void countFrame() {
      ++frames;
      ++windowFrames;
      long now = System.currentTimeMillis();
      if (windowStart == 0L) {
         windowStart = now;
      }

      if (now - windowStart >= 500L) {
         fps = (double)windowFrames * (double)1000.0F / (double)(now - windowStart);
         windowStart = now;
         windowFrames = 0L;
      }

   }

   public static void error(String module, Throwable t) {
      lastErrorModule = module;
      StringBuilder sb = new StringBuilder(t.getClass().getSimpleName());
      if (t.getMessage() != null) {
         sb.append(": ").append(t.getMessage());
      }

      StackTraceElement[] st = t.getStackTrace();
      if (st.length > 0) {
         sb.append(" @ ").append(st[0].getClassName().replace("dev.sevenclient.", "")).append('.').append(st[0].getMethodName()).append(':').append(st[0].getLineNumber());
      }

      lastError = sb.toString();
   }
}
