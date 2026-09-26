package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.HumanRandom;
import dev.sevenclient.util.MouseRotation;
import dev.sevenclient.util.Rotations;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_3532;
import net.minecraft.class_3966;

public class HitFlick extends Module {
   private final NumberSetting angle = (NumberSetting)this.reg(new NumberSetting("Angle", (double)90.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting chance = (NumberSetting)this.reg(new NumberSetting("Chance", (double)100.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting flickDelay = (NumberSetting)this.reg(new NumberSetting("Flick Delay MS", (double)250.0F, (double)0.0F, (double)2000.0F, (double)25.0F));
   private final BoolSetting randomizeOffset = (BoolSetting)this.reg(new BoolSetting("Randomize Offset", false));
   private final NumberSetting randomizeRange = (NumberSetting)this.reg(new NumberSetting("Offset Range", (double)20.0F, (double)0.0F, (double)180.0F, (double)1.0F));
   private final BoolSetting strafeInvert = (BoolSetting)this.reg(new BoolSetting("Strafe Invert", false));
   private final BoolSetting selectHits = (BoolSetting)this.reg(new BoolSetting("Select Hits", true));
   private final NumberSetting flickSpeed = (NumberSetting)this.reg(new NumberSetting("Flick Speed", (double)48.0F, (double)4.0F, (double)200.0F, (double)1.0F));
   private final NumberSetting returnSpeed = (NumberSetting)this.reg(new NumberSetting("Return Speed", (double)30.0F, (double)4.0F, (double)200.0F, (double)1.0F));
   private final NumberSetting maxTicks = (NumberSetting)this.reg(new NumberSetting("Max Flick Ticks", (double)4.0F, (double)1.0F, (double)10.0F, (double)1.0F));
   private final BoolSetting returnAfter = (BoolSetting)this.reg(new BoolSetting("Return After", true));
   private static final int IDLE = 0;
   private static final int FLICKING = 1;
   private static final int RETURNING = 2;
   private final MouseRotation rot = new MouseRotation();
   private int state = 0;
   private int ticks = 0;
   private float savedYaw = 0.0F;
   private float flickYaw = 0.0F;
   private long lastFlick = 0L;
   private boolean rolled = false;

   public HitFlick() {
      super("HitFlick", "Steers sprint knockback by flicking yaw before the hit.", Category.COMBAT);
      NumberSetting var10000 = this.randomizeRange;
      BoolSetting var10001 = this.randomizeOffset;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.returnSpeed;
      var10001 = this.returnAfter;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   public void onDisable() {
      this.idle();
   }

   private void idle() {
      this.state = 0;
      this.ticks = 0;
      this.rolled = false;
      this.rot.reset();
   }

   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.state == 1) {
            if (++this.ticks > (int)this.maxTicks.val()) {
               this.beginReturn();
            }

         } else if (this.state != 2) {
            if (!mc.field_1690.field_1886.method_1434()) {
               this.rolled = false;
            } else if (System.currentTimeMillis() - this.lastFlick >= (long)this.flickDelay.val()) {
               class_1657 target = this.crosshairPlayer();
               if (target == null) {
                  this.rolled = false;
               } else if (mc.field_1724.method_5624()) {
                  if (!this.selectHits.is() || target.field_6235 <= 0) {
                     if (mc.field_1724.method_7261(0.0F) < 0.88F) {
                        this.rolled = false;
                     } else if (!this.rolled) {
                        this.rolled = true;
                        if (HumanRandom.chance(this.chance.val())) {
                           this.beginFlick(target);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void beginFlick(class_1657 target) {
      float toTarget = Rotations.to(mc.field_1724.method_33571(), target.method_33571())[0];
      double bearing = this.angle.val();
      if (this.randomizeOffset.is() && this.randomizeRange.val() > (double)0.0F) {
         bearing += HumanRandom.uniform(-this.randomizeRange.val() / (double)2.0F, this.randomizeRange.val() / (double)2.0F);
      }

      if (this.strafeInvert.is()) {
         boolean pushingRight = bearing > (double)0.0F && bearing < (double)180.0F;
         boolean pushingLeft = bearing > (double)180.0F && bearing < (double)360.0F;
         if (pushingRight && mc.field_1690.field_1849.method_1434()) {
            bearing = (double)360.0F - bearing;
         } else if (pushingLeft && mc.field_1690.field_1913.method_1434()) {
            bearing = (double)360.0F - bearing;
         }
      }

      this.savedYaw = mc.field_1724.method_36454();
      this.flickYaw = class_3532.method_15393((float)((double)toTarget + bearing));
      this.state = 1;
      this.ticks = 0;
      this.lastFlick = System.currentTimeMillis();
      this.rot.speed(this.flickSpeed.val()).tolerance((double)0.0F).accel(0).scaleAxes(false).clampToRemaining(true).jitter((double)0.0F);
      ++Diagnostics.hitFlickArmed;
   }

   private void beginReturn() {
      if (!this.returnAfter.is()) {
         this.idle();
      } else {
         this.state = 2;
         this.ticks = 0;
         this.rot.speed(this.returnSpeed.val()).tolerance((double)0.0F).accel(1).scaleAxes(true).clampToRemaining(true);
      }
   }

   public void onFrame(float dt) {
      if (mc.field_1724 != null && this.state != 0) {
         float want = this.state == 1 ? this.flickYaw : this.savedYaw;
         float[] snapped = MouseRotation.snapToLattice(want, mc.field_1724.method_36455(), mc.field_1724.method_36454(), mc.field_1724.method_36455());
         this.rot.setTarget(snapped[0], snapped[1]);
         boolean done = this.rot.update(mc.field_1724, false);
         if (done && this.state == 2) {
            this.idle();
         }

      }
   }

   public void onAttack(class_1297 attacked) {
      if (this.state == 1) {
         this.beginReturn();
      }

   }

   private class_1657 crosshairPlayer() {
      class_239 var3 = mc.field_1765;
      if (var3 instanceof class_3966 hit) {
         class_1297 var4 = hit.method_17782();
         if (var4 instanceof class_1657 p) {
            if (p != mc.field_1724 && !p.method_7325()) {
               return p;
            }
         }
      }

      return null;
   }
}
