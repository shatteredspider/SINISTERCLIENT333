package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.Human;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.KeyPulse;
import dev.sevenclient.util.Rotations;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;

public class JumpReset extends Module {
   private final NumberSetting chance = (NumberSetting)this.reg(new NumberSetting("Chance", (double)92.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting accuracy = (NumberSetting)this.reg(new NumberSetting("Accuracy", (double)78.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting minHorizontal = (NumberSetting)this.reg(new NumberSetting("Min Knockback", 0.08, (double)0.0F, 0.6, 0.01));
   private final BoolSetting groundOnly = (BoolSetting)this.reg(new BoolSetting("Ground Only", true));
   private final BoolSetting requireSprint = (BoolSetting)this.reg(new BoolSetting("Require Sprint", false));
   private final BoolSetting waterCheck = (BoolSetting)this.reg(new BoolSetting("Water Check", true));
   private final BoolSetting facingOnly = (BoolSetting)this.reg(new BoolSetting("Only When Facing", false));
   private final NumberSetting facingFov = (NumberSetting)this.reg(new NumberSetting("Facing FOV", (double)90.0F, (double)20.0F, (double)180.0F, (double)5.0F));
   private static volatile JumpReset active;
   private static volatile boolean pending = false;
   private KeyPulse jump;
   private int armedIn = -1;

   public JumpReset() {
      super("JumpReset", "Jumps on the knockback tick to convert knockback into height.", Category.MOVEMENT);
      NumberSetting var10000 = this.facingFov;
      BoolSetting var10001 = this.facingOnly;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   private KeyPulse jump() {
      if (this.jump == null) {
         this.jump = new KeyPulse(mc.field_1690.field_1903);
      }

      return this.jump;
   }

   public void onEnable() {
      active = this;
      this.armedIn = -1;
      pending = false;
   }

   public void onDisable() {
      active = null;
      pending = false;
      this.jump().release();
      this.armedIn = -1;
   }

   public static void onServerKnockback(class_243 velocity) {
      JumpReset self = active;
      if (self != null && self.isEnabled() && velocity != null) {
         if (self.suitable(velocity) && self.conditionsOk()) {
            if (Human.roll(self.chance.val())) {
               pending = true;
            }

         }
      }
   }

   private boolean suitable(class_243 v) {
      double horizontal = Math.sqrt(v.field_1352 * v.field_1352 + v.field_1350 * v.field_1350);
      return horizontal >= this.minHorizontal.val() && v.field_1351 > (double)0.0F;
   }

   private boolean conditionsOk() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.waterCheck.is() || !mc.field_1724.method_5799() && !mc.field_1724.method_5771()) {
            if (this.groundOnly.is() && !mc.field_1724.method_24828()) {
               return false;
            } else if (this.requireSprint.is() && !mc.field_1724.method_5624()) {
               return false;
            } else if (mc.field_1690.field_1903.method_1434() && !this.jump().active()) {
               return false;
            } else {
               return !this.facingOnly.is() || this.facingAttacker();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean facingAttacker() {
      double best = Double.MAX_VALUE;

      for(class_1297 e : mc.field_1687.method_18112()) {
         if (e instanceof class_1657 && e != mc.field_1724 && e.method_5805() && !(mc.field_1724.method_5739(e) > 6.0F)) {
            float[] rot = Rotations.to(mc.field_1724.method_33571(), e.method_33571());
            best = Math.min(best, (double)Math.abs(Rotations.wrap(rot[0] - mc.field_1724.method_36454())));
         }
      }

      return best <= this.facingFov.val() / (double)2.0F;
   }

   public void onTick() {
      if (mc.field_1724 != null) {
         this.jump().tick();
         if (pending) {
            pending = false;
            if (Human.flat(this.accuracy.val())) {
               this.armedIn = Human.lateness();
               if (this.armedIn == 0) {
                  this.armedIn = -1;
                  this.fire();
               }
            }

         } else {
            if (this.armedIn >= 0) {
               if (this.armedIn == 0) {
                  this.armedIn = -1;
                  this.fire();
               } else {
                  --this.armedIn;
               }
            }

         }
      }
   }

   private void fire() {
      this.jump().press(Human.tapHold());
      Human.act(60L);
      ++HumanDiag.jumpResets;
   }
}
