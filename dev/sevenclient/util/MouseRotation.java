package dev.sevenclient.util;

import java.util.Random;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public final class MouseRotation {
   public static final int ACCEL_NONE = 0;
   public static final int ACCEL_LINEAR = 1;
   public static final int ACCEL_CUBIC = 2;
   public static final int ACCEL_ANGLE = 3;
   private float pendingYaw;
   private float pendingPitch;
   private float targetYaw = Float.NaN;
   private float targetPitch = Float.NaN;
   private float speed = 48.0F;
   private float tolerance = 0.0F;
   private int accel = 1;
   private boolean scaleAxes = true;
   private boolean clampToRemaining = true;
   private boolean complete = true;
   private final Random random = new Random();
   private long lastJitterRoll = 0L;
   private float yawJitterPhase;
   private float pitchJitterPhase;
   private float yawJitterSpeed;
   private float pitchJitterSpeed;
   private float yawJitterAmp;
   private float pitchJitterAmp;
   private float jitterScale = 0.0F;

   public MouseRotation speed(double v) {
      this.speed = (float)v;
      return this;
   }

   public MouseRotation tolerance(double v) {
      this.tolerance = (float)v;
      return this;
   }

   public MouseRotation accel(int mode) {
      this.accel = mode;
      return this;
   }

   public MouseRotation scaleAxes(boolean v) {
      this.scaleAxes = v;
      return this;
   }

   public MouseRotation clampToRemaining(boolean v) {
      this.clampToRemaining = v;
      return this;
   }

   public MouseRotation jitter(double scale) {
      this.jitterScale = (float)scale;
      return this;
   }

   public boolean isComplete() {
      return this.complete;
   }

   public float pendingYawCounts() {
      return this.pendingYaw;
   }

   public float pendingPitchCounts() {
      return this.pendingPitch;
   }

   public void reset() {
      this.pendingYaw = 0.0F;
      this.pendingPitch = 0.0F;
      this.targetYaw = Float.NaN;
      this.targetPitch = Float.NaN;
      this.complete = true;
   }

   public void setTarget(float yaw, float pitch) {
      this.targetYaw = yaw;
      this.targetPitch = pitch;
      this.complete = false;
   }

   public static float mouseScale() {
      double sens;
      try {
         sens = (Double)class_310.method_1551().field_1690.method_42495().method_41753();
      } catch (Throwable var4) {
         sens = (double)0.5F;
      }

      double f = sens * 0.6 + 0.2;
      return (float)(f * f * f * (double)8.0F);
   }

   public static float degreesPerCount() {
      return mouseScale() * 0.15F;
   }

   public static float[] snapToLattice(float wantYaw, float wantPitch, float curYaw, float curPitch) {
      float step = degreesPerCount();
      if (step <= 0.0F) {
         return new float[]{wantYaw, wantPitch};
      } else {
         int yawCounts = Math.round(class_3532.method_15393(wantYaw - curYaw) / step);
         int pitchCounts = Math.round(class_3532.method_15393(wantPitch - curPitch) / step);
         return new float[]{curYaw + step * (float)yawCounts, curPitch + step * (float)pitchCounts};
      }
   }

   public static float[] lookAt(class_243 eye, class_243 point) {
      double dx = point.field_1352 - eye.field_1352;
      double dy = point.field_1351 - eye.field_1351;
      double dz = point.field_1350 - eye.field_1350;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - (double)90.0F);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizontal)));
      return new float[]{class_3532.method_15393(yaw), class_3532.method_15363(pitch, -90.0F, 90.0F)};
   }

   private boolean outsideTolerance(double absError) {
      float perStep = degreesPerCount();
      if (perStep <= 0.0F) {
         return false;
      } else {
         return Math.round(absError / (double)perStep) > Math.max((long)Math.round(this.tolerance / perStep), 0L);
      }
   }

   private double accelerateYaw(double step, double absError) {
      switch (this.accel) {
         case 1:
            return step + absError * 0.05;
         case 2:
            double s = absError / (double)100.0F + 0.7;
            double m = 0.4 + (double)2.0F * s * s * s + s * s;
            return step * Math.min(Math.max((double)1.0F, m), (double)4.0F);
         case 3:
            return step * ((double)225.0F + absError) / (double)180.0F;
         default:
            return step;
      }
   }

   private double acceleratePitch(double step, double absError) {
      switch (this.accel) {
         case 1:
            return step + absError * 0.05;
         case 2:
            double s = absError / (double)75.0F + 0.7;
            double m = 0.4 + (double)2.0F * s * s * s + s * s;
            return step * Math.max((double)1.0F, m);
         case 3:
            return step * ((double)135.0F + absError) / (double)90.0F;
         default:
            return step;
      }
   }

   private float addPendingStep(float pending, double error, double step) {
      if (this.clampToRemaining) {
         double remaining = Math.abs(error / (double)degreesPerCount());
         step = Math.min(step, remaining);
      }

      return (float)(error > (double)0.0F ? (double)pending + step : (double)pending - step);
   }

   private void rollJitter() {
      long now = System.currentTimeMillis();
      if (now - this.lastJitterRoll > 200L + (long)this.random.nextInt(300)) {
         this.lastJitterRoll = now;
         this.yawJitterSpeed = 0.05F + this.random.nextFloat() * 0.15F;
         this.pitchJitterSpeed = 0.04F + this.random.nextFloat() * 0.12F;
         this.yawJitterAmp = 0.15F + this.random.nextFloat() * 0.25F;
         this.pitchJitterAmp = 0.1F + this.random.nextFloat() * 0.2F;
      }

      this.yawJitterPhase += this.yawJitterSpeed;
      this.pitchJitterPhase += this.pitchJitterSpeed;
      if (this.yawJitterPhase > ((float)Math.PI * 2F)) {
         this.yawJitterPhase -= ((float)Math.PI * 2F);
      }

      if (this.pitchJitterPhase > ((float)Math.PI * 2F)) {
         this.pitchJitterPhase -= ((float)Math.PI * 2F);
      }

   }

   private float yawJitter() {
      return (float)(Math.sin((double)this.yawJitterPhase) * (double)this.yawJitterAmp + Math.sin((double)(this.yawJitterPhase * 2.7F + 1.3F)) * (double)this.yawJitterAmp * 0.3);
   }

   private float pitchJitter() {
      return (float)(Math.sin((double)this.pitchJitterPhase) * (double)this.pitchJitterAmp + Math.sin((double)(this.pitchJitterPhase * 3.1F + 0.7F)) * (double)this.pitchJitterAmp * (double)0.25F);
   }

   public boolean update(class_746 player, boolean pitchEnabled) {
      if (player != null && !Float.isNaN(this.targetYaw)) {
         float perStep = degreesPerCount();
         if (perStep <= 0.0F) {
            return true;
         } else {
            if (this.jitterScale > 0.0F) {
               this.rollJitter();
            }

            float curYaw = player.method_36454();
            float curPitch = player.method_36455() == -90.0F ? -89.99F : player.method_36455();
            float predYaw = curYaw + (float)((int)this.pendingYaw) * perStep;
            float predPitch = curPitch + (float)((int)this.pendingPitch) * perStep;
            double yawError = (double)class_3532.method_15393(this.targetYaw - predYaw);
            double pitchError = (double)class_3532.method_15393(this.targetPitch - predPitch);
            if (this.jitterScale > 0.0F) {
               yawError += (double)(this.yawJitter() * this.jitterScale);
               pitchError += (double)(this.pitchJitter() * this.jitterScale);
            }

            double absYaw = Math.abs(yawError);
            double absPitch = Math.abs(pitchError);
            boolean yawDone = true;
            boolean pitchDone = true;
            if (this.outsideTolerance(absYaw)) {
               double step = (double)this.speed * (double)0.25F;
               if (this.scaleAxes && absPitch > 1.0E-4) {
                  double ratio = absYaw / absPitch;
                  if (ratio < (double)1.0F) {
                     step *= ratio;
                  }
               }

               step = this.accelerateYaw(step, absYaw);
               this.pendingYaw = this.addPendingStep(this.pendingYaw, yawError, step);
               yawDone = false;
            }

            if (pitchEnabled && this.outsideTolerance(absPitch)) {
               double step = (double)this.speed * (double)0.25F;
               if (this.scaleAxes && absYaw > 1.0E-4) {
                  double ratio = absPitch / absYaw;
                  if (ratio < (double)1.0F) {
                     step *= ratio;
                  }
               }

               step = this.acceleratePitch(step, absPitch);
               this.pendingPitch = this.addPendingStep(this.pendingPitch, pitchError, step);
               pitchDone = false;
            }

            int yawCounts = (int)this.pendingYaw;
            int pitchCounts = pitchEnabled ? (int)this.pendingPitch : 0;
            if (yawCounts != 0) {
               float newYaw = curYaw + (float)yawCounts * perStep;
               player.method_36456(newYaw);
               player.method_5847(newYaw);
               this.pendingYaw -= (float)yawCounts;
               HumanDiag.tickYawSpent += (double)Math.abs((float)yawCounts * perStep);
            }

            if (pitchCounts != 0) {
               player.method_36457(class_3532.method_15363(curPitch + (float)pitchCounts * perStep, -90.0F, 90.0F));
               this.pendingPitch -= (float)pitchCounts;
            }

            if (yawCounts != 0 || pitchCounts != 0) {
               RotationSync.dirty = true;
               Diagnostics.lastYawStep = (double)((float)yawCounts * perStep);
            }

            this.complete = yawDone && pitchDone && Math.abs(this.pendingYaw) < 1.0F && Math.abs(this.pendingPitch) < 1.0F;
            return this.complete;
         }
      } else {
         return true;
      }
   }
}
