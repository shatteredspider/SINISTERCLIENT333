package dev.sevenclient.util;

import net.minecraft.class_3532;
import net.minecraft.class_746;

public final class RotationSync {
   private static double lastSentYaw = Double.NaN;
   private static double lastSentPitch = Double.NaN;
   public static volatile boolean dirty = false;
   public static volatile boolean enabled = true;
   public static final int PRIORITY_SILENT_AIM = 10;
   public static final int PRIORITY_HIT_FLICK = 20;
   private static long reqTick = -1L;
   private static int reqPriority = Integer.MIN_VALUE;
   private static String reqOwner = null;
   private static boolean reqActive = false;
   private static float reqYaw = 0.0F;
   private static boolean reqUsePitch = false;
   private static float reqPitch = 0.0F;
   private static float restoreYaw = 0.0F;
   private static float restorePitch = 0.0F;
   private static boolean restorePending = false;
   public static volatile boolean hideRotation = false;
   public static volatile float maxHiddenYaw = 30.0F;
   public static volatile float maxHiddenPitch = 20.0F;
   private static volatile float hiddenYaw = 0.0F;
   private static volatile float hiddenPitch = 0.0F;

   private RotationSync() {
   }

   private static void rollTick() {
      long now = Diagnostics.ticks;
      if (now != reqTick) {
         reqTick = now;
         reqPriority = Integer.MIN_VALUE;
         reqOwner = null;
         reqActive = false;
         reqUsePitch = false;
      }

   }

   public static synchronized boolean requestSilent(String owner, int priority, float yaw) {
      return requestSilent(owner, priority, yaw, false, 0.0F);
   }

   public static synchronized boolean requestSilent(String owner, int priority, float yaw, boolean usePitch, float pitch) {
      rollTick();
      if (reqActive && !owner.equals(reqOwner) && priority <= reqPriority) {
         return false;
      } else {
         reqOwner = owner;
         reqPriority = priority;
         reqActive = true;
         reqYaw = yaw;
         reqUsePitch = usePitch;
         reqPitch = pitch;
         return true;
      }
   }

   public static synchronized void releaseSilent(String owner) {
      if (owner != null && owner.equals(reqOwner)) {
         reqActive = false;
         reqOwner = null;
         reqUsePitch = false;
         reqPriority = Integer.MIN_VALUE;
      }

   }

   public static boolean silentActive() {
      return reqActive && reqTick == Diagnostics.ticks;
   }

   public static String silentOwner() {
      return reqOwner == null ? "-" : reqOwner;
   }

   public static void beginSilent(class_746 player) {
      restorePending = false;
      if (player != null && silentActive()) {
         restoreYaw = player.method_36454();
         restorePitch = player.method_36455();
         player.method_36456(reqYaw);
         player.method_5847(reqYaw);
         if (reqUsePitch) {
            player.method_36457(class_3532.method_15363(reqPitch, -90.0F, 90.0F));
         }

         restorePending = true;
         HumanDiag.silentAimActive = true;
      }

   }

   public static void endSilent(class_746 player) {
      if (restorePending && player != null) {
         restorePending = false;
         player.method_36456(restoreYaw);
         player.method_5847(restoreYaw);
         player.method_36457(restorePitch);
      }

   }

   public static void reset() {
      lastSentYaw = Double.NaN;
      lastSentPitch = Double.NaN;
      dirty = false;
   }

   public static void snapBeforeSend(class_746 player) {
      if (player != null) {
         ++HumanDiag.gcdSyncCalls;
         HumanDiag.lastTickYaw = HumanDiag.tickYawSpent;
         if (HumanDiag.tickYawSpent > HumanDiag.maxTickYaw) {
            HumanDiag.maxTickYaw = HumanDiag.tickYawSpent;
         }

         HumanDiag.tickYawSpent = (double)0.0F;
         boolean quantise = enabled && (dirty || silentActive());
         if (quantise) {
            dirty = false;
            double step = Rotations.gcd();
            if (step > (double)0.0F) {
               if (Double.isNaN(lastSentYaw)) {
                  lastSentYaw = (double)player.method_36454();
                  lastSentPitch = (double)player.method_36455();
               } else {
                  double dYaw = (double)Rotations.wrap((float)((double)player.method_36454() - lastSentYaw));
                  double dPitch = (double)player.method_36455() - lastSentPitch;
                  double qYaw = (double)Math.round(dYaw / step) * step;
                  double qPitch = (double)Math.round(dPitch / step) * step;
                  float newYaw = (float)(lastSentYaw + qYaw);
                  float newPitch = class_3532.method_15363((float)(lastSentPitch + qPitch), -90.0F, 90.0F);
                  ++HumanDiag.gcdSyncApplied;
                  HumanDiag.gcdSyncLast = Math.max(Math.abs(qYaw - dYaw), Math.abs(qPitch - dPitch));
                  player.method_36456(newYaw);
                  player.method_36457(newPitch);
                  player.method_5847(newYaw);
                  lastSentYaw = (double)newYaw;
                  lastSentPitch = (double)newPitch;
               }
            }
         } else {
            lastSentYaw = (double)player.method_36454();
            lastSentPitch = (double)player.method_36455();
         }

         PacketAudit.onFlyingRotation((double)player.method_36454(), (double)player.method_36455());
      }

   }

   public static void quantiseNow(class_746 player) {
      if (player != null && enabled) {
         double step = Rotations.gcd();
         if (!(step <= (double)0.0F)) {
            if (Double.isNaN(lastSentYaw)) {
               lastSentYaw = (double)player.method_36454();
               lastSentPitch = (double)player.method_36455();
            } else {
               PacketAudit.onUseItemRotation((double)player.method_36454(), (double)player.method_36455(), lastSentYaw, lastSentPitch);
               double dYaw = (double)Rotations.wrap((float)((double)player.method_36454() - lastSentYaw));
               double dPitch = (double)player.method_36455() - lastSentPitch;
               double qYaw = (double)Math.round(dYaw / step) * step;
               double qPitch = (double)Math.round(dPitch / step) * step;
               float newYaw = (float)(lastSentYaw + qYaw);
               float newPitch = class_3532.method_15363((float)(lastSentPitch + qPitch), -90.0F, 90.0F);
               player.method_36456(newYaw);
               player.method_36457(newPitch);
               player.method_5847(newYaw);
               lastSentYaw = (double)newYaw;
               lastSentPitch = (double)newPitch;
               dirty = false;
               ++HumanDiag.useItemSnaps;
               HumanDiag.useItemLastSnap = Math.max(Math.abs(qYaw - dYaw), Math.abs(qPitch - dPitch));
            }
         }
      }
   }

   public static void applyHidden(class_746 player, double dYaw, double dPitch) {
      if (player != null && (dYaw != (double)0.0F || dPitch != (double)0.0F)) {
         float newYaw = (float)((double)player.method_36454() + dYaw);
         float newPitch = class_3532.method_15363((float)((double)player.method_36455() + dPitch), -90.0F, 90.0F);
         double appliedPitch = (double)(newPitch - player.method_36455());
         player.method_36456(newYaw);
         player.method_5847(newYaw);
         player.method_36457(newPitch);
         dirty = true;
         if (hideRotation) {
            hiddenYaw = class_3532.method_15363((float)((double)hiddenYaw + dYaw), -maxHiddenYaw, maxHiddenYaw);
            hiddenPitch = class_3532.method_15363((float)((double)hiddenPitch + appliedPitch), -maxHiddenPitch, maxHiddenPitch);
         }
      }

   }

   public static void decayHidden(float dt, double rate) {
      if (hiddenYaw != 0.0F || hiddenPitch != 0.0F) {
         float k = (float)Math.exp(-rate * (double)dt);
         hiddenYaw *= k;
         hiddenPitch *= k;
         if (Math.abs(hiddenYaw) < 0.001F) {
            hiddenYaw = 0.0F;
         }

         if (Math.abs(hiddenPitch) < 0.001F) {
            hiddenPitch = 0.0F;
         }
      }

   }

   public static void clearHidden() {
      hiddenYaw = 0.0F;
      hiddenPitch = 0.0F;
   }

   public static boolean cameraHidden() {
      return hideRotation && (hiddenYaw != 0.0F || hiddenPitch != 0.0F);
   }

   public static float cameraYaw(float realYaw) {
      return realYaw - hiddenYaw;
   }

   public static float cameraPitch(float realPitch) {
      return class_3532.method_15363(realPitch - hiddenPitch, -90.0F, 90.0F);
   }

   public static float hiddenYaw() {
      return hiddenYaw;
   }

   public static float hiddenPitch() {
      return hiddenPitch;
   }
}
