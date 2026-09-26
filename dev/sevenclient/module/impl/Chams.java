package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.util.Projection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_638;

public class Chams extends Module {
   private final ModeSetting renderMode = (ModeSetting)this.reg(new ModeSetting("Render", "Model", new String[]{"Model", "Box", "Both"}));
   private final ModeSetting visibility = (ModeSetting)this.reg(new ModeSetting("Visibility", "Always", new String[]{"Always", "Through Walls Only"}));
   private final NumberSetting range = (NumberSetting)this.reg(new NumberSetting("Range", (double)48.0F, (double)4.0F, (double)128.0F, (double)1.0F));
   private final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", true));
   private final BoolSetting showFriends = (BoolSetting)this.reg(new BoolSetting("Show Friends", true));
   private final BoolSetting showSelf = (BoolSetting)this.reg(new BoolSetting("Include Self", false));
   private final NumberSetting enemyHue = (NumberSetting)this.reg(new NumberSetting("Enemy Hue", (double)352.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting friendHue = (NumberSetting)this.reg(new NumberSetting("Friend Hue", (double)142.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting saturation = (NumberSetting)this.reg(new NumberSetting("Saturation", (double)78.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting brightness = (NumberSetting)this.reg(new NumberSetting("Brightness", (double)100.0F, (double)20.0F, (double)100.0F, (double)1.0F));
   private final BoolSetting boxOutline = (BoolSetting)this.reg(new BoolSetting("Box Outline", true));
   private final NumberSetting fillAlpha = (NumberSetting)this.reg(new NumberSetting("Box Fill Alpha", (double)22.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting lineAlpha = (NumberSetting)this.reg(new NumberSetting("Box Line Alpha", (double)88.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting smoothing = (NumberSetting)this.reg(new NumberSetting("Box Smoothing", (double)72.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting fadeSpeed = (NumberSetting)this.reg(new NumberSetting("Fade Speed", (double)60.0F, (double)5.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting radius = (NumberSetting)this.reg(new NumberSetting("Box Radius", (double)3.0F, (double)0.0F, (double)10.0F, (double)0.5F));
   private static volatile Chams active;
   private final Map<Integer, Ghost> ghosts = new HashMap();
   private class_638 ghostWorld;
   private long lastNanos = 0L;

   public Chams() {
      super("Chams", "Outlines the player model, optionally only through walls.", Category.VISUALS);

      for(Setting<?> s : new Setting[]{this.boxOutline, this.fillAlpha, this.lineAlpha, this.smoothing, this.radius}) {
         s.visibleWhen(() -> !this.renderMode.is("Model"));
      }

      this.registerBindSettings();
   }

   public void onEnable() {
      active = this;
   }

   public void onDisable() {
      active = null;
      this.ghosts.clear();
      this.ghostWorld = null;
      this.lastNanos = 0L;
   }

   public void onTick() {
      this.refreshWorld();
   }

   private void refreshWorld() {
      if (this.ghostWorld != mc.field_1687) {
         this.ghosts.clear();
         this.lastNanos = 0L;
         this.ghostWorld = mc.field_1687;
      }
   }

   public static boolean wantsOutline(class_1297 e) {
      Chams c = active;
      return c != null && c.isEnabled() && !c.renderMode.is("Box") && c.targeted(e);
   }

   public static int outlineColour(class_1297 e) {
      Chams c = active;
      if (c != null && c.isEnabled() && !c.renderMode.is("Box") && c.targeted(e)) {
         boolean friend = SevenClient.get() != null && SevenClient.get().friends.is(e);
         return Projection.hsb((float)(friend ? c.friendHue.val() : c.enemyHue.val()), (float)c.saturation.val() / 100.0F, (float)c.brightness.val() / 100.0F, 255);
      } else {
         return 0;
      }
   }

   private boolean targeted(class_1297 e) {
      class_310 mc2 = class_310.method_1551();
      if (e != null && mc2.field_1724 != null && mc2.field_1687 != null && e.method_5805() && !e.method_31481()) {
         if (e == mc2.field_1724) {
            return this.showSelf.is();
         } else if (!(e instanceof class_1309)) {
            return false;
         } else if (this.playersOnly.is() && !(e instanceof class_1657)) {
            return false;
         } else {
            if (e instanceof class_1657) {
               class_1657 p = (class_1657)e;
               if (p.method_7325()) {
                  return false;
               }
            }

            if ((double)mc2.field_1724.method_5739(e) > this.range.val()) {
               return false;
            } else if (SevenClient.get() != null && SevenClient.get().friends.is(e) && !this.showFriends.is()) {
               return false;
            } else {
               return !this.visibility.is("Through Walls Only") || Projection.occluded(e);
            }
         }
      } else {
         return false;
      }
   }

   private float dt() {
      long now = System.nanoTime();
      if (this.lastNanos == 0L) {
         this.lastNanos = now;
         return 0.016F;
      } else {
         float d = (float)((double)(now - this.lastNanos) / (double)1.0E9F);
         this.lastNanos = now;
         return Math.max(5.0E-4F, Math.min(0.1F, d));
      }
   }

   private static float approach(float cur, float target, float speed, float dt) {
      return cur + (target - cur) * (1.0F - (float)Math.exp((double)(-speed * dt)));
   }

   public void onHudRender(class_332 ctx) {
      this.refreshWorld();
      if (!this.renderMode.is("Model") && mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.field_1842) {
         float dt = this.dt();
         Projection.View view = Projection.capture();
         if (view.valid) {
            float boxSpeed = 4.0F + (float)this.smoothing.val() * 0.34F;
            float fade = (float)this.fadeSpeed.val() * 0.16F;
            long now = System.currentTimeMillis();

            for(class_1297 e : mc.field_1687.method_18112()) {
               if (e != mc.field_1724 && e instanceof class_1309) {
                  boolean want = this.targeted(e);
                  float[] b = Projection.bounds(e, view);
                  Ghost g = (Ghost)this.ghosts.computeIfAbsent(e.method_5628(), (k) -> new Ghost());
                  g.touched = now;
                  if (b == null) {
                     g.alpha = approach(g.alpha, 0.0F, fade, dt);
                  } else {
                     if (!g.seeded) {
                        g.seeded = true;
                        g.x1 = b[0];
                        g.y1 = b[1];
                        g.x2 = b[2];
                        g.y2 = b[3];
                     } else {
                        g.x1 = approach(g.x1, b[0], boxSpeed, dt);
                        g.y1 = approach(g.y1, b[1], boxSpeed, dt);
                        g.x2 = approach(g.x2, b[2], boxSpeed, dt);
                        g.y2 = approach(g.y2, b[3], boxSpeed, dt);
                     }

                     g.alpha = approach(g.alpha, want ? 1.0F : 0.0F, fade, dt);
                     if (!(g.alpha < 0.01F)) {
                        boolean friend = SevenClient.get().friends.is(e);
                        float hue = (float)(friend ? this.friendHue.val() : this.enemyHue.val());
                        float sat = (float)this.saturation.val() / 100.0F;
                        float bri = (float)this.brightness.val() / 100.0F;
                        int fa = (int)(this.fillAlpha.val() * 2.55 * (double)g.alpha);
                        int la = (int)(this.lineAlpha.val() * 2.55 * (double)g.alpha);
                        float w = g.x2 - g.x1;
                        float h = g.y2 - g.y1;
                        if (!(w < 1.0F) && !(h < 1.0F)) {
                           float rad = (float)this.radius.val();
                           if (fa > 2) {
                              Render2D.roundedRect(ctx, g.x1, g.y1, w, h, rad, Projection.hsb(hue, sat, bri, Math.min(255, fa)));
                           }

                           if (this.boxOutline.is() && la > 2) {
                              Render2D.roundedBorder(ctx, g.x1, g.y1, w, h, rad, Projection.hsb(hue, sat, bri, Math.min(255, la)));
                           }
                        }
                     }
                  }
               }
            }

            Iterator<Map.Entry<Integer, Ghost>> it = this.ghosts.entrySet().iterator();

            while(it.hasNext()) {
               if (now - ((Ghost)((Map.Entry)it.next()).getValue()).touched > 3000L) {
                  it.remove();
               }
            }

         }
      }
   }

   private static final class Ghost {
      float x1;
      float y1;
      float x2;
      float y2;
      float alpha;
      boolean seeded;
      long touched;
   }
}
