package dev.sevenclient.util;

import java.util.Random;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public final class SimpleAim {
   public double horizontalSpeed = (double)5.0F;
   public double verticalSpeed = (double)5.0F;
   public boolean aimVertically = false;
   public boolean strafeIncrease = false;
   public boolean closestArea = false;
   public double aimHeightOffset = (double)0.0F;
   private float authority;
   private float horizontalVelocity;
   private float horizontalVelocityBuffer;
   private float verticalVelocity;
   private float verticalVelocityBuffer;
   private float pendingYaw;
   private float pendingPitch;
   private double prevTargetX;
   private double prevTargetZ;
   private double lastAngleDiff;
   private boolean prevOnLeft;
   private boolean prevAbove;
   private int swapTickCounter;
   private int sampleCounter;
   private int driftTimer;
   private int randomOffsetX;
   private int randomOffsetY;
   private int driftX;
   private int driftY;
   private final Random random = new Random();

   public float strength() {
      return this.authority;
   }

   public void reset() {
      this.pendingYaw = 0.0F;
      this.pendingPitch = 0.0F;
      this.horizontalVelocity = 0.0F;
      this.horizontalVelocityBuffer = 0.0F;
      this.verticalVelocity = 0.0F;
      this.verticalVelocityBuffer = 0.0F;
      this.prevTargetX = (double)0.0F;
      this.prevTargetZ = (double)0.0F;
      this.lastAngleDiff = (double)0.0F;
      this.prevOnLeft = false;
      this.prevAbove = false;
      this.swapTickCounter = 0;
      this.sampleCounter = 0;
      this.driftTimer = 0;
      this.randomOffsetX = 0;
      this.randomOffsetY = 0;
      this.driftX = 0;
      this.driftY = 0;
   }

   public void tick(class_746 var1, class_1309 var2, float var3) {
      if (var1 != null && var2 != null) {
         this.updateVelocityBuffers();
         this.applyRotation(var1, var2);
      }
   }

   private void updateVelocityBuffers() {
      if (++this.swapTickCounter > 10) {
         this.verticalVelocityBuffer = this.verticalVelocity;
         this.horizontalVelocity = this.horizontalVelocityBuffer;
         this.horizontalVelocityBuffer = 0.0F;
         this.verticalVelocity = 0.0F;
         this.swapTickCounter = 0;
      }

   }

   private void updateDrift() {
      ++this.driftTimer;
      if (this.driftTimer >= 250 + this.random.nextInt(50)) {
         this.driftTimer = this.random.nextInt(50) - 100;
         this.randomOffsetX = this.random.nextInt(3) - 1;
         this.randomOffsetY = this.random.nextInt(3) - 1;
      }

      int var1 = this.randomOffsetX;
      int var2 = this.randomOffsetY;
      if (this.random.nextInt(10) < 2) {
         var1 = 0;
      }

      if (this.random.nextInt(10) < 2) {
         var2 = 0;
      }

      if (this.driftTimer < 0) {
         var1 = 0;
         var2 = 0;
      }

      if (this.random.nextInt(20) == 1) {
         this.driftX += var1;
         this.driftY += var2;
      }

      if (this.pendingYaw > 0.0F && this.driftX < 0 || this.pendingYaw < 0.0F && this.driftX > 0) {
         this.driftX = 0;
      }

   }

   private void queueHorizontal(float var1) {
      if (var1 != 0.0F) {
         var1 *= 5.0F;
         this.pendingYaw += (float)this.horizontalSpeed * var1;
      } else {
         this.pendingYaw = 0.0F;
      }

   }

   private void queueVertical(float var1) {
      if (var1 != 0.0F) {
         var1 *= 5.0F;
         this.pendingPitch += (float)this.verticalSpeed * var1;
      } else {
         this.pendingPitch = 0.0F;
      }

   }

   private void applyRotation(class_746 var1, class_1309 var2) {
      this.updateDrift();
      class_238 var3 = var2.method_5829();
      double var4;
      double var6;
      double var8;
      if (this.closestArea) {
         class_243 var10 = var1.method_33571();
         class_243 var11 = TargetUtil.nearestPoint(var10, var2);
         var4 = var11.field_1352;
         var6 = var11.field_1351 + this.aimHeightOffset;
         var8 = var11.field_1350;
      } else {
         var4 = var2.method_23317();
         var6 = (var3.field_1322 + var3.field_1325) * (double)0.5F + this.aimHeightOffset;
         var8 = var2.method_23321();
      }

      double var40 = var4 - this.prevTargetX;
      double var12 = var8 - this.prevTargetZ;
      this.prevTargetX = var4;
      this.prevTargetZ = var8;
      double var14 = var4 + var40 * 1.7;
      double var16 = var8 + var12 * 1.7;
      class_243 var18 = var1.method_33571();
      float[] var19 = Rotations.to(var18, new class_243(var14, var6, var16));
      float var20 = var1.method_36454();
      float var21 = var1.method_36455();
      float var22 = class_3532.method_15393(var19[0] - var20);
      float var23 = Math.abs(var22);
      boolean var24 = var22 < 0.0F;
      float var25 = var19[1] - var21;
      boolean var26 = var25 < 0.0F;
      float var27 = Math.abs(var25) - 10.0F;
      float var28 = 1.0F + (float)(this.random.nextDouble() * (double)2.0F) + var23 / 50.0F;
      float var29 = 1.0F + (float)(this.random.nextDouble() * (double)2.0F) + Math.abs(var27) / 50.0F;
      if (Math.abs(var23 - (float)this.lastAngleDiff) > 6.0F) {
         var28 += var23 / 35.0F;
      }

      double var30 = (double)var1.method_5739(var2);
      var28 += (float)Math.max((double)0.0F, ((double)9.0F - var30) / (double)2.5F - (double)2.0F);
      float var32 = (class_310.method_1551().field_1690.field_1913.method_1434() ? 1.0F : 0.0F) - (class_310.method_1551().field_1690.field_1849.method_1434() ? 1.0F : 0.0F);
      boolean var33 = var24 ? var32 < 0.0F : var32 > 0.0F;
      if (this.strafeIncrease && var33) {
         var28 *= 1.6F;
      }

      if (var30 < (double)0.5F) {
         var28 /= 5.0F;
      }

      float var34 = var28 / 90.0F * (var24 ? -1.0F : 1.0F);
      float var35 = var29 / 90.0F * (var26 ? -1.0F : 1.0F);
      if (var23 < 5.0F) {
         var34 = 0.0F;
         this.horizontalVelocity *= 0.7F;
         boolean var36 = var24 ? var32 > 0.0F : var32 < 0.0F;
         if (var36) {
            this.horizontalVelocity *= 0.5F;
         }
      }

      if (var24 != this.prevOnLeft) {
         this.horizontalVelocity = -this.horizontalVelocity;
         this.horizontalVelocityBuffer = -this.horizontalVelocityBuffer;
         this.pendingYaw = 0.0F;
      }

      if (var26 != this.prevAbove) {
         this.verticalVelocityBuffer = -this.verticalVelocityBuffer;
         this.verticalVelocity = -this.verticalVelocity;
         this.pendingPitch = 0.0F;
      }

      if (var27 < 5.0F) {
         var35 = 0.0F;
         this.verticalVelocityBuffer *= 0.7F;
      }

      this.horizontalVelocityBuffer += var34;
      this.verticalVelocity += var35;
      float var42 = this.horizontalVelocity;
      float var37 = this.verticalVelocityBuffer;
      if (Math.abs(var42) > 10.0F) {
         this.horizontalVelocityBuffer = 0.0F;
         this.horizontalVelocity = 0.0F;
      } else {
         float var38 = var42 * 0.15F;
         if (var23 <= 9.0F) {
            var38 /= 10.0F - var23;
         }

         if (Float.isNaN(var38)) {
            this.horizontalVelocityBuffer = 0.0F;
            this.horizontalVelocity = 0.0F;
         } else {
            this.queueHorizontal(var38);
            if (this.aimVertically) {
               float var39 = var37 * 0.15F;
               if (Float.isNaN(var39)) {
                  this.verticalVelocity = 0.0F;
                  this.verticalVelocityBuffer = 0.0F;
                  return;
               }

               this.queueVertical(var39);
            }

            this.prevAbove = var26;
            this.prevOnLeft = var24;
            this.authority = Math.max(0.0F, Math.min(1.0F, 1.0F - var23 / 30.0F));
            if (++this.sampleCounter > 10) {
               this.lastAngleDiff = (double)var23;
               this.sampleCounter = 0;
            }

            this.flush(var1);
         }
      }
   }

   private void flush(class_746 var1) {
      this.pendingYaw += (float)this.driftX;
      this.pendingPitch += (float)this.driftY;
      int var2 = (int)this.pendingYaw;
      int var3 = this.aimVertically ? (int)this.pendingPitch : 0;
      this.pendingYaw -= (float)var2;
      this.pendingPitch = this.aimVertically ? this.pendingPitch - (float)var3 : 0.0F;
      this.driftX = 0;
      this.driftY = 0;
      if (var2 != 0 || var3 != 0) {
         float var4 = MouseRotation.mouseScale();
         var1.method_5872((double)((float)var2 * var4), (double)((float)var3 * var4));
         RotationSync.dirty = true;
         float var5 = MouseRotation.degreesPerCount();
         HumanDiag.tickYawSpent += (double)Math.abs((float)var2 * var5);
         Diagnostics.lastYawStep = (double)((float)var2 * var5);
      }
   }
}
