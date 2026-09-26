package dev.sevenclient.util;

import dev.sevenclient.SevenClient;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public final class FrameDispatcher {
   private static long lastNanos = 0L;
   private static int framesSinceMixin = Integer.MAX_VALUE;

   private FrameDispatcher() {
   }

   public static void fromMouseMixin() {
      framesSinceMixin = 0;
      Diagnostics.frameSourceIsMixin = true;
      dispatch();
   }

   public static void fromRenderFallback() {
      if (framesSinceMixin < 5) {
         ++framesSinceMixin;
      } else {
         Diagnostics.frameSourceIsMixin = false;
         dispatch();
      }

   }

   private static void dispatch() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null && SevenClient.get() != null) {
         long now = System.nanoTime();
         float dt = lastNanos == 0L ? 0.016666668F : (float)((double)(now - lastNanos) / (double)1.0E9F);
         lastNanos = now;
         dt = class_3532.method_15363(dt, 0.001F, 0.1F);
         Diagnostics.lastDt = dt;
         Diagnostics.countFrame();
         SevenClient.get().modules.onFrame(dt);
      }

   }
}
