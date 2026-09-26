package dev.sevenclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_310;

public final class PacketAudit {
   public static final double MINIMUM_DIVISOR = 0.008600000000000003;
   private static final int WINDOW = 80;
   private static final int MIN_SAMPLES = 15;
   private static final Deque<Double> gcdYawWindow = new ArrayDeque();
   private static final Deque<Double> gcdPitchWindow = new ArrayDeque();
   private static double lastYaw = Double.NaN;
   private static double lastPitch = Double.NaN;
   private static double lastDeltaYaw = (double)0.0F;
   private static double lastDeltaPitch = (double)0.0F;
   public static volatile double recoveredSens = (double)-1.0F;
   public static volatile double modeGcdYaw = (double)0.0F;
   public static volatile double worstDotError = (double)0.0F;
   public static volatile double lastDotError = (double)0.0F;
   public static volatile long offLatticeSamples = 0L;
   public static volatile long rotationSamples = 0L;
   public static volatile long useItemMismatch = 0L;
   public static volatile long duplicateSlot = 0L;
   public static volatile long duplicateSprint = 0L;
   public static volatile long duplicateSneak = 0L;
   public static volatile long modulo360 = 0L;
   public static volatile long duplicateLook = 0L;
   private static int lastSlotOnWire = -1;
   private static Boolean lastSprint = null;
   private static Boolean lastSneak = null;
   private static final Deque<Long> sprintFlips = new ArrayDeque();
   private static final Deque<Long> attacks = new ArrayDeque();
   public static volatile int attackRate = 0;
   public static volatile int sprintFlipRate = 0;
   private static volatile boolean forcedSprintResend = false;
   private static volatile boolean forcedSneakResend = false;

   private PacketAudit() {
   }

   public static void onFlyingRotation(double yaw, double pitch) {
      if (Double.isNaN(lastYaw)) {
         lastYaw = yaw;
         lastPitch = pitch;
      } else {
         double dYaw = (double)Math.abs(Rotations.wrap((float)(yaw - lastYaw)));
         double dPitch = Math.abs(pitch - lastPitch);
         if (dYaw != (double)0.0F || dPitch != (double)0.0F) {
            if ((double)Math.abs(Rotations.wrap((float)(yaw - lastYaw))) > (double)320.0F && Math.abs(lastDeltaYaw) < (double)30.0F) {
               ++modulo360;
            }

            feed(gcdYawWindow, dYaw, lastDeltaYaw);
            feed(gcdPitchWindow, dPitch, lastDeltaPitch);
            double mode = mode(gcdYawWindow);
            modeGcdYaw = mode;
            boolean stable = gcdYawWindow.size() >= 30;
            if (mode > 0.008600000000000003 && stable) {
               recoveredSens = (Math.cbrt(mode / 0.15 / (double)8.0F) - 0.2) / 0.6;
               if (dYaw > (double)0.0F) {
                  double trueStep = safeGcd();
                  double basis = trueStep > 0.008600000000000003 ? trueStep : mode;
                  double dots = dYaw / basis;
                  double err = Math.abs(dots - (double)Math.round(dots));
                  lastDotError = err;
                  if (err > worstDotError) {
                     worstDotError = err;
                  }

                  if (err > (double)0.25F) {
                     ++offLatticeSamples;
                  }
               }
            }

            ++rotationSamples;
            lastDeltaYaw = (double)Rotations.wrap((float)(yaw - lastYaw));
            lastDeltaPitch = pitch - lastPitch;
            lastYaw = yaw;
            lastPitch = pitch;
         }
      }
   }

   private static void feed(Deque<Double> window, double delta, double previous) {
      if (delta > (double)0.0F && delta < (double)5.0F && previous != (double)0.0F) {
         double g = gcd(delta, Math.abs(previous));
         if (g > 0.008600000000000003) {
            window.addLast(g);

            while(window.size() > 80) {
               window.removeFirst();
            }
         }
      }

   }

   private static double gcd(double a, double b) {
      double r;
      for(int guard = 0; b > 0.008600000000000003 && guard++ < 64; b = r) {
         r = a - Math.floor(a / b) * b;
         a = b;
      }

      return a;
   }

   private static double mode(Deque<Double> window) {
      if (window.size() < 15) {
         return (double)0.0F;
      } else {
         Map<Long, Integer> counts = new HashMap();

         for(double v : window) {
            counts.merge(Math.round(v * (double)10000.0F), 1, Integer::sum);
         }

         long best = 0L;
         int bestN = 0;

         for(Map.Entry<Long, Integer> e : counts.entrySet()) {
            if ((Integer)e.getValue() > bestN) {
               bestN = (Integer)e.getValue();
               best = (Long)e.getKey();
            }
         }

         return (double)best / (double)10000.0F;
      }
   }

   public static void onUseItemRotation(double packetYaw, double packetPitch, double tickYaw, double tickPitch) {
      if (packetYaw != tickYaw || packetPitch != tickPitch) {
         ++useItemMismatch;
      }

   }

   public static void onSlotSent(int slot) {
      if (slot == lastSlotOnWire) {
         ++duplicateSlot;
      } else {
         lastSlotOnWire = slot;
      }

   }

   public static void tickState(long now, boolean sprinting, boolean sneaking) {
      int slotNow = SlotUtil.selected();
      if (lastSlotOnWire == -1) {
         lastSlotOnWire = slotNow;
      } else if (slotNow != lastSlotOnWire) {
         lastSlotOnWire = slotNow;
      }

      if (lastSprint != null && lastSprint != sprinting) {
         sprintFlips.addLast(now);
      }

      if (lastSprint != null && lastSprint == sprinting && forcedSprintResend) {
         ++duplicateSprint;
      }

      if (lastSneak != null && lastSneak == sneaking && forcedSneakResend) {
         ++duplicateSneak;
      }

      forcedSprintResend = false;
      forcedSneakResend = false;
      lastSprint = sprinting;
      lastSneak = sneaking;

      while(!sprintFlips.isEmpty() && now - (Long)sprintFlips.peekFirst() > 1000L) {
         sprintFlips.removeFirst();
      }

      while(!attacks.isEmpty() && now - (Long)attacks.peekFirst() > 1000L) {
         attacks.removeFirst();
      }

      sprintFlipRate = sprintFlips.size();
      attackRate = attacks.size();
   }

   public static void markForcedSprintResend() {
      forcedSprintResend = true;
   }

   public static void onAttack(long now) {
      attacks.addLast(now);
   }

   public static void reset() {
      gcdYawWindow.clear();
      gcdPitchWindow.clear();
      lastPitch = Double.NaN;
      lastYaw = Double.NaN;
      recoveredSens = (double)-1.0F;
      lastDotError = (double)0.0F;
      worstDotError = (double)0.0F;
      rotationSamples = 0L;
      offLatticeSamples = 0L;
      duplicateSneak = 0L;
      duplicateSprint = 0L;
      duplicateSlot = 0L;
      useItemMismatch = 0L;
      lastSlotOnWire = -1;
      duplicateLook = 0L;
      modulo360 = 0L;
      sprintFlips.clear();
      attacks.clear();
   }

   public static List<String> lines() {
      List<String> out = new ArrayList();
      double actual = actualSensitivity();
      out.add("PACKET AUDIT  (what the server can compute)");
      out.add("-- rotation quantization --");
      out.add(String.format("gcd mode      %.5f  (step %.5f)", modeGcdYaw, safeGcd()));
      out.add(recoveredSens < (double)0.0F ? "sens recover  collecting samples..." : String.format("sens recover  %.3f vs actual %.3f   %s", recoveredSens, actual, Math.abs(recoveredSens - actual) < 0.02 ? "MATCH" : "MISMATCH <-- you are not a mouse"));
      out.add(String.format("dot error     last %.3f / worst %.3f", lastDotError, worstDotError));
      long var10001 = offLatticeSamples;
      out.add("off-lattice   " + var10001 + " of " + rotationSamples + (offLatticeSamples == 0L ? "  clean" : "  <-- AimProcessor bait"));
      out.add("-- protocol consistency --");
      var10001 = useItemMismatch;
      out.add("BadPacketsJ   " + var10001 + " corrected before send  (0 escaped)");
      var10001 = duplicateSlot;
      out.add("BadPacketsA   " + var10001 + (duplicateSlot == 0L ? "  clean" : "  <-- duplicate slot"));
      var10001 = duplicateSprint;
      out.add("BadPacketsF/G " + var10001 + " / " + duplicateSneak);
      var10001 = modulo360;
      out.add("AimModulo360  " + var10001);
      var10001 = duplicateLook;
      out.add("DuplicateLook " + var10001 + (duplicateLook == 0L ? "  clean" : ""));
      out.add("-- observed rates --");
      int var8 = attackRate;
      out.add("attacks       " + var8 + "/s  (yours + modules)");
      out.add("sprint flips  " + sprintFlipRate + "/s" + (sprintFlipRate > 6 ? "  <-- SprintA-G bait" : ""));
      out.add("slot pkts     " + SlotGuard.status());
      out.add("action pkts   " + ActionBudget.status());
      return out;
   }

   private static double actualSensitivity() {
      try {
         return (Double)class_310.method_1551().field_1690.method_42495().method_41753();
      } catch (Throwable var1) {
         return (double)-1.0F;
      }
   }

   private static double safeGcd() {
      try {
         return Rotations.gcd();
      } catch (Throwable var1) {
         return (double)-1.0F;
      }
   }
}
