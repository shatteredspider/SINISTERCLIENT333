package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.RotationSync;
import dev.sevenclient.util.Rotations;
import dev.sevenclient.util.TargetUtil;
import java.util.Optional;
import java.util.Random;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class SilentAim extends Module {
   private final ModeSetting technique = (ModeSetting)this.reg(new ModeSetting("Technique", "Camera", new String[]{"Camera", "Glide", "Packet"}));
   private final ModeSetting activation = (ModeSetting)this.reg(new ModeSetting("Activation", "On Attack", new String[]{"On Attack", "Always"}));
   private final NumberSetting range = (NumberSetting)this.reg(new NumberSetting("Range", (double)3.5F, (double)1.0F, (double)6.0F, 0.1));
   private final NumberSetting maxOffset = (NumberSetting)this.reg(new NumberSetting("Max Offset Deg", (double)18.0F, (double)1.0F, (double)60.0F, (double)1.0F));
   private final NumberSetting inset = (NumberSetting)this.reg(new NumberSetting("Hitbox Inset", 0.08, (double)0.0F, 0.4, 0.01));
   private final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", true));
   private final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   private final BoolSetting weaponsOnly = (BoolSetting)this.reg(new BoolSetting("Limit To Weapons", true));
   private final BoolSetting requireStill = (BoolSetting)this.reg(new BoolSetting("Require Still", true));
   private final NumberSetting cameraSpeed = (NumberSetting)this.reg(new NumberSetting("Camera Speed", (double)65.0F, (double)5.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting recentre = (NumberSetting)this.reg(new NumberSetting("Recentre Rate", (double)6.0F, (double)0.5F, (double)20.0F, (double)0.5F));
   private final NumberSetting glideSpeed = (NumberSetting)this.reg(new NumberSetting("Glide Speed", (double)40.0F, (double)1.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting glideJitter = (NumberSetting)this.reg(new NumberSetting("Glide Jitter", 0.08, (double)0.0F, 0.3, 0.01));
   private final NumberSetting attackGate = (NumberSetting)this.reg(new NumberSetting("Attack Gate Deg", (double)3.0F, (double)0.5F, (double)10.0F, (double)0.5F));
   private final BoolSetting shieldCheck = (BoolSetting)this.reg(new BoolSetting("Shield Check", false));
   private static final String OWNER = "SilentAim";
   private static final double STILL_VELOCITY_SQ = 0.0016;
   private final Random rng = new Random();

   public SilentAim() {
      super("SilentAim", "Aims without moving your view. Camera mode survives prediction; Packet mode does not.", Category.COMBAT);
      this.requireStill.visibleWhen(() -> this.technique.is("Packet"));

      for(Setting var4 : new Setting[]{this.cameraSpeed, this.recentre}) {
         var4.visibleWhen(() -> this.technique.is("Camera"));
      }

      this.glideSpeed.visibleWhen(() -> this.technique.is("Glide"));
      this.glideJitter.visibleWhen(() -> this.technique.is("Glide"));
      this.attackGate.visibleWhen(() -> this.technique.is("Packet"));
      this.registerBindSettings();
   }

   public void onEnable() {
      RotationSync.clearHidden();
   }

   public void onDisable() {
      RotationSync.releaseSilent("SilentAim");
      RotationSync.hideRotation = false;
      RotationSync.clearHidden();
      HumanDiag.silentAimActive = false;
      HumanDiag.silentAimState = "off";
   }

   public void onTick() {
      RotationSync.maxHiddenYaw = (float)this.maxOffset.val();
      RotationSync.maxHiddenPitch = (float)Math.min(this.maxOffset.val(), (double)40.0F);
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.active() && (!this.weaponsOnly.is() || this.weaponOk())) {
            class_1309 var1 = TargetUtil.find(this.range.val(), this.enemiesOnly.is(), this.playersOnly.is());
            if (var1 == null) {
               this.stand("no target");
            } else if (this.shieldCheck.is() && TargetUtil.shieldUp(var1, 5) && !TargetUtil.isAxe(mc.field_1724.method_6047())) {
               this.stand("shield up, holding");
            } else {
               float var2 = mc.field_1724.method_36454();
               float var3 = mc.field_1724.method_36455();
               class_243 var4 = mc.field_1724.method_33571();
               class_238 var5 = var1.method_5829();
               float[] var6 = Rotations.angularWindow(var4, var5, this.inset.val(), var2);
               double var7 = (double)Math.min(var6[0], var6[1]);
               double var9 = (double)Math.max(var6[0], var6[1]);
               double var11 = (double)Math.min(var6[2], var6[3]);
               double var13 = (double)Math.max(var6[2], var6[3]);
               double var15 = class_3532.method_15350((double)var2, var7, var9);
               double var17 = class_3532.method_15350((double)var3, var11, var13);
               double var19 = (double)Rotations.wrap((float)(var15 - (double)var2));
               double var21 = var17 - (double)var3;
               double var23 = Math.hypot(var19, var21);
               if (var23 < 0.01) {
                  this.stand("already on target");
               } else if (var23 > this.maxOffset.val()) {
                  this.stand(String.format("target %.0f deg off, over budget", var23));
               } else {
                  float var25 = (float)((double)var2 + var19);
                  float var26 = class_3532.method_15363((float)((double)var3 + var21), -90.0F, 90.0F);
                  if (!this.raycastHits(var4, var25, var26, var5)) {
                     this.stand("solution does not raycast, skipping");
                  } else {
                     if (this.technique.is("Camera")) {
                        this.cameraMode(var1, var19, var21, var23);
                     } else if (this.technique.is("Glide")) {
                        this.glideMode(var1, var4, var5, var2, var3, var23);
                     } else {
                        this.packetMode(var1, var25, var26, var23);
                     }

                  }
               }
            }
         } else {
            this.stand("inactive");
         }
      } else {
         this.stand("no world");
      }
   }

   private void cameraMode(class_1309 var1, double var2, double var4, double var6) {
      RotationSync.hideRotation = true;
      RotationSync.releaseSilent("SilentAim");
      double var8 = class_3532.method_15350(this.cameraSpeed.val() / (double)100.0F, 0.05, (double)1.0F);
      RotationSync.applyHidden(mc.field_1724, var2 * var8, var4 * var8);
      HumanDiag.silentAimActive = true;
      HumanDiag.silentAimState = String.format("CAMERA  %.1f deg to %s  (hidden %+.1f / %+.1f)", var6, var1.method_5477().getString(), RotationSync.hiddenYaw(), RotationSync.hiddenPitch());
      Diagnostics.aimTarget = var1.method_5477().getString() + " (camera silent)";
   }

   private void glideMode(class_1309 var1, class_243 var2, class_238 var3, float var4, float var5, double var6) {
      double[] var8 = SilentAimGlide.jitteredPoint(this.rng, var2, var3, this.range.val() + (double)0.5F, this.glideJitter.val());
      float[] var9 = Rotations.to(var2, new class_243(var8[0], var8[1], var8[2]));
      double var10 = (double)Rotations.wrap(var9[0] - var4);
      double var12 = (double)(var9[1] - var5);
      double var14 = class_3532.method_15350(this.glideSpeed.val() / (double)100.0F, 0.05, (double)1.0F);
      RotationSync.hideRotation = true;
      RotationSync.releaseSilent("SilentAim");
      SilentAimGlide.approach(mc.field_1724, var10, var12, var14);
      HumanDiag.silentAimActive = true;
      HumanDiag.silentAimState = String.format("GLIDE  %.1f deg to %s  (hidden %+.1f / %+.1f)", var6, var1.method_5477().getString(), RotationSync.hiddenYaw(), RotationSync.hiddenPitch());
      Diagnostics.aimTarget = var1.method_5477().getString() + " (glide silent)";
   }

   private void packetMode(class_1309 var1, float var2, float var3, double var4) {
      RotationSync.hideRotation = false;
      RotationSync.clearHidden();
      if (this.requireStill.is() && !this.still()) {
         RotationSync.releaseSilent("SilentAim");
         HumanDiag.silentAimActive = false;
         HumanDiag.silentAimState = "packet mode held: you are moving (prediction would diverge)";
      } else if (var4 > this.attackGate.val()) {
         RotationSync.releaseSilent("SilentAim");
         HumanDiag.silentAimActive = false;
         HumanDiag.silentAimState = String.format("packet mode held: %.1f deg off, outside attack gate", var4);
      } else {
         boolean var6 = RotationSync.requestSilent("SilentAim", 10, var2, true, var3);
         HumanDiag.silentAimActive = var6;
         HumanDiag.silentAimState = var6 ? String.format("PACKET  %.1f deg to %s", var4, var1.method_5477().getString()) : "yielded to " + RotationSync.silentOwner();
         if (var6) {
            Diagnostics.aimTarget = var1.method_5477().getString() + " (packet silent)";
         }

      }
   }

   private boolean raycastHits(class_243 var1, float var2, float var3, class_238 var4) {
      double var5 = Math.toRadians((double)var2);
      double var7 = Math.toRadians((double)var3);
      double var9 = Math.cos(var7);
      class_243 var11 = new class_243(-Math.sin(var5) * var9, -Math.sin(var7), Math.cos(var5) * var9);
      class_243 var12 = var1.method_1019(var11.method_1021(this.range.val() + (double)0.5F));
      Optional var13 = var4.method_1014(-this.inset.val() * (double)0.5F).method_992(var1, var12);
      return var13.isPresent();
   }

   private boolean still() {
      class_243 var1 = mc.field_1724.method_18798();
      boolean var2 = var1.field_1352 * var1.field_1352 + var1.field_1350 * var1.field_1350 < 0.0016;
      boolean var3 = !mc.field_1690.field_1894.method_1434() && !mc.field_1690.field_1881.method_1434() && !mc.field_1690.field_1913.method_1434() && !mc.field_1690.field_1849.method_1434() && !mc.field_1690.field_1903.method_1434();
      return var2 && var3;
   }

   private void stand(String var1) {
      RotationSync.releaseSilent("SilentAim");
      RotationSync.hideRotation = false;
      HumanDiag.silentAimActive = false;
      HumanDiag.silentAimState = var1;
   }

   public void onFrame(float var1) {
      if (!RotationSync.hideRotation) {
         RotationSync.decayHidden(var1, this.recentre.val());
      }

   }

   public void onPostTick() {
   }

   private boolean active() {
      return this.activation.is("Always") || mc.field_1690.field_1886.method_1434() || this.bind.down();
   }

   private boolean weaponOk() {
      class_1799 var1 = mc.field_1724.method_6047();
      return TargetUtil.isSword(var1) || TargetUtil.isAxe(var1);
   }
}
