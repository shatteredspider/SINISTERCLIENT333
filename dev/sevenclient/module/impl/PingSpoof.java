package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.Theme;
import dev.sevenclient.ui.UiFont;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.PingSpoofer;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public class PingSpoof extends Module {
   private final NumberSetting baseMs = (NumberSetting)this.reg(new NumberSetting("Ping MS", (double)45.0F, (double)0.0F, (double)800.0F, (double)1.0F));
   private final NumberSetting varianceMs = (NumberSetting)this.reg(new NumberSetting("Variance MS", (double)8.0F, (double)0.0F, (double)120.0F, (double)1.0F));
   private final ModeSetting distribution = (ModeSetting)this.reg(new ModeSetting("Distribution", "Mostly Below", new String[]{"Symmetric", "Mostly Below", "Mostly Above"}));
   private final BoolSetting scrollAdjust = (BoolSetting)this.reg(new BoolSetting("Scroll Adjust", true));
   public final KeySetting scrollKey = (KeySetting)this.reg(new KeySetting("Scroll Modifier"));
   private final NumberSetting scrollStep = (NumberSetting)this.reg(new NumberSetting("Scroll Step", (double)5.0F, (double)1.0F, (double)50.0F, (double)1.0F));
   private final BoolSetting hud = (BoolSetting)this.reg(new BoolSetting("HUD", true));
   private final ModeSetting corner = (ModeSetting)this.reg(new ModeSetting("HUD Corner", "Bottom Left", new String[]{"Top Left", "Top Right", "Bottom Left", "Bottom Right"}));
   private final BoolSetting graph = (BoolSetting)this.reg(new BoolSetting("HUD Graph", true));
   private final NumberSetting hudScale = (NumberSetting)this.reg(new NumberSetting("HUD Scale", (double)100.0F, (double)50.0F, (double)200.0F, (double)5.0F));
   private static volatile PingSpoof active;
   private static final int HISTORY = 72;
   private final float[] history = new float[72];
   private int histIndex = 0;
   private boolean histFilled = false;
   private float shown = 0.0F;
   private long lastNanos = 0L;

   public PingSpoof() {
      super("PingSpoof", "Inflates the ping other players see.", Category.PLAYER);
      KeySetting var10000 = this.scrollKey;
      BoolSetting var10001 = this.scrollAdjust;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      NumberSetting var1 = this.scrollStep;
      var10001 = this.scrollAdjust;
      Objects.requireNonNull(var10001);
      var1.visibleWhen(var10001::is);
      ModeSetting var2 = this.corner;
      var10001 = this.hud;
      Objects.requireNonNull(var10001);
      var2.visibleWhen(var10001::is);
      BoolSetting var3 = this.graph;
      var10001 = this.hud;
      Objects.requireNonNull(var10001);
      var3.visibleWhen(var10001::is);
      NumberSetting var4 = this.hudScale;
      var10001 = this.hud;
      Objects.requireNonNull(var10001);
      var4.visibleWhen(var10001::is);
      this.scrollKey.bind(342, false);
      this.registerBindSettings();
   }

   public void onEnable() {
      active = this;
      PingSpoofer.active = true;
   }

   public void onDisable() {
      active = null;
      PingSpoofer.active = false;
   }

   public void onTick() {
      PingSpoofer.active = true;
      PingSpoofer.baseMs = (int)this.baseMs.val();
      PingSpoofer.varianceMs = (int)this.varianceMs.val();
      double var10000;
      switch ((String)this.distribution.get()) {
         case "Mostly Below" -> var10000 = (double)-1.0F;
         case "Mostly Above" -> var10000 = (double)1.0F;
         default -> var10000 = (double)0.0F;
      }

      PingSpoofer.bias = var10000;
      this.history[this.histIndex] = (float)PingSpoofer.lastDelay;
      this.histIndex = (this.histIndex + 1) % 72;
      if (this.histIndex == 0) {
         this.histFilled = true;
      }

      HumanDiag.pingSpoofState = PingSpoofer.delayed + " held, last +" + PingSpoofer.lastDelay + "ms";
   }

   public static boolean handleScroll(double vertical) {
      PingSpoof p = active;
      if (p != null && p.isEnabled() && p.scrollAdjust.is() && vertical != (double)0.0F) {
         if (class_310.method_1551().field_1755 != null) {
            return false;
         } else if (p.scrollKey.isBound() && !p.scrollKey.down()) {
            return false;
         } else {
            double step = p.scrollStep.val() * Math.signum(vertical);
            p.baseMs.set(p.baseMs.val() + step);
            PingSpoofer.baseMs = (int)p.baseMs.val();
            return true;
         }
      } else {
         return false;
      }
   }

   public void onHudRender(class_332 ctx) {
      if (this.hud.is() && !mc.field_1690.field_1842 && mc.field_1755 == null) {
         long now = System.nanoTime();
         float dt = this.lastNanos == 0L ? 0.016F : (float)((double)(now - this.lastNanos) / (double)1.0E9F);
         this.lastNanos = now;
         dt = Math.max(5.0E-4F, Math.min(0.1F, dt));
         this.shown += ((float)PingSpoofer.lastDelay - this.shown) * (1.0F - (float)Math.exp((double)(-9.0F * dt)));
         float s = (float)this.hudScale.val() / 100.0F;
         float w = 108.0F;
         float h = this.graph.is() ? 46.0F : 30.0F;
         float margin = 6.0F;
         int sw = mc.method_22683().method_4486();
         int sh = mc.method_22683().method_4502();
         float x;
         float y;
         switch ((String)this.corner.get()) {
            case "Top Left":
               x = margin;
               y = margin;
               break;
            case "Top Right":
               x = (float)sw - w * s - margin;
               y = margin;
               break;
            case "Bottom Right":
               x = (float)sw - w * s - margin;
               y = (float)sh - h * s - margin;
               break;
            default:
               x = margin;
               y = (float)sh - h * s - margin;
         }

         Matrix3x2fStack m = ctx.method_51448();
         m.pushMatrix();
         m.translate(x, y);
         m.scale(s, s);
         Render2D.shadow(ctx, 0.0F, 0.0F, w, h, 6.0F, 4, Theme.withAlpha(0, 150));
         Render2D.roundedGradient(ctx, 0.0F, 0.0F, w, h, 6.0F, Theme.withAlpha(657932, 235), Theme.withAlpha(1447452, 235));
         Render2D.roundedBorder(ctx, 0.0F, 0.0F, w, h, 6.0F, 654311423);
         UiFont.draw(ctx, "PING", 9.0F, 7.0F, -9737365);
         String num = String.valueOf(Math.round(this.shown));
         UiFont.drawBold(ctx, num, 9.0F, 15.0F, -855310);
         UiFont.draw(ctx, "ms", 9.0F + (float)UiFont.widthBold(num) + 2.0F, 16.0F, -9737365);
         String var10000;
         switch ((String)this.distribution.get()) {
            case "Mostly Below" -> var10000 = "steady";
            case "Mostly Above" -> var10000 = "spiky";
            default -> var10000 = "even";
         }

         String tag = var10000;
         UiFont.drawRight(ctx, tag, w - 9.0F, 7.0F, -9737365);
         UiFont.drawRight(ctx, "target " + (int)this.baseMs.val(), w - 9.0F, 16.0F, -5723992);
         if (this.graph.is()) {
            float gx = 9.0F;
            float gy = 29.0F;
            float gw = w - 18.0F;
            float gh = 11.0F;
            Render2D.roundedRect(ctx, gx, gy, gw, gh, 2.0F, Theme.withAlpha(16777215, 14));
            int n = this.histFilled ? 72 : this.histIndex;
            if (n > 1) {
               float lo = Float.MAX_VALUE;
               float hi = -Float.MAX_VALUE;

               for(int i = 0; i < n; ++i) {
                  float v = this.history[i];
                  lo = Math.min(lo, v);
                  hi = Math.max(hi, v);
               }

               float span = Math.max(1.0F, hi - lo);
               int prevX = -1;
               int prevY = -1;

               for(int i = 0; i < n; ++i) {
                  int idx = (this.histIndex - n + i + 144) % 72;
                  float v = this.history[idx];
                  int px = (int)(gx + gw * (float)i / (float)Math.max(1, n - 1));
                  int py = (int)(gy + gh - 1.0F - (v - lo) / span * (gh - 2.0F));
                  if (prevX >= 0) {
                     Render2D.line(ctx, prevX, prevY, px, py, Theme.withAlpha(16777215, 170));
                  }

                  prevX = px;
                  prevY = py;
               }
            }
         }

         m.popMatrix();
      }
   }
}
