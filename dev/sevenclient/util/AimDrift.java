package dev.sevenclient.util;

import java.util.Random;

public final class AimDrift {
   private final Random random = new Random();
   private int driftTimer;
   private int randomOffsetX;
   private int randomOffsetY;
   private int driftX;
   private int driftY;

   public void reset() {
      this.driftTimer = 0;
      this.randomOffsetX = 0;
      this.randomOffsetY = 0;
      this.driftX = 0;
      this.driftY = 0;
   }

   public void update(float var1) {
      ++this.driftTimer;
      if (this.driftTimer >= 250 + this.random.nextInt(50)) {
         this.driftTimer = this.random.nextInt(50) - 100;
         this.randomOffsetX = this.random.nextInt(3) - 1;
         this.randomOffsetY = this.random.nextInt(3) - 1;
      }

      int var2 = this.randomOffsetX;
      int var3 = this.randomOffsetY;
      if (this.random.nextInt(10) < 2) {
         var2 = 0;
      }

      if (this.random.nextInt(10) < 2) {
         var3 = 0;
      }

      if (this.driftTimer < 0) {
         var2 = 0;
         var3 = 0;
      }

      if (this.random.nextInt(20) == 1) {
         this.driftX += var2;
         this.driftY += var3;
      }

   }

   public void cancelOpposed(float var1) {
      if (var1 > 0.0F && this.driftX < 0 || var1 < 0.0F && this.driftX > 0) {
         this.driftX = 0;
      }

   }

   public int takeAccumX() {
      int var1 = this.driftX;
      this.driftX = 0;
      return var1;
   }

   public int takeAccumY() {
      int var1 = this.driftY;
      this.driftY = 0;
      return var1;
   }
}
