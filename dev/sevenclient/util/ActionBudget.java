package dev.sevenclient.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class ActionBudget {
   private static final int GLOBAL_PER_SECOND = 18;
   private static final Deque<Long> global = new ArrayDeque();
   private static final Map<String, Long> lastByChannel = new HashMap();
   public static volatile int rate = 0;
   public static volatile long blocked = 0L;
   public static volatile long performed = 0L;

   private ActionBudget() {
   }

   private static void prune(long now) {
      while(!global.isEmpty() && now - (Long)global.peekFirst() > 1000L) {
         global.removeFirst();
      }

      rate = global.size();
   }

   public static boolean claim(String channel, long minIntervalMs, long now) {
      prune(now);
      Long last = (Long)lastByChannel.get(channel);
      if (last != null && now - last < minIntervalMs) {
         ++blocked;
         return false;
      } else if (global.size() >= 18) {
         ++blocked;
         return false;
      } else {
         lastByChannel.put(channel, now);
         global.addLast(now);
         ++performed;
         prune(now);
         return true;
      }
   }

   public static void reset() {
      global.clear();
      lastByChannel.clear();
      rate = 0;
   }

   public static String status() {
      return rate + "/s of 18  (" + performed + " ok / " + blocked + " blocked)";
   }
}
