package dev.sevenclient.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SlotGuard {
   private static final long MIN_INTERVAL_MS = 55L;
   private static final int MAX_PER_SECOND = 9;
   private static long lastChange = 0L;
   private static final Deque<Long> recent = new ArrayDeque();
   private static int lastSeen = -1;
   public static volatile long playerChanges = 0L;
   public static volatile int rate = 0;
   public static volatile long blocked = 0L;
   public static volatile long performed = 0L;

   private SlotGuard() {
   }

   private static void prune(long now) {
      while(!recent.isEmpty() && now - (Long)recent.peekFirst() > 1000L) {
         recent.removeFirst();
      }

      rate = recent.size();
   }

   public static void observe(long now) {
      int cur = SlotUtil.selected();
      if (lastSeen == -1) {
         lastSeen = cur;
      } else if (cur != lastSeen) {
         lastSeen = cur;
         if (now - lastChange > 60L) {
            ++playerChanges;
            recent.addLast(now);
            prune(now);
         }
      }

   }

   public static boolean ready(long now) {
      prune(now);
      return now - lastChange >= 55L && recent.size() < 9;
   }

   public static boolean select(int slot, long now) {
      if (slot >= 0 && slot <= 8) {
         if (SlotUtil.selected() == slot) {
            lastSeen = slot;
            return true;
         } else if (!ready(now)) {
            ++blocked;
            return false;
         } else if (!ActionBudget.claim("slot", 0L, now)) {
            ++blocked;
            return false;
         } else {
            PacketAudit.onSlotSent(slot);
            SlotUtil.select(slot);
            lastSeen = slot;
            lastChange = now;
            recent.addLast(now);
            ++performed;
            prune(now);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void reset() {
      recent.clear();
      lastChange = 0L;
      rate = 0;
   }

   public static String status() {
      return rate + "/s of 9  " + performed + " mod / " + playerChanges + " you / " + blocked + " blocked";
   }
}
