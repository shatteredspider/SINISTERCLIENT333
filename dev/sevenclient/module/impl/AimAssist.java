package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.module.setting.StringSetting;
import dev.sevenclient.util.AimSolver;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.Human;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.RotationSync;
import dev.sevenclient.util.Rotations;
import dev.sevenclient.util.SmoothNoise;
import dev.sevenclient.util.TargetUtil;
import dev.sevenclient.util.TurnLatch;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Random;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class AimAssist extends Module {
   private final NumberSetting smoothness = (NumberSetting)this.reg(new NumberSetting("Smoothness", (double)55.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting range = (NumberSetting)this.reg(new NumberSetting("Max Range", 4.2, (double)1.0F, (double)8.0F, 0.1));
   private final ModeSetting activation = (ModeSetting)this.reg(new ModeSetting("Activation", "On Attack", new String[]{"On Attack", "Always", "Attack Or Bind"}));
   private final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   private final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", false));
   private final BoolSetting weaponsOnly = (BoolSetting)this.reg(new BoolSetting("Limit To Weapons", true));
   private final ModeSetting weaponFilter = (ModeSetting)this.reg(new ModeSetting("Weapon Filter", "Sword + Axe", new String[]{"Sword + Axe", "Sword", "Axe", "Any Weapon", "Custom"}));
   private final StringSetting customItems = (StringSetting)this.reg(new StringSetting("Custom Keywords", "sword,axe,mace"));
   private final NumberSetting reactMinMs = (NumberSetting)this.reg(new NumberSetting("Reaction Min MS", (double)0.0F, (double)0.0F, (double)300.0F, (double)1.0F));
   private final NumberSetting reactMaxMs = (NumberSetting)this.reg(new NumberSetting("Reaction Max MS", (double)40.0F, (double)0.0F, (double)400.0F, (double)1.0F));
   private final NumberSetting targetLagMs = (NumberSetting)this.reg(new NumberSetting("Target Lag MS", (double)0.0F, (double)0.0F, (double)120.0F, (double)1.0F));
   private final BoolSetting softLock = (BoolSetting)this.reg(new BoolSetting("Soft Lock", false));
   public final KeySetting softLockBind = (KeySetting)this.reg(new KeySetting("Soft Lock Bind"));
   private final NumberSetting softLockSpeed = (NumberSetting)this.reg(new NumberSetting("Soft Lock Speed", (double)88.0F, (double)10.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting softLockFov = (NumberSetting)this.reg(new NumberSetting("Soft Lock FOV", (double)120.0F, (double)5.0F, (double)180.0F, (double)5.0F));
   private final NumberSetting softLockRange = (NumberSetting)this.reg(new NumberSetting("Soft Lock Range", (double)4.5F, (double)1.0F, (double)8.0F, 0.1));
   private final NumberSetting softLockInset = (NumberSetting)this.reg(new NumberSetting("Soft Lock Inset", 0.1, (double)0.0F, 0.45, 0.01));
   private final BoolSetting softLockPitch = (BoolSetting)this.reg(new BoolSetting("Soft Lock Pitch", true));
   private final BoolSetting advanced = (BoolSetting)this.reg(new BoolSetting("Advanced", false));
   private final NumberSetting strength = (NumberSetting)this.reg(new NumberSetting("Strength", (double)100.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting tracking = (NumberSetting)this.reg(new NumberSetting("Tracking", (double)85.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting flickAssist = (NumberSetting)this.reg(new NumberSetting("Flick Assist", (double)100.0F, (double)0.0F, (double)200.0F, (double)5.0F));
   private final NumberSetting spinAssist = (NumberSetting)this.reg(new NumberSetting("Spin Assist", (double)100.0F, (double)0.0F, (double)200.0F, (double)5.0F));
   private final NumberSetting turnAuthority = (NumberSetting)this.reg(new NumberSetting("Turn Authority", (double)45.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting maxDegTick = (NumberSetting)this.reg(new NumberSetting("Max Deg Per Tick", (double)35.0F, (double)5.0F, (double)90.0F, (double)1.0F));
   private final NumberSetting shoulderBias = (NumberSetting)this.reg(new NumberSetting("Shoulder Bias", (double)25.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting pathVariety = (NumberSetting)this.reg(new NumberSetting("Path Variety", (double)45.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting calmScale = (NumberSetting)this.reg(new NumberSetting("Calm Zone", (double)100.0F, (double)25.0F, (double)250.0F, (double)5.0F));
   private final NumberSetting verticalRatio = (NumberSetting)this.reg(new NumberSetting("Vertical Ratio", (double)75.0F, (double)10.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting tremorScale = (NumberSetting)this.reg(new NumberSetting("Tremor", (double)85.0F, (double)0.0F, (double)200.0F, (double)5.0F));
   private final NumberSetting inset = (NumberSetting)this.reg(new NumberSetting("Hitbox Inset", 0.05, (double)0.0F, 0.35, 0.01));
   private final BoolSetting stopInHitbox = (BoolSetting)this.reg(new BoolSetting("Stop In Hitbox", true));
   private final BoolSetting subTick = (BoolSetting)this.reg(new BoolSetting("Sub-Tick Target", true));
   private final BoolSetting tickSnap = (BoolSetting)this.reg(new BoolSetting("Tick GCD Snap", true));
   private final AimSolver yawSolver = new AimSolver();
   private final AimSolver pitchSolver = new AimSolver();
   private final SmoothNoise noiseYaw = new SmoothNoise(1374496513L);
   private final SmoothNoise noisePitch = new SmoothNoise(2135587861L);
   private final Random rng = new Random();
   private double lastYaw = Double.NaN;
   private double lastPitch = Double.NaN;
   private double humanVelYaw;
   private double humanVelPitch;
   private double humanRate;
   private double humanFastYaw;
   private double humanFastPitch;
   private double spin;
   private double authEase;
   private double authSmooth;
   private double ffYaw;
   private double ffPitch;
   private double prevWinYaw = Double.NaN;
   private double prevWinPitch = Double.NaN;
   private double calmness = (double)1.0F;
   private final TurnLatch latch = new TurnLatch();
   private int targetId = -1;
   private long acquiredAt;
   private long reactionMs;
   private double arcSign = (double)1.0F;
   private double arcAmount = (double)0.5F;
   private double bJitter = (double)1.0F;
   private double vJitter = (double)1.0F;
   private final Deque<double[]> history = new ArrayDeque();
   private double clock;

   public AimAssist() {
      super("AimAssist", "Universal adaptive tracking. One knob: Smoothness.", Category.COMBAT);
      this.customItems.visibleWhen(() -> this.weaponFilter.is("Custom"));

      for(Setting<?> s : new Setting[]{this.softLockBind, this.softLockSpeed, this.softLockFov, this.softLockRange, this.softLockInset, this.softLockPitch}) {
         BoolSetting var10001 = this.softLock;
         Objects.requireNonNull(var10001);
         s.visibleWhen(var10001::is);
      }

      for(Setting<?> s : new Setting[]{this.strength, this.tracking, this.flickAssist, this.spinAssist, this.turnAuthority, this.maxDegTick, this.shoulderBias, this.pathVariety, this.calmScale, this.verticalRatio, this.tremorScale, this.inset, this.subTick, this.tickSnap}) {
         BoolSetting var9 = this.advanced;
         Objects.requireNonNull(var9);
         s.visibleWhen(var9::is);
      }

      this.registerBindSettings();
   }

   public void onEnable() {
      RotationSync.reset();
   }

   public void onDisable() {
      this.yawSolver.reset();
      this.pitchSolver.reset();
      this.lastYaw = this.lastPitch = Double.NaN;
      this.prevWinYaw = this.prevWinPitch = Double.NaN;
      this.ffYaw = this.ffPitch = (double)0.0F;
      this.humanVelYaw = this.humanVelPitch = this.humanRate = (double)0.0F;
      this.humanFastYaw = this.humanFastPitch = (double)0.0F;
      this.spin = this.authEase = this.authSmooth = (double)0.0F;
      this.calmness = (double)1.0F;
      this.latch.reset();
      this.targetId = -1;
      this.history.clear();
      HumanDiag.aimTargetId = -1;
      HumanDiag.turnLatch = 0;
      RotationSync.reset();
   }

   private double s() {
      return this.smoothness.val() / (double)100.0F;
   }

   private double fittsB() {
      return class_3532.method_16436(this.s(), 0.05, 0.2) * this.bJitter;
   }

   private double fittsA() {
      return class_3532.method_16436(this.s(), 0.018, 0.05);
   }

   private double deadzone() {
      return class_3532.method_16436(this.s(), 0.4, 1.1) * (this.calmScale.val() / (double)100.0F);
   }

   private double lagMs() {
      return this.targetLagMs.val();
   }

   private double baseRate() {
      return class_3532.method_16436(this.s(), (double)620.0F, (double)340.0F);
   }

   private double tremorAmp() {
      return class_3532.method_16436(this.s(), 0.035, 0.11) * (this.tremorScale.val() / (double)100.0F);
   }

   private double reactLo() {
      return Math.min(this.reactMinMs.val(), this.reactMaxMs.val());
   }

   private double reactHi() {
      return Math.max(this.reactMinMs.val(), this.reactMaxMs.val());
   }

   private long sampleReaction() {
      double lo = this.reactLo();
      double hi = this.reactHi();
      if (hi <= (double)0.0F) {
         return 0L;
      } else {
         hi = Math.max(lo + (double)1.0F, hi);
         double span = hi - lo;
         double mu = (lo + span * (double)0.25F) * Human.SIG_SPEED * ((double)1.0F + 0.18 * Human.arousal());
         double sigma = span * 0.18 * Human.SIG_VAR;
         double tau = span * 0.35 * Human.SIG_VAR;
         double v = Human.exGauss(mu, sigma, tau);
         return (long)class_3532.method_15350(v, lo * 0.6, hi * 2.2);
      }
   }

   public void onFrame(float dt) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.clock += (double)dt;
         double yaw = (double)mc.field_1724.method_36454();
         double pitch = (double)mc.field_1724.method_36455();
         if (!Double.isNaN(this.lastYaw)) {
            double instYaw = (double)Rotations.wrap((float)(yaw - this.lastYaw)) / Math.max(1.0E-4, (double)dt);
            double instPitch = (pitch - this.lastPitch) / Math.max(1.0E-4, (double)dt);
            double aSlow = (double)1.0F - Math.exp((double)-16.0F * (double)dt);
            this.humanVelYaw += (instYaw - this.humanVelYaw) * aSlow;
            this.humanVelPitch += (instPitch - this.humanVelPitch) * aSlow;
            double aFast = (double)1.0F - Math.exp((double)-60.0F * (double)dt);
            this.humanFastYaw += (instYaw - this.humanFastYaw) * aFast;
            this.humanFastPitch += (instPitch - this.humanFastPitch) * aFast;
         }

         this.humanRate = Math.hypot(this.humanVelYaw, this.humanVelPitch);
         double spinWant = class_3532.method_15350((this.humanRate - (double)260.0F) / (double)380.0F, (double)0.0F, (double)1.0F);
         this.spin += (spinWant - this.spin) * ((double)1.0F - Math.exp((double)-5.0F * (double)dt));
         RotationSync.enabled = this.tickSnap.is();
         Diagnostics.aimSpin = this.spin;
         if (this.softLockActive()) {
            this.softLockFrame(dt, yaw, pitch);
         } else if (!this.active()) {
            this.relax(dt);
         } else if (this.weaponsOnly.is() && !this.weaponOk(mc.field_1724.method_6047())) {
            Diagnostics.aimTarget = "held item rejected by weapon filter";
            this.relax(dt);
         } else {
            class_1309 target = TargetUtil.find(this.range.val(), this.enemiesOnly.is(), this.playersOnly.is());
            if (target == null) {
               Diagnostics.aimTarget = "none";
               this.relax(dt);
            } else {
               if (target.method_5628() != this.targetId) {
                  this.newAcquisition(target.method_5628());
               }

               Diagnostics.aimTarget = target.method_5477().getString();
               HumanDiag.aimTargetId = target.method_5628();
               float progress = 0.0F;
               class_238 full = target.method_5829();
               if (this.subTick.is()) {
                  try {
                     progress = mc.method_61966().method_60637(false);
                     class_243 feetNow = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
                     full = full.method_997(target.method_30950(progress).method_1020(feetNow));
                  } catch (Throwable var133) {
                  }
               }

               double bh = full.method_17940();
               double shoulderY = full.field_1322 + bh * 0.78;
               class_243 centre = full.method_1005();
               long now = System.currentTimeMillis();
               this.history.addLast(new double[]{(double)now, centre.field_1352, centre.field_1351, centre.field_1350});

               while(this.history.size() > 80) {
                  this.history.removeFirst();
               }

               class_243 lagCentre = this.sampleLagged(now - (long)this.lagMs(), centre);
               class_243 shift = lagCentre.method_1020(centre);
               class_238 aimBox = full.method_997(shift);
               class_243 shoulder = new class_243(centre.field_1352 + shift.field_1352, shoulderY + shift.field_1351, centre.field_1350 + shift.field_1350);
               class_243 eye = this.subTick.is() ? mc.field_1724.method_5836(progress) : mc.field_1724.method_33571();
               float[] win = Rotations.angularWindow(eye, aimBox, this.inset.val(), (float)yaw);
               double loYaw = (double)Math.min(win[0], win[1]);
               double hiYaw = (double)Math.max(win[0], win[1]);
               double loPitch = (double)Math.min(win[2], win[3]);
               double hiPitch = (double)Math.max(win[2], win[3]);
               double dist = eye.method_1022(aimBox.method_1005());
               double centreBias = class_3532.method_15350((3.2 - dist) / 2.4, (double)0.0F, 0.9);
               double centreYaw = (loYaw + hiYaw) * (double)0.5F;
               double shoulderPitch = class_3532.method_15350((double)Rotations.to(eye, shoulder)[1], loPitch, hiPitch);
               boolean inside = yaw >= loYaw && yaw <= hiYaw && pitch >= loPitch && pitch <= hiPitch;
               double wantYaw;
               double wantPitch;
               if (inside) {
                  wantYaw = yaw;
                  wantPitch = pitch;
               } else {
                  double sb = 0.6 * (this.shoulderBias.val() / (double)100.0F);
                  wantYaw = class_3532.method_16436(0.45, class_3532.method_15350(yaw, loYaw, hiYaw), centreYaw);
                  wantPitch = class_3532.method_16436(sb, class_3532.method_15350(pitch, loPitch, hiPitch), shoulderPitch);
               }

               double errYaw = (double)Rotations.wrap((float)(wantYaw - yaw));
               double errPitch = wantPitch - pitch;
               if (this.stopInHitbox.is() && inside) {
                  this.yawSolver.reset();
                  this.pitchSolver.reset();
                  double d0 = Math.exp((double)-9.0F * (double)dt);
                  this.authEase *= d0;
                  this.authSmooth *= d0;
                  this.latch.reset();
                  HumanDiag.turnLatch = 0;
                  Diagnostics.aimError = (double)0.0F;
                  Diagnostics.aimAuthority = (double)0.0F;
                  Diagnostics.aimTarget = target.method_5477().getString() + " (in hitbox, holding)";
                  this.lastYaw = yaw;
                  this.lastPitch = pitch;
               } else {
                  errYaw = this.latch.apply(errYaw, this.humanVelYaw, this.ffYaw, this.spin, now);
                  HumanDiag.turnLatch = this.latch.direction();
                  HumanDiag.unwrappedErr = errYaw;
                  double errMag = Math.hypot(errYaw, errPitch);
                  Diagnostics.aimError = errMag;
                  if (errMag > (double)1.5F && this.pathVariety.val() > (double)0.0F) {
                     double px = -errPitch / errMag;
                     double py = errYaw / errMag;
                     double arcFade = class_3532.method_15350((errMag - (double)1.5F) / (double)2.5F, (double)0.0F, (double)1.0F);
                     double bias = this.arcSign * this.arcAmount * Math.min(errMag, (double)25.0F) * 0.1 * (this.pathVariety.val() / (double)100.0F) * arcFade;
                     errYaw += px * bias;
                     errPitch += py * bias;
                     errMag = Math.hypot(errYaw, errPitch);
                  }

                  double winYaw = (loYaw + hiYaw) * (double)0.5F;
                  double winPitch = (loPitch + hiPitch) * (double)0.5F;
                  if (!Double.isNaN(this.prevWinYaw)) {
                     double rawY = (double)Rotations.wrap((float)(winYaw - this.prevWinYaw)) / Math.max(1.0E-4, (double)dt);
                     double rawP = (winPitch - this.prevWinPitch) / Math.max(1.0E-4, (double)dt);
                     double a = (double)1.0F - Math.exp((double)-9.0F * (double)dt);
                     this.ffYaw += (rawY - this.ffYaw) * a;
                     this.ffPitch += (rawP - this.ffPitch) * a;
                  }

                  this.prevWinYaw = winYaw;
                  this.prevWinPitch = winPitch;
                  double dz = Math.max(0.05, this.deadzone());
                  double dzEff = dz * ((double)1.0F + 0.6 * this.calmness);
                  double outside = class_3532.method_15350((errMag - dzEff) / (dz * (double)1.5F), (double)0.0F, (double)1.0F);
                  double wantAuth = outside * outside * ((double)3.0F - (double)2.0F * outside);
                  this.calmness += ((double)1.0F - wantAuth - this.calmness) * ((double)1.0F - Math.exp((double)-4.0F * (double)dt));
                  double easeRate = wantAuth > this.authEase ? (double)7.5F : (double)9.0F;
                  this.authEase += (wantAuth - this.authEase) * ((double)1.0F - Math.exp(-easeRate * (double)dt));
                  double since = (double)(now - this.acquiredAt);
                  double acquire;
                  if (since < (double)this.reactionMs) {
                     acquire = (double)0.0F;
                  } else {
                     double r = Math.min((double)1.0F, (since - (double)this.reactionMs) / (double)110.0F);
                     acquire = r * r * ((double)3.0F - (double)2.0F * r);
                  }

                  double align = (double)0.0F;
                  if (this.humanRate > (double)1.0F && errMag > 1.0E-4) {
                     align = (this.humanVelYaw * errYaw + this.humanVelPitch * errPitch) / (this.humanRate * errMag);
                  }

                  double flick = class_3532.method_15350(this.humanRate / (double)650.0F, (double)0.0F, (double)1.0F);
                  Diagnostics.aimFlick = flick;
                  double maxBoost = (double)1.0F + (double)2.5F * (this.flickAssist.val() / (double)100.0F);
                  double flickUrgency = (double)1.0F + (maxBoost - (double)1.0F) * ((double)1.0F - Math.exp(-2.8 * flick)) * Math.max((double)0.0F, align);
                  double spinUrgency = (double)1.0F + 0.7 * (this.spinAssist.val() / (double)100.0F) * this.spin;
                  double urgencyScale = Math.min((double)1.0F, errMag / (double)6.0F);
                  double urgency = (double)1.0F + (flickUrgency * spinUrgency - (double)1.0F) * urgencyScale;
                  double yieldF = (double)1.0F / ((double)1.0F + this.humanRate / (double)300.0F * (this.humanRate / (double)300.0F));
                  double yieldMul = class_3532.method_16436(Math.max((double)0.0F, -align), (double)1.0F, yieldF);
                  double rate = Math.min((double)700.0F, this.baseRate() * ((double)1.0F + 1.6 * (this.spinAssist.val() / (double)100.0F) * this.spin));
                  double width = Math.min(Math.abs(hiYaw - loYaw), Math.abs(hiPitch - loPitch));
                  double moveTime = AimSolver.fittsTime(errMag, width, this.fittsA(), this.fittsB()) / urgency;
                  double auth = class_3532.method_15350(this.strength.val() / (double)100.0F * this.authEase * acquire * yieldMul, (double)0.0F, (double)1.0F);
                  double needed = errMag / Math.max(0.05, moveTime);
                  double supplied = this.humanRate * Math.max((double)0.0F, align);
                  double coverage = Math.min((double)1.0F, supplied / Math.max((double)1.0F, needed));
                  auth *= (double)1.0F - coverage;
                  Diagnostics.aimCoverage = coverage;
                  double farness = class_3532.method_15350((errMag - (double)60.0F) / (double)120.0F, (double)0.0F, (double)1.0F);
                  farness = farness * farness * ((double)3.0F - (double)2.0F * farness);
                  auth *= class_3532.method_16436(farness, (double)1.0F, this.turnAuthority.val() / (double)100.0F);
                  HumanDiag.farness = farness;
                  double maxAuthStep = (double)9.0F * (double)dt;
                  this.authSmooth += class_3532.method_15350(auth - this.authSmooth, -maxAuthStep, maxAuthStep);
                  auth = class_3532.method_15350(this.authSmooth, (double)0.0F, (double)1.0F);
                  Diagnostics.aimAuthority = auth;
                  Diagnostics.aimUrgency = urgency;
                  double vr = this.verticalRatio.val() / (double)100.0F * this.vJitter;
                  double corrYaw = this.yawSolver.step(errYaw, moveTime, (double)0.0F, this.humanFastYaw, rate, auth, dt);
                  double corrPitch = this.pitchSolver.step(errPitch, moveTime, (double)0.0F, this.humanFastPitch, rate * vr, auth * vr, dt);
                  double trackGain = this.tracking.val() / (double)100.0F * acquire * class_3532.method_16436(Math.max((double)0.0F, -align), (double)1.0F, yieldF);
                  double trackYaw = this.ffYaw * (double)dt * trackGain;
                  double trackPitch = this.ffPitch * (double)dt * trackGain * vr;
                  double dYaw = corrYaw + trackYaw;
                  double dPitch = corrPitch + trackPitch;
                  double engage = Math.min((double)1.0F, errMag / (double)8.0F);
                  double amp = this.tremorAmp() * (0.15 + 0.85 * engage) * Math.min((double)1.0F, auth) * ((double)1.0F - 0.7 * centreBias) * ((double)1.0F - 0.85 * farness);
                  if (amp > (double)0.0F) {
                     dYaw += this.noiseYaw.fbm(this.clock * (double)1.25F) * amp * (double)dt * (double)60.0F;
                     dPitch += this.noisePitch.fbm(this.clock * 1.05) * amp * (double)dt * (double)60.0F * 0.7;
                  }

                  double capYaw = Math.abs(errYaw) + Math.abs(this.ffYaw * (double)dt) + 0.03;
                  double capPitch = Math.abs(errPitch) + Math.abs(this.ffPitch * (double)dt) + 0.03;
                  dYaw = class_3532.method_15350(dYaw, -capYaw, capYaw);
                  dPitch = class_3532.method_15350(dPitch, -capPitch, capPitch);
                  double budget = this.maxDegTick.val() - HumanDiag.tickYawSpent;
                  if (budget <= (double)0.0F) {
                     dYaw = (double)0.0F;
                     dPitch = (double)0.0F;
                     ++HumanDiag.rotBudgetHits;
                  } else if (Math.abs(dYaw) > budget) {
                     dYaw = Math.signum(dYaw) * budget;
                     ++HumanDiag.rotBudgetHits;
                  }

                  HumanDiag.tickYawSpent += Math.abs(dYaw);
                  if (dYaw == (double)0.0F && dPitch == (double)0.0F) {
                     this.lastYaw = yaw;
                     this.lastPitch = pitch;
                  } else {
                     float newYaw = (float)(yaw + dYaw);
                     float newPitch = class_3532.method_15363((float)(pitch + dPitch), -90.0F, 90.0F);
                     mc.field_1724.method_36456(newYaw);
                     mc.field_1724.method_36457(newPitch);
                     mc.field_1724.method_5847(newYaw);
                     RotationSync.dirty = true;
                     Diagnostics.lastYawStep = dYaw;
                     this.lastYaw = (double)newYaw;
                     this.lastPitch = (double)newPitch;
                  }
               }
            }
         }
      }

   }

   private boolean softLockActive() {
      return !this.softLock.is() ? false : !this.softLockBind.isBound() || this.softLockBind.down();
   }

   private void softLockFrame(float dt, double yaw, double pitch) {
      class_1309 target = TargetUtil.find(this.softLockRange.val(), this.enemiesOnly.is(), this.playersOnly.is());
      if (target == null) {
         Diagnostics.aimTarget = "soft lock: no target";
         HumanDiag.aimTargetId = -1;
         this.lastYaw = yaw;
         this.lastPitch = pitch;
      } else {
         float progress = 0.0F;
         class_238 box = target.method_5829();

         try {
            progress = mc.method_61966().method_60637(false);
            class_243 feet = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
            box = box.method_997(target.method_30950(progress).method_1020(feet));
         } catch (Throwable var37) {
         }

         class_243 eye = mc.field_1724.method_5836(progress);
         float[] win = Rotations.angularWindow(eye, box, this.softLockInset.val(), (float)yaw);
         double loYaw = (double)Math.min(win[0], win[1]);
         double hiYaw = (double)Math.max(win[0], win[1]);
         double loPitch = (double)Math.min(win[2], win[3]);
         double hiPitch = (double)Math.max(win[2], win[3]);
         double wantYaw = class_3532.method_15350(yaw, loYaw, hiYaw);
         double wantPitch = class_3532.method_15350(pitch, loPitch, hiPitch);
         double errYaw = (double)Rotations.wrap((float)(wantYaw - yaw));
         double errPitch = this.softLockPitch.is() ? wantPitch - pitch : (double)0.0F;
         if (Math.hypot(errYaw, errPitch) > this.softLockFov.val()) {
            Diagnostics.aimTarget = "soft lock: outside FOV";
            this.lastYaw = yaw;
            this.lastPitch = pitch;
         } else {
            HumanDiag.aimTargetId = target.method_5628();
            Diagnostics.aimTarget = target.method_5477().getString() + " (SOFT LOCK)";
            Diagnostics.aimError = Math.hypot(errYaw, errPitch);
            Diagnostics.aimAuthority = (double)1.0F;
            if (errYaw == (double)0.0F && errPitch == (double)0.0F) {
               this.lastYaw = yaw;
               this.lastPitch = pitch;
            } else {
               double k = class_3532.method_16436(this.softLockSpeed.val() / (double)100.0F, (double)12.0F, (double)90.0F);
               double alpha = (double)1.0F - Math.exp(-k * (double)dt);
               double dYaw = errYaw * alpha;
               double dPitch = errPitch * alpha;
               float newYaw = (float)(yaw + dYaw);
               float newPitch = class_3532.method_15363((float)(pitch + dPitch), -90.0F, 90.0F);
               mc.field_1724.method_36456(newYaw);
               mc.field_1724.method_36457(newPitch);
               mc.field_1724.method_5847(newYaw);
               RotationSync.dirty = true;
               Diagnostics.lastYawStep = dYaw;
               HumanDiag.tickYawSpent += Math.abs(dYaw);
               this.lastYaw = (double)newYaw;
               this.lastPitch = (double)newPitch;
            }
         }
      }

   }

   private void newAcquisition(int id) {
      this.targetId = id;
      this.acquiredAt = System.currentTimeMillis();
      this.reactionMs = this.sampleReaction();
      this.history.clear();
      this.prevWinYaw = this.prevWinPitch = Double.NaN;
      this.yawSolver.reset();
      this.pitchSolver.reset();
      this.authEase = (double)0.0F;
      this.authSmooth = (double)0.0F;
      this.calmness = (double)1.0F;
      this.latch.reset();
      this.arcSign = this.rng.nextBoolean() ? (double)1.0F : (double)-1.0F;
      this.arcAmount = (double)0.25F + this.rng.nextDouble() * (double)0.75F;
      this.bJitter = (double)1.0F + (this.rng.nextDouble() * 0.26 - 0.12) * Human.SIG_VAR;
      this.vJitter = (double)1.0F + (this.rng.nextDouble() * 0.16 - 0.08) * Human.SIG_VAR;
   }

   private void relax(float dt) {
      double decay = Math.exp((double)-6.0F * (double)dt);
      this.yawSolver.reset();
      this.pitchSolver.reset();
      this.ffYaw *= decay;
      this.ffPitch *= decay;
      this.authEase *= decay;
      this.authSmooth *= decay;
      this.calmness = (double)1.0F;
      this.latch.reset();
      this.targetId = -1;
      this.history.clear();
      this.prevWinYaw = this.prevWinPitch = Double.NaN;
      Diagnostics.aimAuthority = (double)0.0F;
      HumanDiag.aimTargetId = -1;
      HumanDiag.turnLatch = 0;
      if (mc.field_1724 != null) {
         this.lastYaw = (double)mc.field_1724.method_36454();
         this.lastPitch = (double)mc.field_1724.method_36455();
      }

   }

   private class_243 sampleLagged(long when, class_243 fallback) {
      if (this.history.size() < 2) {
         return fallback;
      } else {
         double[] older = null;
         double[] newer = null;

         for(double[] sm : this.history) {
            if (!(sm[0] <= (double)when)) {
               newer = sm;
               break;
            }

            older = sm;
         }

         if (older == null) {
            double[] first = (double[])this.history.peekFirst();
            return new class_243(first[1], first[2], first[3]);
         } else if (newer == null) {
            return fallback;
         } else {
            double span = newer[0] - older[0];
            double t = span <= (double)0.0F ? (double)0.0F : ((double)when - older[0]) / span;
            return new class_243(class_3532.method_16436(t, older[1], newer[1]), class_3532.method_16436(t, older[2], newer[2]), class_3532.method_16436(t, older[3], newer[3]));
         }
      }
   }

   private boolean active() {
      boolean attacking = mc.field_1690.field_1886.method_1434();
      boolean var10000;
      switch ((String)this.activation.get()) {
         case "Always" -> var10000 = true;
         case "Attack Or Bind" -> var10000 = attacking || this.bind.down();
         default -> var10000 = attacking;
      }

      return var10000;
   }

   private boolean weaponOk(class_1799 held) {
      boolean var10000;
      switch ((String)this.weaponFilter.get()) {
         case "Sword" -> var10000 = TargetUtil.isSword(held);
         case "Axe" -> var10000 = TargetUtil.isAxe(held);
         case "Any Weapon" -> var10000 = TargetUtil.isWeapon(held, (String)null);
         case "Custom" -> var10000 = TargetUtil.isWeapon(held, (String)this.customItems.get());
         default -> var10000 = TargetUtil.isSword(held) || TargetUtil.isAxe(held);
      }

      return var10000;
   }
}
