package dev.sevenclient.util;

public final class HumanDiag {
   public static volatile long gcdSyncCalls = 0L;
   public static volatile long gcdSyncApplied = 0L;
   public static volatile double gcdSyncLast = (double)0.0F;
   public static volatile int aimTargetId = -1;
   public static volatile int turnLatch = 0;
   public static volatile double farness = (double)0.0F;
   public static volatile double unwrappedErr = (double)0.0F;
   public static volatile double tickYawSpent = (double)0.0F;
   public static volatile double lastTickYaw = (double)0.0F;
   public static volatile double maxTickYaw = (double)0.0F;
   public static volatile long rotBudgetHits = 0L;
   public static volatile String hitSwapState = "idle";
   public static volatile long hitSwapSwaps = 0L;
   public static volatile long hitSwapFeints = 0L;
   public static volatile long hitSwapMisses = 0L;
   public static volatile double hitSwapLastDelay = (double)0.0F;
   public static volatile String triggerState = "off";
   public static volatile long triggerClicks = 0L;
   public static volatile long triggerCrits = 0L;
   public static volatile long triggerPCrits = 0L;
   public static volatile long pcritArmed = 0L;
   public static volatile long sprintBroken = 0L;
   public static volatile boolean silentAimActive = false;
   public static volatile String silentAimState = "off";
   public static volatile String pingSpoofState = "off";
   public static volatile long jumpResets = 0L;
   public static volatile long sprintTaps = 0L;
   public static volatile long useItemSnaps = 0L;
   public static volatile double useItemLastSnap = (double)0.0F;

   private HumanDiag() {
   }

   public static String gcdSyncStatus() {
      return gcdSyncCalls == 0L ? "MIXIN DEAD (never ran)" : gcdSyncCalls + " calls / " + gcdSyncApplied + " snapped" + String.format(" (last %.4f deg)", gcdSyncLast);
   }

   public static String useItemStatus() {
      return useItemSnaps == 0L ? "0 (right-click something to test)" : useItemSnaps + " snapped" + String.format(" (last %.4f deg)", useItemLastSnap);
   }

   public static String rotStatus() {
      return String.format("%.1f last / %.1f peak deg per tick, %d clamped", lastTickYaw, maxTickYaw, rotBudgetHits);
   }

   public static String triggerStats() {
      long c = triggerClicks;
      double pct = c == 0L ? (double)0.0F : (double)100.0F * (double)triggerCrits / (double)c;
      return c + " clicks / " + triggerCrits + " crit (" + String.format("%.0f%%", pct) + ") / " + triggerPCrits + " p-crit, " + pcritArmed + " armed, " + sprintBroken + " w-tap";
   }

   public static String humanState() {
      return String.format("arousal %+.2f  focus %.2f  refr %.0fms%s", Human.arousal(), Human.focus01(), Human.refractoryMs(), Human.busy() ? "  BUSY" : "");
   }
}
