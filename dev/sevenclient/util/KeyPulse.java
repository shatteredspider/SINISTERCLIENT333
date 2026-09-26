package dev.sevenclient.util;

import net.minecraft.class_304;

public final class KeyPulse {
   private final class_304 binding;
   private long releaseAt = 0L;
   private boolean holding = false;

   public KeyPulse(class_304 binding) {
      this.binding = binding;
   }

   public void press(long millis) {
      long now = System.currentTimeMillis();
      this.releaseAt = Math.max(this.releaseAt, now + Math.max(1L, millis));
      this.holding = true;
      this.binding.method_23481(true);
   }

   public void tick() {
      if (this.holding) {
         if (System.currentTimeMillis() >= this.releaseAt) {
            this.release();
         } else {
            this.binding.method_23481(true);
         }
      }

   }

   public void release() {
      if (this.holding) {
         this.holding = false;
         this.releaseAt = 0L;
         this.binding.method_23481(false);
      }

   }

   public boolean active() {
      return this.holding;
   }
}
