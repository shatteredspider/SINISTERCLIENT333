package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.AdaptiveAim;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.SimpleAim;
import dev.sevenclient.util.TargetScores;
import dev.sevenclient.util.TargetUtil;
import java.util.Objects;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_3532;
import net.minecraft.class_3965;

public class AimAssist2 extends Module {
   private final NumberSetting horizontalSpeed = (NumberSetting)this.reg(new NumberSetting("Horizontal Speed", (double)5.0F, (double)1.0F, (double)10.0F, 0.1));
   private final BoolSetting aimVertically = (BoolSetting)this.reg(new BoolSetting("Aim Vertically", false));
   private final NumberSetting verticalSpeed = (NumberSetting)this.reg(new NumberSetting("Vertical Speed", (double)5.0F, (double)1.0F, (double)10.0F, 0.1));
   private final NumberSetting distance = (NumberSetting)this.reg(new NumberSetting("Distance", (double)5.0F, (double)1.0F, (double)8.0F, 0.1));
   private final NumberSetting maxAngle = (NumberSetting)this.reg(new NumberSetting("Max Angle", (double)180.0F, (double)1.0F, (double)360.0F, (double)1.0F));
   private final BoolSetting requireMouseDown = (BoolSetting)this.reg(new BoolSetting("Require Mouse Down", true));
   private final BoolSetting strafeIncrease = (BoolSetting)this.reg(new BoolSetting("Strafe Increase", false));
   private final ModeSetting targetArea = (ModeSetting)this.reg(new ModeSetting("Target Area", "Closest", new String[]{"Closest", "Center"}));
   private final NumberSetting stickyMs = (NumberSetting)this.reg(new NumberSetting("Target Sticky MS", (double)700.0F, (double)0.0F, (double)3000.0F, (double)50.0F));
   private final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", false));
   private final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   private final BoolSetting weaponsOnly = (BoolSetting)this.reg(new BoolSetting("Limit To Weapons", true));
   private final ModeSetting aimMode = (ModeSetting)this.reg(new ModeSetting("Aim Mode", "Adaptive", new String[]{"Adaptive", "Simple"}));
   private final ModeSetting targetMode = (ModeSetting)this.reg(new ModeSetting("Target Mode", "Hybrid", new String[]{"Hybrid", "Distance", "Yaw"}));
   private final BoolSetting checkBlockBreak = (BoolSetting)this.reg(new BoolSetting("Check Block Break", false));
   private final NumberSetting aimHeightOffset = (NumberSetting)this.reg(new NumberSetting("Aim Height Offset", (double)0.0F, (double)-0.5F, 0.3, 0.01));
   private final BoolSetting directCorrection = (BoolSetting)this.reg(new BoolSetting("Direct Correction", false));
   private final BoolSetting yieldToMouse = (BoolSetting)this.reg(new BoolSetting("Yield to Mouse", true));
   private final AdaptiveAim aim = new AdaptiveAim();
   private final SimpleAim simple = new SimpleAim();
   private class_1309 target;
   private long acquiredAt;
   private long lastBlockMs;

   public AimAssist2() {
      super("AimAssist2", "Vape's adaptive aim model. Use instead of AimAssist, not with it.", Category.COMBAT);
      NumberSetting var10000 = this.verticalSpeed;
      BoolSetting var10001 = this.aimVertically;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.directCorrection.visibleWhen(() -> this.aimMode.is("Adaptive"));
      this.yieldToMouse.visibleWhen(() -> this.aimMode.is("Adaptive"));
      this.registerBindSettings();
   }

   public void onEnable() {
      this.clear();
   }

   public void onDisable() {
      this.clear();
      HumanDiag.aimTargetId = -1;
      Diagnostics.aimTarget = "none";
      Diagnostics.aimAuthority = (double)0.0F;
   }

   private void clear() {
      this.aim.reset();
      this.simple.reset();
      this.target = null;
   }

   private boolean weaponOk() {
      class_1799 var1 = mc.field_1724.method_6047();
      return TargetUtil.isSword(var1) || TargetUtil.isAxe(var1);
   }

   private boolean stillValid(class_1309 var1) {
      return var1 != null && var1.method_5805() && !var1.method_31481() && (double)mc.field_1724.method_5739(var1) <= this.distance.val();
   }

   private class_1309 acquire() {
      double var1 = this.distance.val();
      boolean var3 = this.enemiesOnly.is();
      boolean var4 = this.playersOnly.is();
      if (this.targetMode.is("Yaw")) {
         class_1309 var5 = TargetScores.findYaw(var1, var3, var4);
         if (var5 != null) {
            return var5;
         }
      }

      return TargetUtil.find(var1, var3, var4);
   }

   public void onFrame(float var1) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         Module var2 = SevenClient.get().modules.byName("AimAssist");
         if (var2 != null && var2.isEnabled()) {
            Diagnostics.aimTarget = "AimAssist2 idle: disable AimAssist (v1) first";
            this.clear();
         } else {
            boolean var3 = mc.field_1690.field_1886.method_1434();
            if (this.requireMouseDown.is() && !var3) {
               this.clear();
               Diagnostics.aimTarget = "none";
            } else if (this.weaponsOnly.is() && !this.weaponOk()) {
               this.clear();
               Diagnostics.aimTarget = "held item rejected by weapon filter";
            } else {
               long var4 = System.currentTimeMillis();
               if (this.checkBlockBreak.is()) {
                  if (var3 && mc.field_1765 instanceof class_3965) {
                     this.lastBlockMs = var4;
                  }

                  if (var4 - this.lastBlockMs < 250L) {
                     this.clear();
                     Diagnostics.aimTarget = "paused (mining block)";
                     return;
                  }
               }

               if (!this.stillValid(this.target) || !this.requireMouseDown.is() && var4 - this.acquiredAt > (long)this.stickyMs.val()) {
                  class_1309 var6 = this.acquire();
                  if (var6 != this.target) {
                     this.aim.reset();
                     this.simple.reset();
                  }

                  this.target = var6;
                  this.acquiredAt = var4;
               }

               if (this.target == null) {
                  this.clear();
                  HumanDiag.aimTargetId = -1;
                  Diagnostics.aimTarget = "none";
               } else {
                  float var8 = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(this.target.method_23321() - mc.field_1724.method_23321(), this.target.method_23317() - mc.field_1724.method_23317())) - (double)90.0F) - mc.field_1724.method_36454());
                  if ((double)Math.abs(var8) > this.maxAngle.val() / (double)2.0F) {
                     this.aim.reset();
                     this.simple.reset();
                     Diagnostics.aimTarget = this.target.method_5477().getString() + " (outside max angle)";
                  } else {
                     String var7;
                     if (this.aimMode.is("Simple")) {
                        this.simple.horizontalSpeed = this.horizontalSpeed.val();
                        this.simple.verticalSpeed = this.verticalSpeed.val();
                        this.simple.aimVertically = this.aimVertically.is();
                        this.simple.strafeIncrease = this.strafeIncrease.is();
                        this.simple.closestArea = this.targetArea.is("Closest");
                        this.simple.aimHeightOffset = this.aimHeightOffset.val();
                        this.simple.tick(mc.field_1724, this.target, var1);
                        var7 = "simple";
                        Diagnostics.aimAuthority = (double)this.simple.strength();
                     } else {
                        this.aim.horizontalSpeed = this.horizontalSpeed.val();
                        this.aim.verticalSpeed = this.verticalSpeed.val();
                        this.aim.aimVertically = this.aimVertically.is();
                        this.aim.strafeIncrease = this.strafeIncrease.is();
                        this.aim.closestArea = this.targetArea.is("Closest");
                        this.aim.aimHeightOffset = this.aimHeightOffset.val();
                        this.aim.directCorrection = this.directCorrection.is();
                        this.aim.yieldToMouse = this.yieldToMouse.is();
                        this.aim.tick(mc.field_1724, this.target);
                        var7 = "adaptive";
                        Diagnostics.aimAuthority = (double)this.aim.strength();
                     }

                     HumanDiag.aimTargetId = this.target.method_5628();
                     String var10000 = this.target.method_5477().getString();
                     Diagnostics.aimTarget = var10000 + " (" + var7 + "/" + (String)this.targetMode.get() + ")" + (var7.equals("adaptive") && this.aim.isYielding() ? " (yielding)" : "");
                     Diagnostics.aimError = (double)Math.abs(var8);
                  }
               }
            }
         }
      } else {
         this.clear();
      }
   }
}
