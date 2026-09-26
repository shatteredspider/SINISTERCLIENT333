package dev.sevenclient.util;

import java.util.Random;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public final class AdaptiveAim {
   public double horizontalSpeed = (double)5.0F;
   public double verticalSpeed = (double)5.0F;
   public boolean aimVertically = false;
   public boolean strafeIncrease = false;
   public boolean closestArea = false;
   public double aimHeightOffset = (double)0.0F;
   public boolean directCorrection = false;
   private double aimX;
   private double aimY;
   private double aimZ;
   private double leadX;
   private double leadY;
   private double leadZ;
   private boolean aimPointInitialized;
   private double predX;
   private double predY;
   private double predZ;
   private boolean predictionInitialized;
   private float aimStrength;
   private float yawBias;
   private float pitchBias;
   private float yawVelocity;
   private float pitchVelocity;
   private float yawAccel;
   private float pitchAccel;
   private float lastTargetYaw;
   private float lastTargetPitch;
   private float lastPlayerYaw;
   private float lastPlayerPitch;
   private float lastYawDiff;
   private float lastPitchDiff;
   private float overshoot;
   private float yawFlickTicks;
   private float pitchFlickTicks;
   private float lastYawSign;
   private float lastPitchSign;
   private float pendingYaw;
   private float pendingPitch;
   private boolean initialized;
   private long lastFrameNanos;
   private float airFactor;
   private double smoothAir;
   private float verticalVelocity;
   private double lastEyeY;
   private double lastGroundEyeY;
   private float driftPos;
   private float driftVelocity;
   private float driftTarget;
   private float driftNoise;
   private long driftNextNanos;
   private long noiseStartNanos;
   private final Random random = new Random();
   public boolean yieldToMouse = true;
   private double lastPostYaw = Double.NaN;
   private double lastPostPitch = Double.NaN;
   private float ownAppliedYaw;
   private float ownAppliedPitch;
   private float opposeTime;
   private boolean yielding;
   private long refractoryUntilNanos;
   private int agreeFrames;
   private float idleTime;
   private float handSignedYaw;
   private float handSignedPitch;
   private float handEnergy;

   public void reset() {
      this.pendingPitch = 0.0F;
      this.pendingYaw = 0.0F;
      this.pitchBias = 0.0F;
      this.yawBias = 0.0F;
      this.pitchVelocity = 0.0F;
      this.yawVelocity = 0.0F;
      this.pitchAccel = 0.0F;
      this.yawAccel = 0.0F;
      this.lastPitchDiff = 0.0F;
      this.lastYawDiff = 0.0F;
      this.lastTargetPitch = 0.0F;
      this.lastTargetYaw = 0.0F;
      this.lastPlayerPitch = 0.0F;
      this.lastPlayerYaw = 0.0F;
      this.initialized = false;
      this.lastFrameNanos = 0L;
      this.aimPointInitialized = false;
      this.aimZ = (double)0.0F;
      this.aimY = (double)0.0F;
      this.aimX = (double)0.0F;
      this.leadZ = (double)0.0F;
      this.leadY = (double)0.0F;
      this.leadX = (double)0.0F;
      this.aimStrength = 0.0F;
      this.pitchFlickTicks = 0.0F;
      this.yawFlickTicks = 0.0F;
      this.lastPitchSign = 0.0F;
      this.lastYawSign = 0.0F;
      this.overshoot = 0.0F;
      this.lastGroundEyeY = (double)0.0F;
      this.lastEyeY = (double)0.0F;
      this.verticalVelocity = 0.0F;
      this.airFactor = 0.0F;
      this.smoothAir = (double)0.0F;
      this.predictionInitialized = false;
      this.predZ = (double)0.0F;
      this.predY = (double)0.0F;
      this.predX = (double)0.0F;
      this.noiseStartNanos = System.nanoTime();
      this.driftNoise = 0.0F;
      this.driftTarget = 0.0F;
      this.driftVelocity = 0.0F;
      this.driftPos = 0.0F;
      this.driftNextNanos = 0L;
      this.lastPostYaw = Double.NaN;
      this.lastPostPitch = Double.NaN;
      this.ownAppliedYaw = 0.0F;
      this.ownAppliedPitch = 0.0F;
      this.opposeTime = 0.0F;
      this.yielding = false;
      this.refractoryUntilNanos = 0L;
      this.agreeFrames = 0;
      this.idleTime = 0.0F;
      this.handSignedYaw = 0.0F;
      this.handSignedPitch = 0.0F;
      this.handEnergy = 0.0F;
   }

   private static float lerp(float var0, float var1, float var2) {
      return var1 + var0 * (var2 - var1);
   }

   private static float smoothStep(float var0, float var1, float var2) {
      float var3 = class_3532.method_15363((var2 - var0) / (var1 - var0), 0.0F, 1.0F);
      return var3 * var3 * (3.0F - 2.0F * var3);
   }

   private static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
   }

   private double[] resolveAimTarget(class_746 var1, class_1309 var2) {
      double var3 = var1.method_33571().field_1351;
      class_238 var5 = var2.method_5829();
      double var6 = var5.field_1322;
      double var8 = var5.field_1325;
      double var10 = var6 + (var8 - var6) * 0.65;
      double var12 = Math.min(0.85, this.smoothAir);
      double var14 = 0.1 + var12;
      double var16 = class_3532.method_15350(var3 - var14, var6 + 0.01, var8 - 0.01);
      double var18 = class_3532.method_15350(var12 / 0.55, (double)0.0F, (double)1.0F);
      double var20 = class_3532.method_15350(var16 + (var10 - var16) * var18 + this.aimHeightOffset, var6 + 0.01, var8 - 0.01);
      if (this.closestArea) {
         double var22 = class_3532.method_15350(var1.method_23317(), var5.field_1323, var5.field_1320);
         double var24 = class_3532.method_15350(var1.method_23321(), var5.field_1321, var5.field_1324);
         double var26 = class_3532.method_15350(var3, var6 + 0.01, var8 - 0.01);
         double var28 = class_3532.method_15350(var26 - var14, var6 + 0.01, var8 - 0.01);
         var28 += (var10 - var28) * var18 + this.aimHeightOffset;
         var28 = class_3532.method_15350(var28, var6 + 0.01, var8 - 0.01);
         double var30 = var22;
         double var32 = var28;
         double var34 = var24;
         if (this.predictionInitialized) {
            double var36 = 0.35;
            var30 = this.predX + (var22 - this.predX) * var36;
            var32 = this.predY + (var28 - this.predY) * var36;
            var34 = this.predZ + (var24 - this.predZ) * var36;
         }

         this.predX = var30;
         this.predY = var32;
         this.predZ = var34;
         this.predictionInitialized = true;
         return new double[]{var30, var32, var34};
      } else {
         return new double[]{var2.method_23317(), var20, var2.method_23321()};
      }
   }

   private float computePitchDrift(float var1, float var2, long var3) {
      float var5 = 0.65F + 0.35F * this.aimStrength;
      if (this.driftNextNanos == 0L || var3 >= this.driftNextNanos) {
         float var6 = lerp(var5, 0.05F, 0.15F);
         float var7 = var1 > 22.0F ? -0.18F : (var1 < -22.0F ? 0.18F : 0.0F);
         this.driftTarget = clamp((this.random.nextFloat() * 2.0F - 1.0F) * var6 + var7, -0.5F, 0.5F);
         this.driftNextNanos = var3 + (300L + (long)this.random.nextInt(420)) * 1000000L;
      }

      float var15 = lerp(var5, 2.0F, 5.0F);
      float var16 = (float)Math.pow((double)0.04F, (double)var2);
      this.driftVelocity += (this.driftTarget - this.driftPos) * var15 * var2;
      this.driftVelocity *= var16;
      this.driftPos += this.driftVelocity * var2;
      float var8 = lerp(var5, 0.45F, 0.8F);
      if (this.driftPos > var8) {
         this.driftPos = var8;
         this.driftVelocity = Math.min(0.0F, this.driftVelocity);
      } else if (this.driftPos < -var8) {
         this.driftPos = -var8;
         this.driftVelocity = Math.max(0.0F, this.driftVelocity);
      }

      this.driftNoise += (this.random.nextFloat() * 2.0F - 1.0F - this.driftNoise) * clamp(var2 * 5.0F, 0.02F, 0.18F);
      float var9 = (float)(var3 - this.noiseStartNanos) / 1.0E9F;
      float var10 = (float)(Math.sin((double)var9 * 8.7 + 0.4) * 0.35 + Math.sin((double)var9 * 13.1 + 2.2) * 0.22 + Math.sin((double)var9 * 19.6 + 1.1) * 0.12 + (double)(this.driftNoise * 0.3F));
      float var11 = -this.driftPos * lerp(var5, 1.4F, 3.0F);
      float var12 = this.driftVelocity * 0.25F + var11 + var10 * lerp(var5, 0.35F, 0.9F);
      float var13 = 1.0F - 0.85F * smoothStep(72.0F, 88.0F, Math.abs(var1));
      float var14 = lerp(var5, 0.25F, 0.55F) * var13;
      return clamp(var12, -var14, var14);
   }

   public void tick(class_746 var1, class_1309 var2) {
      if (var1 != null && var2 != null) {
         long var4 = System.nanoTime();
         float var6 = this.initialized && this.lastFrameNanos != 0L ? clamp((float)(var4 - this.lastFrameNanos) / 1.0E9F, 0.008333334F, 0.12F) : 0.016666668F;
         this.lastFrameNanos = var4;
         float var7 = 0.0F;
         float var8 = 0.0F;
         float var9 = var1.method_36454();
         float var10 = var1.method_36455();
         if (this.initialized && !Double.isNaN(this.lastPostYaw) && !Double.isNaN(this.lastPostPitch)) {
            float var11 = class_3532.method_15393(var9 - (float)this.lastPostYaw);
            float var12 = var10 - (float)this.lastPostPitch;
            float var13 = (float)Math.sqrt((double)(var11 * var11 + var12 * var12));
            if (var13 > 60.0F) {
               this.opposeTime = 0.0F;
               this.yielding = false;
               this.agreeFrames = 0;
               this.idleTime = 0.0F;
               this.handSignedYaw = 0.0F;
               this.handSignedPitch = 0.0F;
               this.handEnergy = 0.0F;
            } else {
               var7 = var11 - this.ownAppliedYaw;
               var8 = var12 - this.ownAppliedPitch;
            }
         } else {
            this.opposeTime = 0.0F;
            this.yielding = false;
            this.agreeFrames = 0;
            this.idleTime = 0.0F;
            this.handSignedYaw = 0.0F;
            this.handSignedPitch = 0.0F;
            this.handEnergy = 0.0F;
         }

         this.lastPostYaw = (double)var9;
         this.lastPostPitch = (double)var10;
         this.ownAppliedYaw = 0.0F;
         this.ownAppliedPitch = 0.0F;
         float var173 = clamp(var6 * 8.0F, 0.05F, 0.5F);
         this.handSignedYaw += (var7 - this.handSignedYaw) * var173;
         this.handSignedPitch += (var8 - this.handSignedPitch) * var173;
         this.handEnergy += ((float)Math.sqrt((double)(var7 * var7 + var8 * var8)) - this.handEnergy) * var173;
         float var174 = (float)Math.sqrt((double)(this.handSignedYaw * this.handSignedYaw + this.handSignedPitch * this.handSignedPitch));
         boolean var175 = this.handEnergy > 0.005F && var174 < 0.3F * this.handEnergy;
         float var14 = var175 ? 0.0F : var7;
         float var15 = var175 ? 0.0F : var8;
         if (this.noiseStartNanos == 0L) {
            this.noiseStartNanos = var4;
         }

         double[] var16 = this.resolveAimTarget(var1, var2);
         double var17 = var16[0];
         double var19 = var16[1];
         double var21 = var16[2];
         double var23 = var2.method_18798().field_1352;
         double var25 = var2.method_18798().field_1351;
         double var27 = var2.method_18798().field_1350;
         double var29 = var1.method_18798().field_1352;
         double var31 = var1.method_18798().field_1351;
         double var33 = var1.method_18798().field_1350;
         double var35 = var23 - var29;
         double var37 = var25 - var31;
         double var39 = var27 - var33;
         double var41 = var23 + var35 * 0.08;
         double var43 = var25 + var37 * 0.1;
         double var45 = var27 + var39 * 0.08;
         if (!this.aimPointInitialized) {
            this.aimX = var17;
            this.aimY = var19;
            this.aimZ = var21;
            this.leadX = var41;
            this.leadY = var43;
            this.leadZ = var45;
            this.aimPointInitialized = true;
         }

         double var47 = Math.sqrt(var23 * var23 + var27 * var27);
         double var49 = Math.sqrt(var35 * var35 + var39 * var39);
         double var51 = (double)var1.method_5739(var2);
         double var53 = this.closestArea ? 0.36 : 0.34;
         double var55 = class_3532.method_15350(var53 + Math.min(0.35, var49 * (double)0.5F), 0.08, (double)0.75F);
         double var57 = var47 + var49 * (double)0.25F;
         double var59 = class_3532.method_15350(0.18 + Math.min(0.42, var57 * 0.72), 0.12, 0.68);
         double var61 = class_3532.method_15350((double)var6 * (double)60.0F, 0.45, 2.4);
         double var63 = (double)1.0F - Math.pow((double)1.0F - var55, var61);
         double var65 = (double)1.0F - Math.pow((double)1.0F - var59, var61);
         if (!this.yielding) {
            this.aimX += (var17 - this.aimX) * var63;
            this.aimY += (var19 - this.aimY) * var63;
            this.aimZ += (var21 - this.aimZ) * var63;
            this.leadX += (var41 - this.leadX) * var65;
            this.leadY += (var43 - this.leadY) * var65;
            this.leadZ += (var45 - this.leadZ) * var65;
         }

         double var67 = var17 - this.aimX;
         double var69 = var21 - this.aimZ;
         double var71 = 0.3 + var47 * 1.3 + var49 * 0.4;
         if (Math.sqrt(var67 * var67 + var69 * var69) > var71) {
            this.aimX = var17;
            this.aimY = var19;
            this.aimZ = var21;
            this.leadX = var41;
            this.leadY = var43;
            this.leadZ = var45;
         }

         double var73 = class_3532.method_15350(0.35 + var51 * 0.045 + var47 * 0.95 + Math.min(0.18, var49 * 0.12), 0.1, 1.05);
         double var75 = class_3532.method_15350((var51 - 0.8) / (double)2.5F, (double)0.0F, (double)1.0F);
         var73 = Math.max(0.05, var73 * (0.3 + 0.7 * var75));
         double var77 = var1.method_33571().field_1351;
         float var79 = 0.0F;
         if (this.initialized && this.lastEyeY != (double)0.0F) {
            var79 = (float)((var77 - this.lastEyeY) / (double)var6);
         }

         this.verticalVelocity += (var79 - this.verticalVelocity) * 0.65F;
         this.lastEyeY = var77;
         if (var1.method_24828()) {
            this.lastGroundEyeY = var77;
            this.airFactor *= 0.35F;
         } else {
            if (this.lastGroundEyeY == (double)0.0F) {
               this.lastGroundEyeY = var77;
            }

            double var80 = Math.max((double)0.0F, var77 - this.lastGroundEyeY);
            float var82 = (float)Math.min(0.7, Math.max((double)0.0F, (double)this.verticalVelocity) * 0.34);
            float var83 = (float)Math.min(0.82, var80 * 0.58);
            this.airFactor = Math.max(this.airFactor * 0.92F, Math.max(var82, var83));
         }

         double var177 = (double)3.5F * (double)var6;
         double var178 = (double)this.airFactor - this.smoothAir;
         if (var178 > var177) {
            var178 = var177;
         } else if (var178 < -var177) {
            var178 = -var177;
         }

         this.smoothAir += var178;
         double var84 = this.aimY - var77;
         double var86 = class_3532.method_15350(Math.abs(var84) / 0.7, (double)0.0F, (double)1.0F);
         double var88 = Math.min(var73, 0.7 + var51 * 0.04);
         double var90 = this.leadY * var88 * (0.28 + 0.52 * var86);
         double var92 = 0.16 + var51 * 0.055;
         var90 = class_3532.method_15350(var90, -var92, var92);
         double var94 = this.aimX - var1.method_23317();
         double var96 = this.aimZ - var1.method_23321();
         double var98 = this.aimY + var90 - var77;
         double var100 = Math.sqrt(var94 * var94 + var96 * var96);
         float var102 = (float)(Math.toDegrees(Math.atan2(var96, var94)) - (double)90.0F);
         float var103 = (float)(-Math.toDegrees(Math.atan2(var98, Math.max(var100, 1.0E-4))));
         if (this.initialized) {
            float var104 = Math.abs(class_3532.method_15393(var102 - this.lastTargetYaw));
            float var105 = Math.abs(class_3532.method_15393(var103 - this.lastTargetPitch));
            float var106 = this.closestArea ? 20.0F : 12.0F;
            float var107 = clamp((Math.max(var104, var105) - var106) / 50.0F, 0.0F, 1.0F);
            this.overshoot = this.yielding ? this.overshoot : Math.max(this.overshoot, var107);
            if (var107 > 0.3F) {
               this.aimStrength *= 0.3F;
               this.aimX = var17;
               this.aimY = var19;
               this.aimZ = var21;
            }

            float var108 = 1.0F + Math.min(2.0F, var104 / 60.0F);
            float var109 = 1.0F + Math.min(2.0F, var105 / 60.0F);
            float var110 = (120.0F + (float)(var49 * (double)700.0F)) * var108;
            float var111 = (95.0F + (float)(Math.abs(this.leadY) * (double)550.0F)) * var109;
            float var112 = Math.abs(class_3532.method_15393(var102 - var1.method_36454()));
            var110 *= 1.0F + smoothStep(10.0F, 35.0F, var112) * 1.5F;
            var110 = clamp(var110, 90.0F, 1080.0F);
            var111 = clamp(var111, 70.0F, 500.0F);
            float var113 = clamp(class_3532.method_15393(var102 - this.lastTargetYaw), -var110 * var6, var110 * var6);
            float var114 = clamp(class_3532.method_15393(var103 - this.lastTargetPitch), -var111 * var6, var111 * var6);
            var102 = this.lastTargetYaw + var113;
            var103 = this.lastTargetPitch + var114;
         }

         if (!this.yielding) {
            this.overshoot *= (float)Math.pow(0.02, (double)var6);
         }

         if (this.overshoot < 0.01F) {
            this.overshoot = 0.0F;
         }

         float var180 = 1.0F + 3.0F * this.overshoot;
         float var181 = var1.method_36454();
         float var182 = var1.method_36455();
         float var183 = class_3532.method_15393(var102 - var181);
         float var184 = this.aimVertically ? class_3532.method_15393(var103 - var182) : 0.0F;
         float var185 = Math.abs(var183);
         float var188 = Math.abs(var184);
         float var190 = (float)Math.sqrt((double)(var185 * var185 + var188 * var188));
         float var191 = 1.0F - smoothStep(1.5F, 8.0F, var190);
         if (this.initialized) {
            float var192 = -(var185 - Math.abs(this.lastYawDiff)) / var6;
            var191 = clamp(var191 + clamp(var192 / 20.0F, -0.3F, 0.3F), 0.0F, 1.0F);
         }

         float var193 = var191 > this.aimStrength ? clamp(var6 * 3.0F, 0.01F, 0.25F) : clamp(var6 * 20.0F, 0.05F, 0.8F);
         this.aimStrength += (var191 - this.aimStrength) * var193;
         float var194 = this.aimStrength;
         float var115 = 0.0F;
         float var116 = 0.0F;
         float var117 = 0.0F;
         float var118 = 0.0F;
         if (this.initialized) {
            float var119 = class_3532.method_15393(var102 - this.lastTargetYaw) / var6;
            float var120 = class_3532.method_15393(var103 - this.lastTargetPitch) / var6;
            var117 = class_3532.method_15393(var181 - this.lastPlayerYaw) / var6;
            var118 = class_3532.method_15393(var182 - this.lastPlayerPitch) / var6;
            float var121 = clamp(var6 * 12.0F, 0.05F, 0.45F);
            if (!this.yielding) {
               this.yawAccel += (var119 - this.yawAccel) * var121;
               this.pitchAccel += (var120 - this.pitchAccel) * var121;
            }

            var115 = this.yawAccel;
            var116 = this.pitchAccel;
         }

         float var195 = (float)this.horizontalSpeed * 0.75F;
         float var196 = (float)this.verticalSpeed * 0.75F;
         float var197 = clamp((var195 - 10.0F) / 90.0F, 0.0F, 1.0F);
         float var122 = clamp((var196 - 10.0F) / 90.0F, 0.0F, 1.0F);
         if (!this.yielding) {
            if (Math.signum(var183) != Math.signum(this.lastYawDiff) && var185 > 0.1F && Math.abs(this.lastYawDiff) > 0.1F) {
               this.yawBias *= 0.3F;
            }

            if (Math.signum(var184) != Math.signum(this.lastPitchDiff) && var188 > 0.1F && Math.abs(this.lastPitchDiff) > 0.1F) {
               this.pitchBias *= 0.3F;
            }
         }

         float var123 = var194 * var194;
         if (!this.yielding) {
            this.yawBias += var183 * var6 * var123 * (1.0F - var197);
            this.pitchBias += var184 * var6 * var123 * (1.0F - var122);
            float var124 = 1.0F - (1.0F - var194) * clamp(var6 * 5.0F, 0.0F, 0.5F);
            this.yawBias *= var124;
            this.pitchBias *= var124;
         }

         float var198 = 15.0F * (1.0F - var197 * 0.9F);
         float var125 = 10.0F * (1.0F - var122 * 0.9F);
         this.yawBias = clamp(this.yawBias, -var198, var198);
         this.pitchBias = clamp(this.pitchBias, -var125, var125);
         float var126 = this.initialized ? (var183 - this.lastYawDiff) / var6 : 0.0F;
         float var127 = this.initialized ? (var184 - this.lastPitchDiff) / var6 : 0.0F;
         if (!this.yielding) {
            this.yawVelocity = this.yawVelocity * 0.85F + var126 * 0.15F;
            this.pitchVelocity = this.pitchVelocity * 0.85F + var127 * 0.15F;
         }

         float var128 = this.closestArea ? 1.5F : 0.5F;
         float var129 = Math.signum(var183);
         this.yawFlickTicks = var129 != this.lastYawSign && var185 > var128 ? Math.min(this.yawFlickTicks + 1.0F, 8.0F) : Math.max(0.0F, this.yawFlickTicks - var6 * 3.0F);
         this.lastYawSign = var129;
         float var130 = Math.signum(var184);
         this.pitchFlickTicks = var130 != this.lastPitchSign && var188 > var128 ? Math.min(this.pitchFlickTicks + 1.0F, 8.0F) : Math.max(0.0F, this.pitchFlickTicks - var6 * 3.0F);
         this.lastPitchSign = var130;
         float var131 = clamp(this.yawFlickTicks / 5.0F, 0.0F, 1.0F);
         float var132 = clamp(this.pitchFlickTicks / 5.0F, 0.0F, 1.0F);
         float var133 = Math.min(var195, 10.0F);
         float var134 = Math.min(var196, 10.0F);
         float var135 = (var133 - 1.0F) / 9.0F;
         float var136 = (var134 - 1.0F) / 9.0F;
         var135 *= var135;
         var136 *= var136;
         float var137 = 0.15F + 0.85F * clamp(var135, 0.0F, 1.0F);
         float var138 = 0.15F + 0.85F * clamp(var136, 0.0F, 1.0F);
         float var139 = lerp(var194, 8.0F, 2.5F + var133 * 0.15F) * var137;
         float var140 = lerp(var194, 7.0F, 2.2F + var134 * 0.13F) * var138;
         float var141 = lerp(var194, 0.15F, 0.8F + var133 * 0.04F) * var137;
         float var142 = lerp(var194, 0.12F, 0.65F + var134 * 0.035F) * var138;
         float var143 = lerp(var194, 0.08F, 0.25F) * var137;
         float var144 = lerp(var194, 0.06F, 0.2F) * var138;
         float var145 = (0.85F + var133 * 0.015F) * var137;
         float var146 = (0.82F + var134 * 0.013F) * var138;
         float var147 = lerp(var194, 0.1F, 0.3F);
         float var148 = lerp(var194, 0.08F, 0.25F);
         var140 *= 1.0F - 0.6F * var132;
         var144 *= 1.0F + 2.0F * var132;
         float var149 = var139 * (1.0F - 0.6F * var131) * var183 + var141 * this.yawBias + var143 * (1.0F + 2.0F * var131) * this.yawVelocity + var145 * var115 - var147 * var117;
         float var150 = 0.0F;
         if (this.aimVertically) {
            var150 = var140 * var184 + var142 * this.pitchBias + var144 * this.pitchVelocity + var146 * var116 - var148 * var118;
         } else {
            this.pitchBias = 0.0F;
            this.pitchVelocity = 0.0F;
         }

         if (!this.yielding) {
            var150 += this.computePitchDrift(var182, var6, var4);
         }

         float var151 = (class_310.method_1551().field_1690.field_1913.method_1434() ? 1.0F : 0.0F) - (class_310.method_1551().field_1690.field_1849.method_1434() ? 1.0F : 0.0F);
         if (this.strafeIncrease && Math.abs(var151) > 0.01F) {
            boolean var152 = var183 > 0.0F;
            if (var152 && var151 < 0.0F || !var152 && var151 > 0.0F) {
               var149 *= 1.15F;
            }
         }

         float var208 = smoothStep(8.0F, 40.0F, var185);
         float var153 = 1.0F + var208 * 2.5F;
         float var154 = (22.0F + var133 * 15.0F) * var137 * var180 * var153;
         float var155 = (18.0F + var134 * 13.0F) * var138 * var180;
         float var156 = (Math.abs(var115) * 0.4F + 18.0F) * (0.35F + var137 * 0.65F) * var153;
         float var157 = (Math.abs(var116) * 0.38F + 14.0F) * (0.35F + var138 * 0.65F);
         float var158 = Math.min(400.0F * var180 * var153, Math.max(var154, var156));
         float var159 = Math.min(300.0F * var180, Math.max(var155, var157));
         var149 = clamp(var149, -var158, var158);
         var150 = clamp(var150, -var159, var159);
         float var160 = smoothStep(0.5F, 3.0F, (float)var51);
         float var161 = lerp(var208, 0.15F, 0.65F);
         var149 *= lerp(var194, var161 + (1.0F - var161) * var160, 0.4F + 0.6F * var160);
         var150 *= lerp(var194, 0.2F + 0.8F * var160, 0.45F + 0.55F * var160);
         float var162 = var149 * var6;
         float var163 = var150 * var6;
         if (!this.yieldToMouse) {
            this.yielding = false;
            this.opposeTime = 0.0F;
            this.agreeFrames = 0;
            this.idleTime = 0.0F;
         } else {
            boolean var164 = Math.abs(this.yawAccel) > 60.0F;
            float var165 = (float)Math.sqrt((double)(var14 * var14 + var15 * var15));
            if (this.yielding) {
               if (var164) {
                  this.yielding = false;
                  this.opposeTime = 0.0F;
                  this.agreeFrames = 0;
                  this.idleTime = 0.0F;
                  this.refractoryUntilNanos = var4 + 200000000L;
               } else {
                  float var166 = var14 * var183 + var15 * var184;
                  if (var166 > 0.0F && var165 > 0.02F) {
                     ++this.agreeFrames;
                  } else {
                     this.agreeFrames = 0;
                  }

                  if (var165 < 0.02F) {
                     this.idleTime += var6;
                  } else {
                     this.idleTime = 0.0F;
                  }

                  if (this.agreeFrames >= 2 || this.idleTime >= 0.3F && var190 < 8.0F) {
                     this.yielding = false;
                     this.opposeTime = 0.0F;
                     this.agreeFrames = 0;
                     this.idleTime = 0.0F;
                     this.refractoryUntilNanos = var4 + 200000000L;
                  }
               }
            }

            if (!this.yielding) {
               boolean var214 = false;
               if (!var164) {
                  float var167 = (float)Math.sqrt((double)(var162 * var162 + var163 * var163));
                  if (var165 > 0.02F && var167 > 0.015F) {
                     float var168 = var14 * var162 + var15 * var163;
                     if (var168 < 0.0F && var14 * var183 + var15 * var184 <= 0.0F) {
                        var214 = true;
                     }
                  }
               }

               if (var214) {
                  this.opposeTime += var6;
               } else {
                  this.opposeTime = Math.max(0.0F, this.opposeTime - 2.0F * var6);
               }

               float var217 = var4 < this.refractoryUntilNanos ? 0.2F : 0.12F;
               if (this.opposeTime >= var217) {
                  this.yielding = true;
               }
            }
         }

         if (this.yielding) {
            this.aimStrength = 0.0F;
            var149 = 0.0F;
            var150 = 0.0F;
         } else {
            if (this.opposeTime > 0.0F) {
               float var209 = 1.0F - 0.7F * Math.min(1.0F, this.opposeTime / 0.12F);
               var149 *= var209;
               var150 *= var209;
            }

            float var210 = (float)(var4 - this.noiseStartNanos) / 1.0E9F;
            float var212 = (float)(Math.sin((double)var210 * 62.83) * 0.4 + Math.sin((double)var210 * 47.12) * (double)0.25F + Math.sin((double)var210 * 78.54) * 0.15);
            float var215 = (float)(Math.sin((double)var210 * 56.55 + 1.3) * 0.35 + Math.sin((double)var210 * 43.98 + 0.7) * 0.2 + Math.sin((double)var210 * 72.26 + 2.1) * 0.12);
            float var218 = (float)(Math.sin((double)var210 * 12.57) * 0.8 + Math.sin((double)var210 * 7.85) * (double)0.5F);
            float var220 = (float)(Math.sin((double)var210 * 10.47 + 0.9) * 0.6 + Math.sin((double)var210 * 5.65 + 1.8) * 0.4);
            float var169 = (float)(Math.sin((double)var210 * 1.26) * 0.3);
            float var170 = (float)(Math.sin((double)var210 * 0.94 + (double)0.5F) * 0.2);
            float var171 = (0.15F + 0.85F * var194) * 0.85F;
            float var172 = 0.5F + 0.5F * (1.0F - var135);
            var149 += (var212 + var218 + var169) * var171 * var172 * 1.5F;
            if (this.aimVertically) {
               var150 += (var215 + var220 + var170) * var171 * var172;
            }
         }

         float var3;
         if ((var3 = MouseRotation.degreesPerCount()) > 1.0E-5F) {
            float var211 = var149 * var6 / var3;
            float var213 = var150 * var6 / var3;
            if (this.directCorrection) {
               float var216 = 0.35F;
               float var219 = (float)this.horizontalSpeed * 2.0F;
               float var221 = (float)this.verticalSpeed * 2.0F;
               float var222 = clamp(var183 / var3, -var219, var219);
               float var223 = this.aimVertically ? clamp(var184 / var3, -var221, var221) : 0.0F;
               this.pendingYaw += var211 * (1.0F - var216) + var222 * var216;
               this.pendingPitch += var213 * (1.0F - var216) + var223 * var216;
            } else {
               this.pendingYaw += var211;
               this.pendingPitch += var213;
            }
         }

         this.initialized = true;
         this.lastYawDiff = var183;
         this.lastPitchDiff = var184;
         this.lastTargetYaw = var102;
         this.lastTargetPitch = var103;
         this.lastPlayerYaw = var181;
         this.lastPlayerPitch = var182;
         this.flush(var1);
      }
   }

   private void flush(class_746 var1) {
      int var2 = (int)this.pendingYaw;
      int var3 = (int)this.pendingPitch;
      this.pendingYaw -= (float)var2;
      this.pendingPitch -= (float)var3;
      if (var2 != 0 || var3 != 0) {
         float var4 = MouseRotation.mouseScale();
         var1.method_5872((double)((float)var2 * var4), (double)((float)var3 * var4));
         RotationSync.dirty = true;
         float var5 = MouseRotation.degreesPerCount();
         this.ownAppliedYaw += (float)var2 * var5;
         this.ownAppliedPitch += (float)var3 * var5;
         HumanDiag.tickYawSpent += (double)Math.abs((float)var2 * var5);
         Diagnostics.lastYawStep = (double)((float)var2 * var5);
      }
   }

   public boolean isYielding() {
      return this.yielding;
   }

   public float strength() {
      return this.aimStrength;
   }
}
