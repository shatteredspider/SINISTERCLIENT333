package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.ui.UiFont;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.TargetUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_3532;

public class TargetESP extends Module {
   private static final int COL_FRIEND = -11870592;
   private static final int COL_ENEMY = -495247;
   private final ModeSetting scope = (ModeSetting)this.reg(new ModeSetting("Scope", "Aim Target", new String[]{"Aim Target", "All Players", "All Living"}));
   private final BoolSetting box = (BoolSetting)this.reg(new BoolSetting("Box", true));
   private final BoolSetting corners = (BoolSetting)this.reg(new BoolSetting("Corners Only", true));
   private final BoolSetting healthBar = (BoolSetting)this.reg(new BoolSetting("Health Bar", true));
   private final BoolSetting nameTag = (BoolSetting)this.reg(new BoolSetting("Name", true));
   private final BoolSetting tracer = (BoolSetting)this.reg(new BoolSetting("Tracer", false));
   private final NumberSetting range = (NumberSetting)this.reg(new NumberSetting("Range", (double)24.0F, (double)4.0F, (double)96.0F, (double)1.0F));

   public TargetESP() {
      super("TargetESP", "Boxes and a tracer to the current target. Debugging tool first.", Category.RENDER);
      BoolSetting var10000 = this.corners;
      BoolSetting var10001 = this.box;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.field_1842) {
         int sw = mc.method_22683().method_4486();
         int sh = mc.method_22683().method_4502();
         double fov = (double)(Integer)mc.field_1690.method_41808().method_41753();
         float progress = mc.method_61966().method_60637(false);
         class_243 origin = mc.field_1724.method_5836(progress);
         float camYaw = mc.field_1724.method_36454();
         float camPitch = mc.field_1724.method_36455();

         for(class_1297 e : this.collect()) {
            this.drawEntity(ctx, e, origin, camYaw, camPitch, progress, sw, sh, fov);
         }
      }

   }

   private List<class_1297> collect() {
      List<class_1297> out = new ArrayList();
      if (this.scope.is("Aim Target")) {
         int id = HumanDiag.aimTargetId;
         if (id >= 0 && mc.field_1687 != null) {
            class_1297 e = mc.field_1687.method_8469(id);
            if (e != null && e != mc.field_1724 && e.method_5805()) {
               out.add(e);
            }
         }

         return out;
      } else {
         boolean playersOnly = this.scope.is("All Players");
         double r = this.range.val();

         for(class_1297 e : mc.field_1687.method_18112()) {
            if (e != mc.field_1724 && e.method_5805() && !e.method_31481() && e instanceof class_1309 && (!playersOnly || e instanceof class_1657)) {
               if (e instanceof class_1657) {
                  class_1657 p = (class_1657)e;
                  if (p.method_7325()) {
                     continue;
                  }
               }

               if (!((double)mc.field_1724.method_5739(e) > r)) {
                  out.add(e);
               }
            }
         }

         return out;
      }
   }

   private void drawEntity(class_332 ctx, class_1297 e, class_243 origin, float camYaw, float camPitch, float progress, int sw, int sh, double fov) {
      class_238 raw = e.method_5829();
      class_243 feetNow = new class_243(e.method_23317(), e.method_23318(), e.method_23321());
      class_238 b = raw.method_997(e.method_30950(progress).method_1020(feetNow));
      double minX = Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double maxX = -Double.MAX_VALUE;
      double maxY = -Double.MAX_VALUE;
      boolean any = false;
      double[] xs = new double[]{b.field_1323, b.field_1320};
      double[] ys = new double[]{b.field_1322, b.field_1325};
      double[] zs = new double[]{b.field_1321, b.field_1324};
      class_243 f = class_243.method_1030(camPitch, camYaw).method_1029();
      class_243 r = class_243.method_1030(0.0F, camYaw + 90.0F).method_1029();
      class_243 u = r.method_1036(f).method_1029();
      double[][] corners = new double[8][3];
      int corner = 0;
      for(double x : xs) {
         for(double y : ys) {
            for(double z : zs) {
               class_243 d = new class_243(x, y, z).method_1020(origin);
               corners[corner++] = new double[]{d.method_1026(r), d.method_1026(u), d.method_1026(f)};
            }
         }
      }
      List<double[]> visible = new ArrayList();
      for(double[] point : corners) {
         if (point[2] >= 0.05) {
            visible.add(point);
         }
      }
      // Clip each box edge that crosses the near plane, rather than rejecting
      // the entire entity when just one corner is behind the camera.
      for(int i = 0; i < corners.length; ++i) {
         for(int axis = 0; axis < 3; ++axis) {
            int neighbor = i ^ (1 << axis);
            if (i < neighbor) {
               double[] a = corners[i];
               double[] end = corners[neighbor];
               if ((a[2] < 0.05) != (end[2] < 0.05)) {
                  double t = (0.05 - a[2]) / (end[2] - a[2]);
                  visible.add(new double[]{a[0] + t * (end[0] - a[0]), a[1] + t * (end[1] - a[1]), 0.05});
               }
            }
         }
      }
      for(double[] point : visible) {
         double[] p = this.project(point[0], point[1], point[2], sw, sh, fov);
         any = true;
         minX = Math.min(minX, p[0]);
         maxX = Math.max(maxX, p[0]);
         minY = Math.min(minY, p[1]);
         maxY = Math.max(maxY, p[1]);
      }

      if (any && !(maxX < (double)0.0F) && !(minX > (double)sw) && !(maxY < (double)0.0F) && !(minY > (double)sh)) {
         int x1 = (int)Math.floor(minX);
         int y1 = (int)Math.floor(minY);
         int x2 = (int)Math.ceil(maxX);
         int y2 = (int)Math.ceil(maxY);
         if (x2 - x1 >= 2 && y2 - y1 >= 2) {
            boolean isAimTarget = e.method_5628() == HumanDiag.aimTargetId;
            boolean isFriend = SevenClient.get().friends.is(e);
            boolean isEnemy = !isFriend && SevenClient.get().enemies.is(e);
            int colour;
            if (isFriend) {
               colour = -11870592;
            } else if (isEnemy) {
               colour = -495247;
            } else if (isAimTarget) {
               colour = -1;
            } else {
               colour = -6381922;
            }

            if (this.box.is()) {
               if (this.corners.is()) {
                  this.drawCorners(ctx, x1, y1, x2, y2, colour);
               } else {
                  this.drawRect(ctx, x1, y1, x2, y2, colour);
               }
            }

            if (this.healthBar.is() && e instanceof class_1309) {
               class_1309 le = (class_1309)e;
               float frac = class_3532.method_15363(le.method_6032() / Math.max(1.0F, le.method_6063()), 0.0F, 1.0F);
               int barH = y2 - y1;
               int fill = (int)((float)barH * frac);
               ctx.method_25294(x1 - 4, y1, x1 - 2, y2, -1442840576);
               ctx.method_25294(x1 - 4, y2 - fill, x1 - 2, y2, colour);
            }

            if (this.nameTag.is()) {
               String var10000 = e.method_5477().getString();
               String label = var10000 + "  " + String.format("%.1fm", mc.field_1724.method_5739(e));
               int w = UiFont.width(label);
               UiFont.draw(ctx, label, (float)x1 + (float)(x2 - x1) / 2.0F - (float)w / 2.0F, (float)(y1 - UiFont.height()) - 2.0F, colour);
            }

            if (this.tracer.is()) {
               this.line(ctx, sw / 2, sh, (x1 + x2) / 2, y2, colour);
            }
         }
      }

   }

   private double[] project(double xc, double yc, double zc, int sw, int sh, double fovDeg) {
      double tanHalf = Math.tan(Math.toRadians(fovDeg) * (double)0.5F);
      double aspect = (double)sw / (double)Math.max(1, sh);
      double sx = (double)sw * (double)0.5F * ((double)1.0F + xc / zc / (tanHalf * aspect));
      double sy = (double)sh * (double)0.5F * ((double)1.0F - yc / zc / tanHalf);
      return new double[]{sx, sy};
   }

   private void drawRect(class_332 ctx, int x1, int y1, int x2, int y2, int c) {
      ctx.method_25294(x1, y1, x2, y1 + 1, c);
      ctx.method_25294(x1, y2 - 1, x2, y2, c);
      ctx.method_25294(x1, y1, x1 + 1, y2, c);
      ctx.method_25294(x2 - 1, y1, x2, y2, c);
   }

   private void drawCorners(class_332 ctx, int x1, int y1, int x2, int y2, int c) {
      int lx = Math.max(2, (x2 - x1) / 4);
      int ly = Math.max(2, (y2 - y1) / 6);
      ctx.method_25294(x1, y1, x1 + lx, y1 + 1, c);
      ctx.method_25294(x1, y1, x1 + 1, y1 + ly, c);
      ctx.method_25294(x2 - lx, y1, x2, y1 + 1, c);
      ctx.method_25294(x2 - 1, y1, x2, y1 + ly, c);
      ctx.method_25294(x1, y2 - 1, x1 + lx, y2, c);
      ctx.method_25294(x1, y2 - ly, x1 + 1, y2, c);
      ctx.method_25294(x2 - lx, y2 - 1, x2, y2, c);
      ctx.method_25294(x2 - 1, y2 - ly, x2, y2, c);
   }

   private void line(class_332 ctx, int x0, int y0, int x1, int y1, int c) {
      int dx = Math.abs(x1 - x0);
      int sx = x0 < x1 ? 1 : -1;
      int dy = -Math.abs(y1 - y0);
      int sy = y0 < y1 ? 1 : -1;
      int err = dx + dy;
      int guard = 0;

      while(guard++ < 4000) {
         ctx.method_25294(x0, y0, x0 + 1, y0 + 1, c);
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

   private static boolean weaponSanity(class_1799 s) {
      return TargetUtil.isWeapon(s, (String)null);
   }
}
