package dev.sevenclient.module.impl;

import dev.sevenclient.mixin.MinecraftClientAccessor;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.HumanRandom;
import dev.sevenclient.util.Rotations;
import net.minecraft.class_1799;
import net.minecraft.class_3532;
import net.minecraft.class_7923;

public class FastXP extends Module {
   private final NumberSetting cps = (NumberSetting)this.reg(new NumberSetting("CPS", (double)4.0F, (double)1.0F, (double)20.0F, (double)1.0F));
   private final BoolSetting bypassCooldown = (BoolSetting)this.reg(new BoolSetting("Bypass Vanilla Cooldown", false));
   private final NumberSetting pitchNoise = (NumberSetting)this.reg(new NumberSetting("Pitch Noise", 0.2, (double)0.0F, (double)0.5F, 0.05));
   private final BoolSetting onlyWithBottles = (BoolSetting)this.reg(new BoolSetting("Require Bottles", true));
   private final BoolSetting restoreRotation = (BoolSetting)this.reg(new BoolSetting("Restore Pitch", true));
   private final double[] residual = new double[2];
   private float basePitch = 0.0F;
   private boolean pressed = false;
   private double accumulator = (double)0.0F;

   public FastXP() {
      super("FastXP", "Throws experience bottles at up to 20 CPS through the real use path.", Category.PLAYER);
      this.registerBindSettings();
   }

   public void onEnable() {
      if (mc.field_1724 != null) {
         this.basePitch = mc.field_1724.method_36455();
      }

      this.accumulator = (double)0.0F;
   }

   public void onDisable() {
      if (this.pressed) {
         mc.field_1690.field_1904.method_23481(false);
         this.pressed = false;
      }

      if (this.restoreRotation.is() && mc.field_1724 != null) {
         mc.field_1724.method_36457(class_3532.method_15363(this.basePitch, -90.0F, 90.0F));
      }

      this.residual[0] = (double)0.0F;
      this.residual[1] = (double)0.0F;
   }

   public void onTick() {
      if (mc.field_1724 != null) {
         if (this.onlyWithBottles.is() && !this.holdingBottle()) {
            if (this.pressed) {
               mc.field_1690.field_1904.method_23481(false);
               this.pressed = false;
            }
         } else {
            this.accumulator += this.cps.val() / (double)20.0F;
            boolean fireThisTick = this.accumulator >= (double)1.0F;
            if (fireThisTick) {
               --this.accumulator;
            }

            if (!fireThisTick) {
               if (this.pressed) {
                  mc.field_1690.field_1904.method_23481(false);
                  this.pressed = false;
               }
            } else {
               if (this.pitchNoise.val() > (double)0.0F) {
                  double drift = HumanRandom.tremor((double)System.nanoTime() / (double)1.0E9F) * this.pitchNoise.val();
                  float target = class_3532.method_15363(this.basePitch + (float)drift, -90.0F, 90.0F);
                  float delta = Rotations.snap((double)(target - mc.field_1724.method_36455()), this.residual, 1);
                  if (delta != 0.0F) {
                     mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455() + delta, -90.0F, 90.0F));
                  }
               }

               if (this.bypassCooldown.is()) {
                  try {
                     ((MinecraftClientAccessor)mc).seven$setItemUseCooldown(0);
                  } catch (Throwable var6) {
                  }
               }

               mc.field_1690.field_1904.method_23481(true);
               this.pressed = true;
            }
         }
      }

   }

   public void onPostTick() {
      if (this.pressed) {
         mc.field_1690.field_1904.method_23481(false);
         this.pressed = false;
      }

   }

   private boolean holdingBottle() {
      return isBottle(mc.field_1724.method_6047()) || isBottle(mc.field_1724.method_6079());
   }

   private static boolean isBottle(class_1799 stack) {
      return stack != null && !stack.method_7960() ? class_7923.field_41178.method_10221(stack.method_7909()).method_12832().equals("experience_bottle") : false;
   }
}
