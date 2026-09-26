package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.util.HumanRandom;
import dev.sevenclient.util.Projection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_3966;

public class HitParticles extends Module {
   private final ModeSetting shape = (ModeSetting)this.reg(new ModeSetting("Shape", "Spark", new String[]{"Spark", "Nova", "Ring", "Ouroboros", "Spiral", "Helix"}));
   private final NumberSetting count = (NumberSetting)this.reg(new NumberSetting("Count", (double)14.0F, (double)1.0F, (double)64.0F, (double)1.0F));
   private final NumberSetting lifetime = (NumberSetting)this.reg(new NumberSetting("Lifetime", 0.6, 0.1, (double)3.0F, 0.05));
   private final NumberSetting size = (NumberSetting)this.reg(new NumberSetting("Size", 2.6, (double)1.0F, (double)10.0F, 0.2));
   private final NumberSetting speed = (NumberSetting)this.reg(new NumberSetting("Speed", (double)42.0F, (double)5.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting spread = (NumberSetting)this.reg(new NumberSetting("Spread", (double)65.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting gravity = (NumberSetting)this.reg(new NumberSetting("Gravity", (double)55.0F, (double)0.0F, (double)200.0F, (double)5.0F));
   private final NumberSetting drag = (NumberSetting)this.reg(new NumberSetting("Drag", (double)35.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting ringRadius = (NumberSetting)this.reg(new NumberSetting("Ring Radius", 0.85, 0.1, (double)3.0F, 0.05));
   private final NumberSetting orbitSpeed = (NumberSetting)this.reg(new NumberSetting("Orbit Speed", (double)220.0F, (double)10.0F, (double)900.0F, (double)10.0F));
   private final NumberSetting contraction = (NumberSetting)this.reg(new NumberSetting("Contraction", (double)45.0F, (double)-100.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting taper = (NumberSetting)this.reg(new NumberSetting("Tail Taper", (double)70.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting rise = (NumberSetting)this.reg(new NumberSetting("Rise", (double)20.0F, (double)-100.0F, (double)100.0F, (double)5.0F));
   private final ModeSetting colourMode = (ModeSetting)this.reg(new ModeSetting("Colour", "Solid", new String[]{"Solid", "Dual Fade", "Rainbow", "By Speed"}));
   private final NumberSetting hue = (NumberSetting)this.reg(new NumberSetting("Hue", (double)40.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting hue2 = (NumberSetting)this.reg(new NumberSetting("Hue B", (double)320.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting rainbowSpeed = (NumberSetting)this.reg(new NumberSetting("Rainbow Speed", (double)90.0F, (double)5.0F, (double)600.0F, (double)5.0F));
   private final NumberSetting rainbowSpin = (NumberSetting)this.reg(new NumberSetting("Rainbow Spread", (double)140.0F, (double)0.0F, (double)360.0F, (double)10.0F));
   private final NumberSetting saturation = (NumberSetting)this.reg(new NumberSetting("Saturation", (double)45.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting brightness = (NumberSetting)this.reg(new NumberSetting("Brightness", (double)100.0F, (double)20.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting hueJitter = (NumberSetting)this.reg(new NumberSetting("Hue Jitter", (double)14.0F, (double)0.0F, (double)90.0F, (double)1.0F));
   private final BoolSetting critFlash = (BoolSetting)this.reg(new BoolSetting("Crit Emphasis", true));
   private final NumberSetting critHue = (NumberSetting)this.reg(new NumberSetting("Crit Hue", (double)196.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting critScale = (NumberSetting)this.reg(new NumberSetting("Crit Multiplier", (double)200.0F, (double)100.0F, (double)400.0F, (double)10.0F));
   private final BoolSetting trails = (BoolSetting)this.reg(new BoolSetting("Trails", true));
   private final NumberSetting trailAlpha = (NumberSetting)this.reg(new NumberSetting("Trail Alpha", (double)35.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final BoolSetting glow = (BoolSetting)this.reg(new BoolSetting("Glow", true));
   private final NumberSetting turbulence = (NumberSetting)this.reg(new NumberSetting("Turbulence", (double)12.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private static final int MAX = 900;
   private final List<Spark> sparks = new ArrayList();
   private long lastNanos = 0L;
   private float clock = 0.0F;

   public HitParticles() {
      super("HitParticles", "Sparks on every landed hit.", Category.VISUALS);

      for(Setting<?> s : new Setting[]{this.speed, this.spread, this.gravity, this.drag}) {
         s.visibleWhen(() -> this.shape.is("Spark") || this.shape.is("Nova"));
      }

      for(Setting<?> s : new Setting[]{this.ringRadius, this.orbitSpeed, this.contraction, this.taper, this.rise}) {
         s.visibleWhen(this::orbital);
      }

      this.hue2.visibleWhen(() -> this.colourMode.is("Dual Fade"));
      this.rainbowSpeed.visibleWhen(() -> this.colourMode.is("Rainbow"));
      this.rainbowSpin.visibleWhen(() -> this.colourMode.is("Rainbow"));
      NumberSetting var10000 = this.critHue;
      BoolSetting var10001 = this.critFlash;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.critScale;
      var10001 = this.critFlash;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.trailAlpha;
      var10001 = this.trails;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   private boolean orbital() {
      return this.shape.is("Ring") || this.shape.is("Ouroboros") || this.shape.is("Spiral") || this.shape.is("Helix");
   }

   public void onDisable() {
      this.sparks.clear();
      this.lastNanos = 0L;
   }

   public void onAttack(class_1297 target) {
      if (mc.field_1724 != null && target != null) {
         class_243 var10000;
         label114: {
            class_239 var4 = mc.field_1765;
            if (var4 instanceof class_3966) {
               class_3966 ehr = (class_3966)var4;
               if (ehr.method_17782() == target) {
                  var10000 = mc.field_1765.method_17784();
                  break label114;
               }
            }

            var10000 = target.method_5829().method_1005();
         }

         class_243 at = var10000;
         class_243 toward = at.method_1020(mc.field_1724.method_33571());
         if (toward.method_1027() < 1.0E-6) {
            toward = new class_243((double)0.0F, (double)1.0F, (double)0.0F);
         }

         class_243 normal = toward.method_1029();
         class_243 away = normal.method_1021((double)-1.0F);
         class_243 helper = Math.abs(normal.field_1351) > 0.92 ? new class_243((double)1.0F, (double)0.0F, (double)0.0F) : new class_243((double)0.0F, (double)1.0F, (double)0.0F);
         class_243 axisA = normal.method_1036(helper).method_1029();
         class_243 axisB = normal.method_1036(axisA).method_1029();
         boolean crit = this.critFlash.is() && !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 < -0.08;
         float critMul = crit ? (float)(this.critScale.val() / (double)100.0F) : 1.0F;
         int n = Math.max(1, (int)(this.count.val() * (crit ? 1.6 : (double)1.0F)));
         float baseHue = (float)(crit ? this.critHue.val() : this.hue.val());
         boolean orb = this.orbital();
         boolean helix = this.shape.is("Helix");
         boolean nova = this.shape.is("Nova");

         for(int i = 0; i < n; ++i) {
            Spark s = new Spark();
            s.pos = at;
            s.prev = at;
            s.maxLife = (float)(this.lifetime.val() * (orb ? (double)1.0F : 0.6 + HumanRandom.uniform((double)0.0F, 0.7)));
            s.life = s.maxLife;
            s.hueBase = baseHue + (float)HumanRandom.uniform(-this.hueJitter.val(), this.hueJitter.val());
            s.phase = n <= 1 ? 0.0F : (float)i / (float)(n - 1);
            s.scale = (float)this.size.val() * critMul;
            if (orb) {
               s.orbital = true;
               s.centre = at;
               s.axisA = axisA;
               s.axisB = axisB;
               s.orbR = this.ringRadius.val();
               double arc = this.shape.is("Ouroboros") ? 5.403539364174444 : (Math.PI * 2D);
               s.ang = (double)s.phase * arc + (helix && i % 2 == 0 ? Math.PI : (double)0.0F);
               s.angVel = Math.toRadians(this.orbitSpeed.val()) * (helix && i % 2 == 0 ? (double)-1.0F : (double)1.0F);
               s.vel = class_243.field_1353;
               s.scale *= 1.0F - (float)(this.taper.val() / (double)100.0F) * s.phase;
            } else {
               class_243 dir;
               if (nova) {
                  double a = (Math.PI * 2D) * (double)i / (double)n;
                  dir = axisA.method_1021(Math.cos(a)).method_1019(axisB.method_1021(Math.sin(a))).method_1029();
               } else {
                  double sp = this.spread.val() / (double)100.0F;
                  dir = away.method_1031(HumanRandom.gauss(sp * 0.8), HumanRandom.gauss(sp * 0.8) + 0.18, HumanRandom.gauss(sp * 0.8)).method_1029();
               }

               double mag = this.speed.val() / (double)100.0F * (double)7.0F * (double)critMul * (nova ? (double)1.0F : 0.45 + HumanRandom.uniform((double)0.0F, 0.9));
               s.vel = dir.method_1021(mag);
            }

            this.sparks.add(s);
         }

         while(this.sparks.size() > 900) {
            this.sparks.remove(0);
         }

      }
   }

   private int colourOf(Spark s, float ease, double velLen, int alpha) {
      float sat = (float)this.saturation.val() / 100.0F;
      float bri = (float)this.brightness.val() / 100.0F;
      float h;
      switch ((String)this.colourMode.get()) {
         case "Dual Fade" -> h = (float)(this.hue.val() + (this.hue2.val() - this.hue.val()) * ((double)1.0F - (double)ease));
         case "Rainbow" -> h = (float)((double)this.clock * this.rainbowSpeed.val() + (double)s.phase * this.rainbowSpin.val());
         case "By Speed" -> h = (float)(this.hue.val() + Math.min((double)1.0F, velLen / (double)8.0F) * (double)120.0F);
         default -> h = s.hueBase;
      }

      return Projection.hsb(h, sat, bri, alpha);
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.field_1842) {
         long now = System.nanoTime();
         float dt = this.lastNanos == 0L ? 0.016F : (float)((double)(now - this.lastNanos) / (double)1.0E9F);
         this.lastNanos = now;
         dt = Math.max(5.0E-4F, Math.min(0.1F, dt));
         this.clock += dt;
         double g = this.gravity.val() / (double)100.0F * (double)16.0F;
         double dragK = this.drag.val() / (double)100.0F * 3.2;
         double contract = this.contraction.val() / (double)100.0F;
         double riseRate = this.rise.val() / (double)100.0F * 1.6;
         double turb = this.turbulence.val() / (double)100.0F * 2.2;
         int trailA = (int)(this.trailAlpha.val() * 2.55);
         Projection.View view = Projection.capture();
         Iterator<Spark> it = this.sparks.iterator();

         while(it.hasNext()) {
            Spark s = (Spark)it.next();
            s.life -= dt;
            if (s.life <= 0.0F) {
               it.remove();
            } else {
               s.prev = s.pos;
               float t = s.life / Math.max(0.001F, s.maxLife);
               float ease = t * t * (3.0F - 2.0F * t);
               if (s.orbital) {
                  s.ang += s.angVel * (double)dt;
                  double r = s.orbR * ((double)1.0F - contract * ((double)1.0F - (double)t));
                  if (this.shape.is("Spiral")) {
                     r *= 0.35 + 0.65 * ((double)1.0F - (double)t);
                  }

                  s.centre = s.centre.method_1031((double)0.0F, riseRate * (double)dt, (double)0.0F);
                  class_243 offset = s.axisA.method_1021(Math.cos(s.ang) * r).method_1019(s.axisB.method_1021(Math.sin(s.ang) * r));
                  s.pos = s.centre.method_1019(offset);
                  if (turb > (double)0.0F) {
                     double w = Math.sin((double)this.clock * 5.3 + (double)s.phase * 9.1) * turb * 0.02;
                     s.pos = s.pos.method_1031(w, Math.cos((double)this.clock * 4.1 + (double)s.phase * 7.7) * turb * 0.02, w);
                  }
               } else {
                  double vlen = s.vel.method_1033();
                  class_243 accel = (new class_243((double)0.0F, -g, (double)0.0F)).method_1019(s.vel.method_1021(-dragK * vlen));
                  if (turb > (double)0.0F) {
                     accel = accel.method_1031(Math.sin((double)this.clock * 7.1 + (double)s.phase * (double)11.0F) * turb, Math.cos((double)this.clock * 6.3 + (double)s.phase * (double)8.0F) * turb * (double)0.5F, Math.sin((double)this.clock * 5.7 + (double)s.phase * (double)13.0F) * turb);
                  }

                  s.vel = s.vel.method_1019(accel.method_1021((double)dt));
                  s.pos = s.pos.method_1019(s.vel.method_1021((double)dt));
               }

               if (view.valid) {
                  double[] p = Projection.project(s.pos, view);
                  if (p != null) {
                     float bodyFade = s.orbital ? 1.0F - (float)(this.taper.val() / (double)100.0F) * s.phase * 0.65F : 1.0F;
                     int a = (int)(235.0F * ease * bodyFade);
                     if (a >= 4) {
                        float sz = Math.max(1.0F, s.scale * (0.35F + 0.65F * ease));
                        int col = this.colourOf(s, ease, s.vel.method_1033(), a);
                        if (this.trails.is() && trailA > 2) {
                           double[] q = Projection.project(s.prev, view);
                           if (q != null) {
                              double dx = p[0] - q[0];
                              double dy = p[1] - q[1];
                              if (dx * dx + dy * dy < (double)6000.0F) {
                                 Render2D.line(ctx, (int)q[0], (int)q[1], (int)p[0], (int)p[1], this.colourOf(s, ease, (double)0.0F, (int)((float)(a * trailA) / 255.0F)));
                              }
                           }
                        }

                        if (this.glow.is() && a > 40) {
                           Render2D.softGlow(ctx, (float)p[0], (float)p[1], sz * 2.6F, sz * 2.6F, this.colourOf(s, ease, (double)0.0F, (int)((float)a * 0.16F)), 10);
                        }

                        Render2D.dot(ctx, (float)p[0] - sz / 2.0F, (float)p[1] - sz / 2.0F, sz, col, a);
                     }
                  }
               }
            }
         }

      }
   }

   private static final class Spark {
      class_243 pos;
      class_243 prev;
      class_243 vel;
      float life;
      float maxLife;
      float hueBase;
      float scale;
      boolean orbital;
      class_243 centre;
      class_243 axisA;
      class_243 axisB;
      double ang;
      double angVel;
      double orbR;
      float phase;
   }
}
