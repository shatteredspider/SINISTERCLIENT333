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
import dev.sevenclient.util.TargetPriority;
import dev.sevenclient.util.TargetScores;
import dev.sevenclient.util.TargetUtil;
import java.util.Objects;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_3532;
import net.minecraft.class_3965;

public class AimAssist2 extends Module {
   private final NumberSetting horizontalSpeed = (NumberSetting)this.reg(new NumberSetting("Horizontal Speed", 5.0, 1.0, 10.0, 0.1));
   private final BoolSetting aimVertically = (BoolSetting)this.reg(new BoolSetting("Aim Vertically", false));
   private final NumberSetting verticalSpeed = (NumberSetting)this.reg(new NumberSetting("Vertical Speed", 5.0, 1.0, 10.0, 0.1));
   private final NumberSetting distance = (NumberSetting)this.reg(new NumberSetting("Distance", 5.0, 1.0, 8.0, 0.1));
   private final NumberSetting maxAngle = (NumberSetting)this.reg(new NumberSetting("Max Angle", 180.0, 1.0, 360.0, 1.0));
   private final BoolSetting requireMouseDown = (BoolSetting)this.reg(new BoolSetting("Require Mouse Down", true));
   private final BoolSetting strafeIncrease = (BoolSetting)this.reg(new BoolSetting("Strafe Increase", false));
   private final ModeSetting targetArea = (ModeSetting)this.reg(new ModeSetting("Target Area", "Closest", new String[]{"Closest", "Center"}));
   private final NumberSetting stickyMs = (NumberSetting)this.reg(new NumberSetting("Target Sticky MS", 700.0, 0.0, 3000.0, 50.0));
   private final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", false));
   private final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   private final BoolSetting weaponsOnly = (BoolSetting)this.reg(new BoolSetting("Limit To Weapons", true));
   private final ModeSetting aimMode = (ModeSetting)this.reg(new ModeSetting("Aim Mode", "Adaptive", new String[]{"Adaptive", "Simple"}));
   private final ModeSetting targetMode = (ModeSetting)this.reg(new ModeSetting("Target Mode", "Hybrid", new String[]{"Hybrid", "Distance", "Yaw"}));
   private final BoolSetting checkBlockBreak = (BoolSetting)this.reg(new BoolSetting("Check Block Break", false));
   private final NumberSetting aimHeightOffset = (NumberSetting)this.reg(new NumberSetting("Aim Height Offset", 0.0, -0.5, 0.3, 0.01));
   private final BoolSetting directCorrection = (BoolSetting)this.reg(new BoolSetting("Direct Correction", false));
   private final BoolSetting yieldToMouse = (BoolSetting)this.reg(new BoolSetting("Yield to Mouse", true));
   private final AdaptiveAim aim = new AdaptiveAim();
   private final SimpleAim simple = new SimpleAim();
   private class_1309 target;
   private long acquiredAt;
   private long lastBlockMs;

   public AimAssist2() {
      super("AimAssist2", "Vape's adaptive aim model. Use instead of AimAssist, not with it.", Category.COMBAT);
      NumberSetting speed = this.verticalSpeed;
      BoolSetting vertical = this.aimVertically;
      Objects.requireNonNull(vertical);
      speed.visibleWhen(vertical::is);
      this.directCorrection.visibleWhen(() -> this.aimMode.is("Adaptive"));
      this.yieldToMouse.visibleWhen(() -> this.aimMode.is("Adaptive"));
      this.registerBindSettings();
   }

   public void onEnable() { this.clear(); }

   public void onDisable() {
      this.clear();
      HumanDiag.aimTargetId = -1;
      Diagnostics.aimTarget = "none";
      Diagnostics.aimAuthority = 0.0;
   }

   private void clear() {
      this.aim.reset();
      this.simple.reset();
      this.target = null;
   }

   private boolean weaponOk() {
      class_1799 held = mc.field_1724.method_6047();
      return TargetUtil.isSword(held) || TargetUtil.isAxe(held);
   }

   private boolean stillValid(class_1309 candidate) {
      if (candidate == null || !candidate.method_5805() || candidate.method_31481()
            || mc.field_1724.method_5739(candidate) > this.distance.val()) return false;
      if (candidate instanceof class_1657 player) {
         return !player.method_7325() && !SevenClient.get().friends.is(player)
               && (!this.enemiesOnly.is() || SevenClient.get().enemies.is(player));
      }
      return !this.playersOnly.is() && (!this.enemiesOnly.is() || SevenClient.get().enemies.is(candidate));
   }

   private class_1309 acquire() {
      double range = this.distance.val();
      boolean enemies = this.enemiesOnly.is();
      class_1657 preferred = TargetPriority.winner(range, enemies);
      if (preferred != null) return preferred;
      if (this.targetMode.is("Yaw")) {
         class_1309 yaw = TargetScores.findYaw(range, enemies, this.playersOnly.is());
         if (yaw != null) return yaw;
      }
      return TargetUtil.find(range, enemies, this.playersOnly.is());
   }

   public void onFrame(float delta) {
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1755 != null) {
         this.clear();
         return;
      }
      Module oldAim = SevenClient.get().modules.byName("AimAssist");
      if (oldAim != null && oldAim.isEnabled()) {
         Diagnostics.aimTarget = "AimAssist2 idle: disable AimAssist (v1) first";
         this.clear();
         return;
      }
      boolean mouseDown = mc.field_1690.field_1886.method_1434();
      if (this.requireMouseDown.is() && !mouseDown) {
         this.clear();
         Diagnostics.aimTarget = "none";
         return;
      }
      if (this.weaponsOnly.is() && !this.weaponOk()) {
         this.clear();
         Diagnostics.aimTarget = "held item rejected by weapon filter";
         return;
      }
      long now = System.currentTimeMillis();
      if (this.checkBlockBreak.is()) {
         if (mouseDown && mc.field_1765 instanceof class_3965) this.lastBlockMs = now;
         if (now - this.lastBlockMs < 250L) {
            this.clear();
            Diagnostics.aimTarget = "paused (mining block)";
            return;
         }
      }
      class_1657 preferred = TargetPriority.winner(this.distance.val(), this.enemiesOnly.is());
      if (!this.stillValid(this.target) || preferred != null && this.target != preferred
            || !this.requireMouseDown.is() && now - this.acquiredAt > (long)this.stickyMs.val()) {
         class_1309 next = this.acquire();
         if (next != this.target) {
            this.aim.reset();
            this.simple.reset();
         }
         this.target = next;
         this.acquiredAt = now;
      }
      if (this.target == null) {
         this.clear();
         HumanDiag.aimTargetId = -1;
         Diagnostics.aimTarget = "none";
         return;
      }
      float angle = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(
            this.target.method_23321() - mc.field_1724.method_23321(),
            this.target.method_23317() - mc.field_1724.method_23317())) - 90.0)
            - mc.field_1724.method_36454());
      if (Math.abs(angle) > this.maxAngle.val() / 2.0) {
         this.aim.reset();
         this.simple.reset();
         Diagnostics.aimTarget = this.target.method_5477().getString() + " (outside max angle)";
         return;
      }
      String mode;
      if (this.aimMode.is("Simple")) {
         this.simple.horizontalSpeed = this.horizontalSpeed.val();
         this.simple.verticalSpeed = this.verticalSpeed.val();
         this.simple.aimVertically = this.aimVertically.is();
         this.simple.strafeIncrease = this.strafeIncrease.is();
         this.simple.closestArea = this.targetArea.is("Closest");
         this.simple.aimHeightOffset = this.aimHeightOffset.val();
         this.simple.tick(mc.field_1724, this.target, delta);
         mode = "simple";
         Diagnostics.aimAuthority = this.simple.strength();
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
         mode = "adaptive";
         Diagnostics.aimAuthority = this.aim.strength();
      }
      HumanDiag.aimTargetId = this.target.method_5628();
      Diagnostics.aimTarget = this.target.method_5477().getString() + " (" + mode + "/" + this.targetMode.get() + ")"
            + (mode.equals("adaptive") && this.aim.isYielding() ? " (yielding)" : "");
      Diagnostics.aimError = Math.abs(angle);
   }
}
