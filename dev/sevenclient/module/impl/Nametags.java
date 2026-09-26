package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.Theme;
import dev.sevenclient.ui.UiFont;
import dev.sevenclient.util.Projection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9304;
import org.joml.Matrix3x2fStack;

public class Nametags extends Module {
   private final NumberSetting range = (NumberSetting)this.reg(new NumberSetting("Range", (double)64.0F, (double)4.0F, (double)128.0F, (double)1.0F));
   private final NumberSetting scale = (NumberSetting)this.reg(new NumberSetting("Scale", (double)100.0F, (double)50.0F, (double)200.0F, (double)5.0F));
   private final BoolSetting distanceScale = (BoolSetting)this.reg(new BoolSetting("Scale With Distance", true));
   private final BoolSetting showHealth = (BoolSetting)this.reg(new BoolSetting("Health", true));
   private final BoolSetting healthBar = (BoolSetting)this.reg(new BoolSetting("Health Bar", true));
   private final BoolSetting showDistance = (BoolSetting)this.reg(new BoolSetting("Distance", true));
   private final BoolSetting showArmor = (BoolSetting)this.reg(new BoolSetting("Armour", true));
   private final BoolSetting showEnchants = (BoolSetting)this.reg(new BoolSetting("Enchantments", true));
   private final BoolSetting showHeld = (BoolSetting)this.reg(new BoolSetting("Held Item", true));
   private final BoolSetting colourFriends = (BoolSetting)this.reg(new BoolSetting("Colour Friends", true));
   private final NumberSetting friendHue = (NumberSetting)this.reg(new NumberSetting("Friend Hue", (double)142.0F, (double)0.0F, (double)360.0F, (double)1.0F));
   private final NumberSetting bgAlpha = (NumberSetting)this.reg(new NumberSetting("Background Alpha", (double)72.0F, (double)0.0F, (double)100.0F, (double)1.0F));

   public Nametags() {
      super("Nametags", "Names, health, armour and enchantments above players.", Category.VISUALS);
      BoolSetting var10000 = this.healthBar;
      BoolSetting var10001 = this.showHealth;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.showEnchants;
      var10001 = this.showArmor;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      NumberSetting var2 = this.friendHue;
      var10001 = this.colourFriends;
      Objects.requireNonNull(var10001);
      var2.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.field_1842) {
         Projection.View view = Projection.capture();
         if (view.valid) {
            List<Tag> tags = new ArrayList();

            for(class_1297 e : mc.field_1687.method_18112()) {
               if (e instanceof class_1657) {
                  class_1657 p = (class_1657)e;
                  if (e != mc.field_1724 && e.method_5805() && !e.method_31481() && !p.method_7325()) {
                     double d = (double)mc.field_1724.method_5739(e);
                     if (!(d > this.range.val())) {
                        class_238 b = Projection.lerpedBox(e, view);
                        double[] head = Projection.project(new class_243(b.method_1005().field_1352, b.field_1325 + 0.45, b.method_1005().field_1350), view);
                        if (head != null) {
                           Tag t = new Tag();
                           t.player = p;
                           t.sx = head[0];
                           t.sy = head[1];
                           t.dist = d;
                           float base = (float)this.scale.val() / 100.0F;
                           t.s = this.distanceScale.is() ? base * (float)Math.max(0.55, Math.min((double)1.0F, (double)6.0F / Math.max((double)1.0F, d))) : base;
                           tags.add(t);
                        }
                     }
                  }
               }
            }

            tags.sort((a, bx) -> Double.compare(bx.dist, a.dist));

            for(Tag t : tags) {
               this.drawTag(ctx, t);
            }

         }
      }
   }

   private void drawTag(class_332 ctx, Tag t) {
      class_1657 p = t.player;
      boolean friend = SevenClient.get().friends.is(p);
      boolean enemy = SevenClient.get().enemies.is(p);
      String name = p.method_5477().getString();
      float hp = p.method_6032() + p.method_6067();
      float maxHp = Math.max(1.0F, p.method_6063());
      float frac = Math.max(0.0F, Math.min(1.0F, hp / maxHp));
      String right = "";
      if (this.showHealth.is()) {
         right = String.format(Locale.ROOT, "%.1f", hp);
      }

      if (this.showDistance.is()) {
         right = right.isEmpty() ? (int)t.dist + "m" : right + "  " + (int)t.dist + "m";
      }

      int nameW = UiFont.widthBold(name);
      int rightW = right.isEmpty() ? 0 : UiFont.width(right);
      float gap = right.isEmpty() ? 0.0F : 8.0F;
      float innerW = (float)nameW + gap + (float)rightW;
      float padX = 5.0F;
      float lineH = (float)UiFont.height() + 1.0F;
      float boxW = innerW + padX * 2.0F;
      float boxH = lineH + 6.0F + (this.showHealth.is() && this.healthBar.is() ? 3.0F : 0.0F);
      List<String> armour = this.showArmor.is() ? this.armourLine(p) : List.of();
      float armW = 0.0F;

      for(String a : armour) {
         armW += (float)UiFont.width(a) + 7.0F;
      }

      Matrix3x2fStack m = ctx.method_51448();
      m.pushMatrix();
      m.translate((float)t.sx, (float)t.sy);
      m.scale(t.s, t.s);
      float x = -boxW / 2.0F;
      float y = -boxH;
      if (!armour.isEmpty()) {
         float ax = -armW / 2.0F;
         float ay = y - lineH - 4.0F;
         Render2D.roundedRect(ctx, ax - 4.0F, ay - 2.0F, armW + 8.0F, lineH + 4.0F, 3.0F, Theme.withAlpha(0, (int)(this.bgAlpha.val() * 2.05)));

         for(String a : armour) {
            UiFont.draw(ctx, a, ax, ay, -5723992);
            ax += (float)UiFont.width(a) + 7.0F;
         }
      }

      int accent = friend && this.colourFriends.is() ? Projection.hsb((float)this.friendHue.val(), 0.75F, 1.0F, 255) : (enemy ? Projection.hsb(352.0F, 0.78F, 1.0F, 255) : -855310);
      Render2D.roundedRect(ctx, x, y, boxW, boxH, 3.5F, Theme.withAlpha(0, (int)(this.bgAlpha.val() * 2.55)));
      Render2D.roundedBorder(ctx, x, y, boxW, boxH, 3.5F, Theme.alpha(accent, 0.35F));
      UiFont.drawBold(ctx, name, x + padX, y + 3.0F, accent);
      if (!right.isEmpty()) {
         UiFont.drawRight(ctx, right, x + boxW - padX, y + 3.0F, frac < 0.35F ? Projection.hsb(0.0F, 0.8F, 1.0F, 255) : -5723992);
      }

      if (this.showHealth.is() && this.healthBar.is()) {
         float by = y + boxH - 4.0F;
         float bw = boxW - padX * 2.0F;
         Render2D.roundedRect(ctx, x + padX, by, bw, 2.0F, 1.0F, Theme.withAlpha(16777215, 40));
         if (frac > 0.01F) {
            Render2D.roundedRect(ctx, x + padX, by, bw * frac, 2.0F, 1.0F, Projection.hsb(frac * 110.0F, 0.85F, 1.0F, 235));
         }
      }

      m.popMatrix();
   }

   private List<String> armourLine(class_1657 p) {
      List<String> out = new ArrayList();
      class_1304[] slots = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};

      for(class_1304 slot : slots) {
         String s = this.describe(p.method_6118(slot));
         if (s != null) {
            out.add(s);
         }
      }

      if (this.showHeld.is()) {
         String s = this.describe(p.method_6118(class_1304.field_6173));
         if (s != null) {
            out.add(s);
         }
      }

      return out;
   }

   private String describe(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         String base = material(stack);
         if (!this.showEnchants.is()) {
            return base;
         } else {
            StringBuilder sb = new StringBuilder(base);

            try {
               class_9304 ench = stack.method_58657();
               if (ench != null && !ench.method_57543()) {
                  sb.append(' ');

                  for(class_6880<class_1887> e : ench.method_57534()) {
                     sb.append(code(e)).append(ench.method_57536(e));
                  }
               }
            } catch (Throwable var7) {
            }

            return sb.toString();
         }
      } else {
         return null;
      }
   }

   private static String material(class_1799 stack) {
      String path;
      try {
         path = class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
      } catch (Throwable var5) {
         return "?";
      }

      int u = path.indexOf(95);
      String head = u > 0 ? path.substring(0, u) : path;
      String s = head.length() <= 4 ? head : head.substring(0, 4);
      char var10000 = Character.toUpperCase(s.charAt(0));
      return var10000 + s.substring(1);
   }

   private static String code(class_6880<class_1887> e) {
      String id;
      try {
         id = e.method_55840();
      } catch (Throwable var5) {
         return "?";
      }

      int c = id.indexOf(58);
      String path = c >= 0 ? id.substring(c + 1) : id;
      if (path.contains("_")) {
         String[] parts = path.split("_");
         String var10000 = String.valueOf(Character.toUpperCase(parts[0].charAt(0)));
         return var10000 + Character.toUpperCase(parts[1].charAt(0));
      } else {
         return String.valueOf(Character.toUpperCase(path.charAt(0)));
      }
   }

   private static final class Tag {
      class_1657 player;
      double sx;
      double sy;
      double dist;
      float s;
   }
}
