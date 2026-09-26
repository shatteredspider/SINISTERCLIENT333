package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import java.util.Objects;
import net.minecraft.class_332;

public class Crosshair extends Module {
   private final ModeSetting shape = (ModeSetting)this.reg(new ModeSetting("Shape", "Star", new String[]{"Star", "Cross", "Dot", "Circle"}));
   private final NumberSetting points = (NumberSetting)this.reg(new NumberSetting("Points", (double)4.0F, (double)3.0F, (double)12.0F, (double)1.0F));
   private final NumberSetting size = (NumberSetting)this.reg(new NumberSetting("Size", (double)7.0F, (double)2.0F, (double)30.0F, (double)1.0F));
   private final NumberSetting inner = (NumberSetting)this.reg(new NumberSetting("Inner Size", (double)2.0F, (double)0.0F, (double)20.0F, (double)1.0F));
   private final NumberSetting gap = (NumberSetting)this.reg(new NumberSetting("Centre Gap", (double)0.0F, (double)0.0F, (double)12.0F, (double)1.0F));
   private final NumberSetting thickness = (NumberSetting)this.reg(new NumberSetting("Thickness", (double)1.0F, (double)1.0F, (double)4.0F, (double)1.0F));
   private final NumberSetting rotation = (NumberSetting)this.reg(new NumberSetting("Rotation", (double)0.0F, (double)0.0F, (double)359.0F, (double)1.0F));
   private final NumberSetting alpha = (NumberSetting)this.reg(new NumberSetting("Opacity", (double)100.0F, (double)10.0F, (double)100.0F, (double)5.0F));
   private final BoolSetting outline = (BoolSetting)this.reg(new BoolSetting("Outline", true));
   private final BoolSetting dot = (BoolSetting)this.reg(new BoolSetting("Centre Dot", false));
   private final BoolSetting hideVanilla = (BoolSetting)this.reg(new BoolSetting("Hide In First Person", false));
   private final BoolSetting spin = (BoolSetting)this.reg(new BoolSetting("Spin", false));
   private final NumberSetting spinSpeed = (NumberSetting)this.reg(new NumberSetting("Spin Speed", (double)40.0F, (double)5.0F, (double)360.0F, (double)5.0F));
   private double phase = (double)0.0F;

   public Crosshair() {
      super("Crosshair", "Custom crosshair that also renders in third person.", Category.RENDER);
      this.points.visibleWhen(() -> this.shape.is("Star"));
      this.inner.visibleWhen(() -> this.shape.is("Star"));
      NumberSetting var10000 = this.spinSpeed;
      BoolSetting var10001 = this.spin;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1724 != null && !mc.field_1690.field_1842 && mc.field_1755 == null && (!this.hideVanilla.is() || !mc.field_1690.method_31044().method_31034())) {
         int cx = mc.method_22683().method_4486() / 2;
         int cy = mc.method_22683().method_4502() / 2;
         if (this.spin.is()) {
            this.phase += this.spinSpeed.val() * 0.016;
            if (this.phase > (double)360.0F) {
               this.phase -= (double)360.0F;
            }
         }

         double rot = Math.toRadians(this.rotation.val() + (this.spin.is() ? this.phase : (double)0.0F));
         int a = (int)((double)255.0F * (this.alpha.val() / (double)100.0F)) << 24;
         int col = 16448250 | a;
         int shade = 0 | (int)((double)200.0F * (this.alpha.val() / (double)100.0F)) << 24;
         switch ((String)this.shape.get()) {
            case "Cross" -> this.cross(ctx, cx, cy, col, shade);
            case "Dot" -> this.dotOnly(ctx, cx, cy, col, shade);
            case "Circle" -> this.circle(ctx, cx, cy, col, shade);
            default -> this.star(ctx, cx, cy, rot, col, shade);
         }

         if (this.dot.is()) {
            int dotT = (int)this.thickness.val();
            if (this.outline.is()) {
               ctx.method_25294(cx - dotT - 1, cy - dotT - 1, cx + dotT + 1, cy + dotT + 1, shade);
            }

            ctx.method_25294(cx - dotT, cy - dotT, cx + dotT, cy + dotT, col);
         }
      }

   }

   private void star(class_332 ctx, int cx, int cy, double rot, int col, int shade) {
      int n = (int)this.points.val() * 2;
      double outR = this.size.val();
      double inR = Math.max((double)0.0F, this.inner.val());

      for(int i = 0; i < n; ++i) {
         double ang = rot + (Math.PI * 2D) * (double)i / (double)n;
         double r = i % 2 == 0 ? outR : inR;
         if (!(r <= (double)0.0F)) {
            double g = this.gap.val();
            int x0 = (int)Math.round((double)cx + Math.cos(ang) * g);
            int y0 = (int)Math.round((double)cy + Math.sin(ang) * g);
            int x1 = (int)Math.round((double)cx + Math.cos(ang) * r);
            int y1 = (int)Math.round((double)cy + Math.sin(ang) * r);
            if (this.outline.is()) {
               this.line(ctx, x0, y0, x1, y1, shade, (int)this.thickness.val() + 1);
            }

            this.line(ctx, x0, y0, x1, y1, col, (int)this.thickness.val());
         }
      }

   }

   private void cross(class_332 ctx, int cx, int cy, int col, int shade) {
      int s = (int)this.size.val();
      int g = (int)this.gap.val();
      int t = (int)this.thickness.val();
      if (this.outline.is()) {
         ctx.method_25294(cx - s - 1, cy - t - 1, cx - g + 1, cy + t + 1, shade);
         ctx.method_25294(cx + g - 1, cy - t - 1, cx + s + 1, cy + t + 1, shade);
         ctx.method_25294(cx - t - 1, cy - s - 1, cx + t + 1, cy - g + 1, shade);
         ctx.method_25294(cx - t - 1, cy + g - 1, cx + t + 1, cy + s + 1, shade);
      }

      ctx.method_25294(cx - s, cy - t, cx - g, cy + t, col);
      ctx.method_25294(cx + g, cy - t, cx + s, cy + t, col);
      ctx.method_25294(cx - t, cy - s, cx + t, cy - g, col);
      ctx.method_25294(cx - t, cy + g, cx + t, cy + s, col);
   }

   private void dotOnly(class_332 ctx, int cx, int cy, int col, int shade) {
      int t = Math.max(1, (int)this.thickness.val());
      if (this.outline.is()) {
         ctx.method_25294(cx - t - 1, cy - t - 1, cx + t + 1, cy + t + 1, shade);
      }

      ctx.method_25294(cx - t, cy - t, cx + t, cy + t, col);
   }

   private void circle(class_332 ctx, int cx, int cy, int col, int shade) {
      double r = this.size.val();
      int steps = Math.max(12, (int)(r * (double)6.0F));

      for(int i = 0; i < steps; ++i) {
         double ang = (Math.PI * 2D) * (double)i / (double)steps;
         int x = (int)Math.round((double)cx + Math.cos(ang) * r);
         int y = (int)Math.round((double)cy + Math.sin(ang) * r);
         int t = (int)this.thickness.val();
         if (this.outline.is()) {
            ctx.method_25294(x - t, y - t, x + t, y + t, shade);
         }

         ctx.method_25294(x, y, x + t, y + t, col);
      }

   }

   private void line(class_332 ctx, int x0, int y0, int x1, int y1, int c, int t) {
      int dx = Math.abs(x1 - x0);
      int sx = x0 < x1 ? 1 : -1;
      int dy = -Math.abs(y1 - y0);
      int sy = y0 < y1 ? 1 : -1;
      int err = dx + dy;
      int guard = 0;

      while(guard++ < 512) {
         ctx.method_25294(x0, y0, x0 + t, y0 + t, c);
         if (x0 == x1 && y0 == y1) {
            break;
         }

         int e2 = 2 * err;
         if (e2 >= dy) {
            err += dy;
            x0 += sx;
         }

         if (e2 <= dx) {
            err += dx;
            y0 += sy;
         }
      }

   }
}
