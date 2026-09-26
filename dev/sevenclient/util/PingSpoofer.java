package dev.sevenclient.util;

import dev.sevenclient.SevenClient;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_2535;
import net.minecraft.class_2827;

public final class PingSpoofer {
   public static volatile boolean active = false;
   public static volatile int baseMs = 45;
   public static volatile int varianceMs = 8;
   private static double current = (double)-1.0F;
   private static final Random RNG = new Random();
   public static volatile int lastDelay = 0;
   public static volatile double bias = (double)0.0F;
   public static volatile long delayed = 0L;
   private static final AtomicBoolean passthrough = new AtomicBoolean(false);
   private static ScheduledExecutorService scheduler;

   private PingSpoofer() {
   }

   private static synchronized ScheduledExecutorService scheduler() {
      if (scheduler == null || scheduler.isShutdown()) {
         scheduler = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = new Thread(r, "777-pingspoof");
            t.setDaemon(true);
            return t;
         });
      }

      return scheduler;
   }

   public static boolean intercept(class_2535 connection, class_2827 packet) {
      if (active && connection != null) {
         if (passthrough.get()) {
            return false;
         } else {
            int delay = nextDelay();
            lastDelay = delay;
            ++delayed;
            scheduler().schedule(() -> {
               try {
                  passthrough.set(true);
                  connection.method_10743(packet);
               } catch (Throwable failure) {
                  SevenClient.LOG.warn("Deferred keep-alive send failed", failure);
               } finally {
                  passthrough.set(false);
               }

            }, (long)delay, TimeUnit.MILLISECONDS);
            return true;
         }
      } else {
         return false;
      }
   }

   private static int nextDelay() {
      double v = (double)Math.max(0, varianceMs);
      if (current < (double)0.0F) {
         current = (double)baseMs;
      }

      double target = (double)baseMs + bias * v * 0.55;
      double skew = bias >= (double)0.0F ? (double)1.0F : 0.72;
      current = current + (target - current) * 0.35 + RNG.nextGaussian() * v * 0.45 * skew;
      double lo = Math.max((double)0.0F, (double)baseMs - v);
      double hi = (double)baseMs + v;
      if (current < lo) {
         current = lo;
      }

      if (current > hi) {
         current = hi;
      }

      return (int)Math.round(current);
   }

   public static void shutdown() {
      if (scheduler != null) {
         scheduler.shutdownNow();
         scheduler = null;
      }

   }
}
