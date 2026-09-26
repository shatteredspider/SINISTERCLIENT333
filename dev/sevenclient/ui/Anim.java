package dev.sevenclient.ui;

import java.util.HashMap;
import java.util.Map;

public final class Anim {
   private static final Map<String, Float> VALUES = new HashMap();
   public static volatile float speedScale = 1.0F;
   public static volatile boolean enabled = true;
   private static long lastNanos = 0L;
   private static float dt = 0.016F;

   private Anim() {
   }

   public static void beginFrame() {
      long now = System.nanoTime();
      if (lastNanos == 0L) {
         dt = 0.016F;
      } else {
         dt = (float)((double)(now - lastNanos) / (double)1.0E9F);
         if (dt < 5.0E-4F) {
            dt = 5.0E-4F;
         }

         if (dt > 0.1F) {
            dt = 0.1F;
         }
      }

      lastNanos = now;
   }

   public static float dt() {
      return dt;
   }

   public static float to(String key, float target, float speed) {
      if (!enabled) {
         VALUES.put(key, target);
         return target;
      } else {
         float cur = (Float)VALUES.getOrDefault(key, target);
         float k = 1.0F - (float)Math.exp((double)(-Math.max(0.05F, speed * speedScale) * dt));
         float next = cur + (target - cur) * k;
         if (Math.abs(target - next) < 5.0E-4F) {
            next = target;
         }

         VALUES.put(key, next);
         return next;
      }
   }

   public static float hover(String key, boolean on) {
      return to(key, on ? 1.0F : 0.0F, 16.0F);
   }

   public static float toggle(String key, boolean on) {
      return to(key, on ? 1.0F : 0.0F, 13.0F);
   }

   public static float panel(String key, float target) {
      return to(key, target, 9.0F);
   }

   public static float scroll(String key, float target) {
      return to(key, target, 14.0F);
   }

   public static float raw(String key, float def) {
      return (Float)VALUES.getOrDefault(key, def);
   }

   public static void set(String key, float v) {
      VALUES.put(key, v);
   }

   public static void clear() {
      VALUES.clear();
   }

   public static float easeOutCubic(float t) {
      float u = 1.0F - clamp01(t);
      return 1.0F - u * u * u;
   }

   public static float easeInOut(float t) {
      t = clamp01(t);
      return t * t * (3.0F - 2.0F * t);
   }

   public static float easeOutBack(float t) {
      t = clamp01(t);
      float c = 1.70158F;
      float u = t - 1.0F;
      return 1.0F + (c + 1.0F) * u * u * u + c * u * u;
   }

   public static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }
}
