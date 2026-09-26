package dev.sevenclient.ui;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.impl.ClickGuiModule;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.module.setting.StringSetting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ClickGuiScreen extends class_437 {
   private static final float W = 540.0F;
   private static final float H = 320.0F;
   private static final float SIDE_W = 150.0F;
   private static final float PAD = 12.0F;
   private static final float ROW_H = 40.0F;
   private static final float COL_GAP = 8.0F;
   private static final float SET_H = 18.0F;
   private static final float SET_H_NUM = 30.0F;
   private static final float RADIUS = 12.0F;
   private static final float NAV_TOP = 54.0F;
   private static final float NAV_RH = 26.0F;
   private static final float NAV_GAP = 2.0F;
   private static final int PANEL_TOP = -181789118;
   private static final int PANEL_BOT = -183103448;
   private static final int SIDE_FILL = -400747728;
   private static final int HAIRLINE = 1442840575;
   private static final int GLOW = 815627242;
   private static final int PILL_SEL = 1503887336;
   private static final int TEXT_MAIN = -1;
   private static final int TEXT_DIM = -4607028;
   private static final int ROW_FILL = 352321535;
   private static final int ROW_LIGHT = 687865855;
   private static final int ROW_LINE = 536870911;
   private static final int TOG_ON = -6456342;
   private static final int TOG_OFF = -11910056;
   private static final int KNOB_OFF = -4607028;
   private static final int SEARCH_FILL = 822083583;
   private static final int STAR_COL = -3557121;
   private static Category category;
   private static final Set<String> expanded;
   private static String search;
   private static boolean searchFocus;
   private static float scrollTarget;
   private static long openedAt;
   private KeySetting listening;
   private NumberSetting dragging;
   private StringSetting editing;
   private double mx;
   private double my;
   private float wx;
   private float wy;
   private final List<Row> rows = new ArrayList();

   public ClickGuiScreen() {
      super(class_2561.method_43470("777"));
      openedAt = System.currentTimeMillis();
   }

   public boolean method_25421() {
      return false;
   }

   private float scale() {
      float var1 = (float)this.field_22789;
      float var2 = (float)this.field_22790;
      float var3 = Math.min(var1 / 580.0F, var2 / 360.0F);
      return Math.min(1.0F, Math.max(0.42F, var3));
   }

   private float winW() {
      return 540.0F * this.scale();
   }

   private float winH() {
      return 320.0F * this.scale();
   }

   private float winX() {
      return ((float)this.field_22789 - this.winW()) / 2.0F;
   }

   private float winY() {
      return ((float)this.field_22790 - this.winH()) / 2.0F;
   }

   private float sideW() {
      return 150.0F * this.scale();
   }

   private float pad() {
      return 12.0F * this.scale();
   }

   private float colGap() {
      return 8.0F * this.scale();
   }

   private float colW() {
      return (this.listW() - this.colGap()) / 2.0F;
   }

   private float listX() {
      return this.winX() + this.sideW() + this.pad();
   }

   private float listW() {
      return this.winW() - this.sideW() - this.pad() * 2.0F;
   }

   private float listTop() {
      return this.winY() + 74.0F * this.scale();
   }

   private float listBottom() {
      return this.winY() + this.winH() - this.pad();
   }

   private float listH() {
      return this.listBottom() - this.listTop();
   }

   private List<Module> visible() {
      ArrayList var1 = new ArrayList();
      String var2 = search.trim().toLowerCase(Locale.ROOT);

      for(Module var4 : SevenClient.get().modules.all()) {
         if (var2.isEmpty()) {
            if (var4.category() == category) {
               var1.add(var4);
            }
         } else if (var4.name().toLowerCase(Locale.ROOT).contains(var2) || var4.description().toLowerCase(Locale.ROOT).contains(var2)) {
            var1.add(var4);
         }
      }

      return var1;
   }

   private static int enabledCount(Category var0) {
      int var1 = 0;

      for(Module var3 : SevenClient.get().modules.all()) {
         if (var3.category() == var0 && var3.isEnabled()) {
            ++var1;
         }
      }

      return var1;
   }

   private static int totalCount(Category var0) {
      int var1 = 0;

      for(Module var3 : SevenClient.get().modules.all()) {
         if (var3.category() == var0) {
            ++var1;
         }
      }

      return var1;
   }

   private float settingsHeight(Module var1) {
      float var2 = 0.0F;

      for(Setting var4 : var1.settings()) {
         if (var4.visible()) {
            var2 += (var4 instanceof NumberSetting ? 30.0F : 18.0F) * this.scale() + 2.0F;
         }
      }

      return var2 + 6.0F * this.scale();
   }

   private float contentHeight() {
      float[] var1 = new float[]{0.0F, 0.0F};
      int var2 = 0;

      for(Module var4 : this.visible()) {
         int var5 = var2++ % 2;
         var1[var5] += 40.0F * this.scale() + 6.0F;
         float var6 = Anim.raw("exp:" + var4.name(), 0.0F);
         if (var6 > 0.002F) {
            var1[var5] += this.settingsHeight(var4) * var6;
         }
      }

      return Math.max(var1[0], var1[1]);
   }

   private void buildRows() {
      this.rows.clear();
      float var1 = this.scale();
      float var2 = this.colW();
      float var3 = var2 + this.colGap();
      float var4 = Anim.scroll("scroll", scrollTarget);
      float[] var5 = new float[]{this.listTop() - var4, this.listTop() - var4};
      int var6 = 0;

      for(Module var8 : this.visible()) {
         int var9 = var6++ % 2;
         float var10 = this.listX() + (float)var9 * var3;
         float var11 = var5[var9];
         float var12 = 40.0F * var1;
         this.rows.add(new Row(ClickGuiScreen.Kind.MODULE, var8, (Setting)null, var10, var11, var2, var12, 1.0F));
         var11 += var12 + 6.0F;
         String var22 = var8.name();
         String var13 = "exp:" + var22;
         float var14 = expanded.contains(var8.name()) ? 1.0F : 0.0F;
         float var15 = Anim.panel(var13, var14);
         if (var15 > 0.002F) {
            float var16 = Anim.easeOutCubic(var15);

            for(Setting var18 : var8.settings()) {
               if (var18.visible()) {
                  float var19 = (var18 instanceof NumberSetting ? 30.0F : 18.0F) * var1;
                  Kind var20 = var18 instanceof BoolSetting ? ClickGuiScreen.Kind.BOOL : (var18 instanceof NumberSetting ? ClickGuiScreen.Kind.NUMBER : (var18 instanceof ModeSetting ? ClickGuiScreen.Kind.MODE : (var18 instanceof KeySetting ? ClickGuiScreen.Kind.KEY : ClickGuiScreen.Kind.STRING)));
                  this.rows.add(new Row(var20, var8, var18, var10, var11, var2, var19 * var16, var16));
                  var11 += (var19 + 2.0F) * var16;
               }
            }

            var11 += 6.0F * var1 * var16;
         }

         var5[var9] = var11;
      }

   }

   private boolean inList(Row var1) {
      return var1.y + var1.h > this.listTop() - 2.0F && var1.y < this.listBottom() + 2.0F;
   }

   private static void star(class_332 var0, float var1, float var2, float var3, int var4) {
      float[] var5 = new float[10];
      float[] var6 = new float[10];

      for(int var7 = 0; var7 < 10; ++var7) {
         double var8 = (-Math.PI / 2D) + (double)var7 * Math.PI / (double)5.0F;
         float var10 = var7 % 2 == 0 ? var3 : var3 * 0.45F;
         var5[var7] = var1 + (float)(Math.cos(var8) * (double)var10);
         var6[var7] = var2 + (float)(Math.sin(var8) * (double)var10);
      }

      for(int var11 = 0; var11 < 10; ++var11) {
         int var12 = (var11 + 1) % 10;
         Render2D.line(var0, Math.round(var5[var11]), Math.round(var6[var11]), Math.round(var5[var12]), Math.round(var6[var12]), var4);
      }

   }

   private static void catIcon(class_332 var0, Category var1, float var2, float var3, float var4, int var5) {
      switch (var1.ordinal()) {
         case 0 -> Icons.combat(var0, var2, var3, var4, var5);
         case 1 -> Icons.movement(var0, var2, var3, var4, var5);
         case 2 -> Icons.player(var0, var2, var3, var4, var5);
         case 3 -> Icons.render(var0, var2, var3, var4, var5);
         default -> Icons.sliders(var0, var2, var3, var4, var5);
      }

   }

   public void method_25394(class_332 var1, int var2, int var3, float var4) {
      Anim.beginFrame();
      ClickGuiModule.sync();
      this.mx = (double)var2;
      this.my = (double)var3;
      float var5 = Anim.to("open", 1.0F, 11.0F);
      float var6 = Anim.easeOutCubic(var5);
      var1.method_25294(0, 0, this.field_22789, this.field_22790, Theme.alpha(-939524096, var6));
      float var7 = this.winX();
      float var8 = this.winY() + (1.0F - var6) * 16.0F;
      float var9 = this.winW();
      float var10 = this.winH();
      float var11 = 12.0F * this.scale();
      this.wx = var7;
      this.wy = var8;
      Render2D.softGlow(var1, var7 + var9 / 2.0F, var8 + var10 / 2.0F, var9 / 2.0F + 10.0F, var10 / 2.0F + 10.0F, Theme.alpha(815627242, var6), 12);
      Render2D.shadow(var1, var7, var8, var9, var10, var11, 6, Theme.alpha(-1275068416, var6));
      Render2D.roundedGradient(var1, var7, var8, var9, var10, var11, Theme.alpha(-181789118, var6), Theme.alpha(-183103448, var6));
      Render2D.roundedBorder(var1, var7, var8, var9, var10, var11, Theme.alpha(1442840575, var6));
      Render2D.innerTopLight(var1, var7, var8, var9, var11, Theme.alpha(687865855, var6));
      this.renderSidebar(var1, var7, var8, var10, var6);
      this.renderHeader(var1, var6);
      this.buildRows();
      var1.method_44379((int)this.listX() - 4, (int)this.listTop(), (int)(this.listX() + this.listW()) + 4, (int)this.listBottom());

      for(Row var13 : this.rows) {
         if (this.inList(var13) && var13.h > 0.6F) {
            this.renderRow(var1, var13, var6);
         }
      }

      var1.method_44380();
      this.renderScrollbar(var1, var6);
   }

   private void renderSidebar(class_332 var1, float var2, float var3, float var4, float var5) {
      float var6 = this.scale();
      float var7 = this.sideW();
      float var8 = 12.0F * var6;
      Render2D.roundedRect(var1, var2, var3, var7, var4, var8, Theme.alpha(-400747728, var5));
      var1.method_25294((int)(var2 + var7 - var8), (int)var3, (int)(var2 + var7), (int)(var3 + var4), Theme.alpha(-400747728, var5));
      var1.method_25294((int)(var2 + var7) - 1, (int)var3 + (int)var8, (int)(var2 + var7), (int)(var3 + var4 - var8), Theme.alpha(419430399, var5));
      float var9 = var2 + 14.0F * var6;
      star(var1, var9 + 5.0F * var6, var3 + 19.0F * var6, 5.0F * var6, Theme.alpha(-3557121, var5));
      UiFont.drawBold(var1, "777", var9 + 13.0F * var6, var3 + 14.0F * var6, Theme.alpha(-1, var5));
      UiFont.draw(var1, "v1.7.0", var9 + 13.0F * var6 + (float)UiFont.widthBold("777") + 5.0F, var3 + 15.0F * var6, Theme.alpha(-4607028, var5));
      float var10 = var3 + 40.0F * var6;
      UiFont.draw(var1, "MODULES", var9, var10, Theme.alpha(-4607028, var5 * 0.8F));
      var10 += 14.0F * var6;

      for(Category var14 : Category.values()) {
         float var15 = 26.0F * var6;
         boolean var16 = Render2D.hovered(this.mx, this.my, var2 + 8.0F, var10, var7 - 16.0F, var15);
         boolean var17 = var14 == category && search.trim().isEmpty();
         float var18 = Anim.hover("nav:" + var14.name(), var16);
         float var19 = Anim.toggle("navsel:" + var14.name(), var17);
         if (var19 > 0.01F) {
            Render2D.roundedRect(var1, var2 + 8.0F, var10, var7 - 16.0F, var15, 7.0F * var6, Theme.alpha(1503887336, var19 * var5));
         }

         if (var18 > 0.01F && var19 < 0.99F) {
            Render2D.roundedRect(var1, var2 + 8.0F, var10, var7 - 16.0F, var15, 7.0F * var6, Theme.alpha(352321535, var18 * (1.0F - var19) * var5));
         }

         int var20 = Theme.mix(-4607028, -1, Math.max(var19, var18 * 0.5F));
         float var21 = 10.0F * var6;
         float var22 = var2 + 16.0F * var6;
         catIcon(var1, var14, var22, var10 + (var15 - var21) / 2.0F, var21, Theme.alpha(var20, var5));
         int var23 = Theme.mix(-4607028, -1, var19);
         UiFont.draw(var1, var14.label(), var22 + var21 + 6.0F * var6, var10 + (var15 - (float)UiFont.height()) / 2.0F, Theme.alpha(var23, var5));
         String var24 = String.valueOf(totalCount(var14));
         UiFont.drawRight(var1, var24, var2 + var7 - 16.0F * var6, var10 + (var15 - (float)UiFont.height()) / 2.0F, Theme.alpha(-4607028, var5 * 0.8F));
         var10 += var15 + 2.0F;
      }

      float var26 = var3 + var4 - 26.0F * var6;
      int var27 = 0;

      for(Module var29 : SevenClient.get().modules.all()) {
         if (var29.isEnabled()) {
            ++var27;
         }
      }

      UiFont.draw(var1, var27 + " enabled", var9, var26, Theme.alpha(-4607028, var5));
   }

   private void renderHeader(class_332 var1, float var2) {
      float var4 = this.scale();
      float var5 = this.listX();
      float var6 = this.winY() + 22.0F * var4;
      String var7 = search.trim().isEmpty() ? category.label() : "Search";
      UiFont.drawBold(var1, var7, var5, var6, Theme.alpha(-1, var2));
      float var8 = (float)UiFont.widthBold(var7);
      UiFont.draw(var1, " Modules", var5 + var8, var6, Theme.alpha(-4607028, var2));
      List var9 = this.visible();
      int var10 = 0;

      for(Module var12 : var9) {
         if (var12.isEnabled()) {
            ++var10;
         }
      }

      int var10001 = var9.size();
      UiFont.draw(var1, var10001 + " modules  ·  " + var10 + " enabled", var5, var6 + 13.0F * var4, Theme.alpha(-4607028, var2 * 0.85F));
      float var19 = Math.min(190.0F * var4, this.listW() * 0.46F);
      float var20 = this.listX() + this.listW() - var19;
      float var13 = var6 - 3.0F;
      float var14 = 22.0F * var4;
      boolean var15 = Render2D.hovered(this.mx, this.my, var20, var13, var19, var14);
      float var16 = Anim.hover("searchf", searchFocus || var15);
      Render2D.roundedRect(var1, var20, var13, var19, var14, 7.0F * var4, Theme.alpha(822083583, var2));
      Render2D.roundedBorder(var1, var20, var13, var19, var14, 7.0F * var4, Theme.alpha(Theme.mix(587202559, 1442840575, var16), var2));
      Render2D.magnifier(var1, var20 + 11.0F, var13 + var14 / 2.0F, Theme.alpha(-4607028, var2));
      String var3 = search.isEmpty() && !searchFocus ? "Search modules" : search;
      if (searchFocus && System.currentTimeMillis() / 500L % 2L == 0L) {
         var3 = (String)var3 + "|";
      }

      int var18 = search.isEmpty() && !searchFocus ? -4607028 : -1;
      UiFont.draw(var1, UiFont.trim(var3, (int)(var19 - 30.0F)), var20 + 21.0F, var13 + (var14 - (float)UiFont.height()) / 2.0F, Theme.alpha(var18, var2));
   }

   private void renderRow(class_332 var1, Row var2, float var3) {
      float var4 = this.scale();
      float var5 = var3 * var2.fade;
      boolean var6 = var2.hit(this.mx, this.my) && this.my > (double)this.listTop() && this.my < (double)this.listBottom();
      if (var2.kind == ClickGuiScreen.Kind.MODULE) {
         Module var16 = var2.module;
         float var17 = Anim.hover("hov:" + var16.name(), var6);
         float var22 = Anim.toggle("en:" + var16.name(), var16.isEnabled());
         Render2D.roundedRect(var1, var2.x, var2.y, var2.w, var2.h, 8.0F * var4, Theme.alpha(352321535, var5));
         if (var17 > 0.01F) {
            Render2D.roundedRect(var1, var2.x, var2.y, var2.w, var2.h, 8.0F * var4, Theme.alpha(268435455, var17 * var5));
         }

         Render2D.roundedBorder(var1, var2.x, var2.y, var2.w, var2.h, 8.0F * var4, Theme.alpha(Theme.mix(536870911, 1442840575, Math.max(var17, var22 * 0.6F)), var5));
         Render2D.innerTopLight(var1, var2.x, var2.y, var2.w, 8.0F * var4, Theme.alpha(687865855, var5));
         float var26 = var2.y + (var2.h - (float)UiFont.height()) / 2.0F;
         UiFont.drawBold(var1, UiFont.trim(var16.name(), (int)(var2.w - 78.0F * var4)), var2.x + 12.0F * var4, var26, Theme.alpha(Theme.mix(-4607028, -1, var22), var5));
         float var30 = Anim.raw("exp:" + var16.name(), 0.0F);
         if (!var16.settings().isEmpty()) {
            Render2D.chevron(var1, var2.x + var2.w - 46.0F * var4, var2.y + var2.h / 2.0F, 3.0F * var4, var30, Theme.alpha(-4607028, var5));
         }

         this.drawToggle(var1, var2.x + var2.w - 34.0F * var4, var2.y + (var2.h - 14.0F * var4) / 2.0F, var4, var22, var5);
      } else {
         if (var6) {
            Render2D.roundedRect(var1, var2.x, var2.y, var2.w, var2.h, 5.0F * var4, Theme.alpha(352321535, var5 * 0.6F));
         }

         float var7 = var2.x + 12.0F * var4;
         float var8 = var2.y + (var2.h - (float)UiFont.height()) / 2.0F;
         switch (var2.kind.ordinal()) {
            case 1:
               BoolSetting var20 = (BoolSetting)var2.setting;
               UiFont.draw(var1, UiFont.trim(var20.name(), (int)(var2.w - 52.0F * var4)), var7, var8, Theme.alpha(-4607028, var5));
               float var24 = Anim.toggle("bs:" + var2.module.name() + var20.name(), var20.is());
               this.drawToggle(var1, var2.x + var2.w - 34.0F * var4, var2.y + (var2.h - 14.0F * var4) / 2.0F, var4, var24, var5);
               break;
            case 2:
               NumberSetting var19 = (NumberSetting)var2.setting;
               UiFont.draw(var1, UiFont.trim(var19.name(), (int)(var2.w - 70.0F * var4)), var7, var2.y + 3.0F * var4, Theme.alpha(-4607028, var5));
               UiFont.drawRightBold(var1, fmt(var19.val()), var2.x + var2.w - 12.0F * var4, var2.y + 3.0F * var4, Theme.alpha(-1, var5));
               float var28 = var2.w - 24.0F * var4;
               float var32 = var2.y + var2.h - 10.0F * var4;
               Render2D.roundedRect(var1, var7, var32, var28, 3.0F, 1.5F, Theme.alpha(-11910056, var5));
               float var35 = (float)((double)var28 * var19.normalized());
               if (var35 > 0.5F) {
                  Render2D.roundedGradient(var1, var7, var32, var35, 3.0F, 1.5F, Theme.alpha(-6456342, var5), Theme.alpha(-3557121, var5));
               }

               boolean var36 = this.dragging == var19;
               float var15 = (var36 ? 5.0F : 4.0F) * var4;
               if (var36) {
                  Render2D.disc(var1, var7 + var35, var32 + 1.5F, 8.0F * var4, Theme.alpha(-6456342, 0.25F * var5));
               }

               Render2D.disc(var1, var7 + var35, var32 + 1.5F, var15, Theme.alpha(-1, var5));
               break;
            case 3:
               ModeSetting var18 = (ModeSetting)var2.setting;
               UiFont.draw(var1, UiFont.trim(var18.name(), (int)(var2.w * 0.4F)), var7, var8, Theme.alpha(-4607028, var5));
               String var23 = UiFont.trim((String)var18.get(), (int)(var2.w * 0.45F));
               float var27 = Math.min(var2.w * 0.55F, (float)UiFont.width(var23) + 18.0F);
               float var31 = var2.x + var2.w - 12.0F * var4 - var27;
               boolean var34 = Render2D.hovered(this.mx, this.my, var31, var2.y + 2.0F, var27, var2.h - 4.0F);
               float var14 = Anim.hover("md:" + var2.module.name() + var18.name(), var34);
               Render2D.roundedRect(var1, var31, var2.y + 2.0F, var27, var2.h - 4.0F, 5.0F * var4, Theme.alpha(Theme.mix(352321535, 822083583, var14), var5));
               Render2D.roundedBorder(var1, var31, var2.y + 2.0F, var27, var2.h - 4.0F, 5.0F * var4, Theme.alpha(587202559, var5));
               UiFont.drawCentered(var1, var23, var31 + var27 / 2.0F, var8, Theme.alpha(-1, var5));
               break;
            case 4:
               KeySetting var9 = (KeySetting)var2.setting;
               UiFont.draw(var1, UiFont.trim(var9.name(), (int)(var2.w * 0.4F)), var7, var8, Theme.alpha(-4607028, var5));
               String var10 = this.listening == var9 ? "press a key" : var9.label();
               float var11 = Math.min(var2.w * 0.55F, (float)UiFont.width(var10) + 18.0F);
               float var12 = var2.x + var2.w - 12.0F * var4 - var11;
               boolean var13 = this.listening == var9;
               Render2D.roundedRect(var1, var12, var2.y + 2.0F, var11, var2.h - 4.0F, 5.0F * var4, Theme.alpha(var13 ? 1503887336 : 352321535, var5));
               Render2D.roundedBorder(var1, var12, var2.y + 2.0F, var11, var2.h - 4.0F, 5.0F * var4, Theme.alpha(var13 ? 1442840575 : 587202559, var5));
               UiFont.drawCentered(var1, var10, var12 + var11 / 2.0F, var8, Theme.alpha(var13 ? -1 : -4607028, var5));
               break;
            default:
               StringSetting var21 = (StringSetting)var2.setting;
               UiFont.draw(var1, UiFont.trim(var21.name(), (int)(var2.w * 0.35F)), var7, var8, Theme.alpha(-4607028, var5));
               float var25 = Math.min(var2.w * 0.55F, Math.max(80.0F, (float)UiFont.width((String)var21.get()) + 22.0F));
               float var29 = var2.x + var2.w - 12.0F * var4 - var25;
               Render2D.roundedRect(var1, var29, var2.y + 2.0F, var25, var2.h - 4.0F, 5.0F * var4, Theme.alpha(352321535, var5));
               Render2D.roundedBorder(var1, var29, var2.y + 2.0F, var25, var2.h - 4.0F, 5.0F * var4, Theme.alpha(this.editing == var21 ? 1442840575 : 587202559, var5));
               String var10000 = (String)var21.get();
               String var33 = var10000 + (this.editing == var21 && System.currentTimeMillis() / 500L % 2L == 0L ? "|" : "");
               UiFont.draw(var1, UiFont.trim(var33, (int)var25 - 14), var29 + 8.0F, var8, Theme.alpha(-1, var5));
         }

      }
   }

   private void drawToggle(class_332 var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = 26.0F * var4;
      float var8 = 14.0F * var4;
      float var9 = var8 / 2.0F;
      Render2D.roundedRect(var1, var2, var3, var7, var8, var9, Theme.alpha(Theme.mix(-11910056, -6456342, var5), var6));
      if (var5 > 0.02F) {
         Render2D.disc(var1, var2 + var7 - var9, var3 + var9, (var9 + 2.5F * var4) * var5, Theme.alpha(-6456342, 0.22F * var5 * var6));
      }

      float var10 = var2 + var9 + (var7 - var8) * Anim.easeOutBack(var5);
      Render2D.disc(var1, var10, var3 + var9, var9 - 2.0F, Theme.alpha(Theme.mix(-4607028, -1, var5), var6));
   }

   private void renderScrollbar(class_332 var1, float var2) {
      float var4 = this.contentHeight();
      float var3;
      if (!(var4 <= (var3 = this.listH()))) {
         float var5 = this.listX() + this.listW() + 3.0F;
         float var6 = var3 / var4;
         float var7 = Math.max(24.0F, var3 * var6);
         float var8 = var4 - var3;
         float var9 = var8 <= 0.0F ? 0.0F : Anim.raw("scroll", 0.0F) / var8;
         float var10 = this.listTop() + (var3 - var7) * Anim.clamp01(var9);
         Render2D.roundedRect(var1, var5, this.listTop(), 3.0F, var3, 1.5F, Theme.alpha(352321535, var2 * 0.6F));
         Render2D.roundedRect(var1, var5, var10, 3.0F, var7, 1.5F, Theme.alpha(-6456342, var2 * 0.55F));
      }
   }

   private static String fmt(double var0) {
      return var0 == Math.floor(var0) && !Double.isInfinite(var0) ? String.valueOf((int)var0) : String.format(Locale.ROOT, "%.2f", var0);
   }

   public boolean method_25402(class_11909 var1, boolean var2) {
      double var3 = var1.comp_4798();
      double var5 = var1.comp_4799();
      int var7 = var1.method_74245();
      this.mx = var3;
      this.my = var5;
      float var8 = this.scale();
      float var9 = this.wx;
      float var10 = this.sideW();
      float var11 = this.wy + 54.0F * var8;

      for(Category var15 : Category.values()) {
         float var16 = 26.0F * var8;
         if (Render2D.hovered(var3, var5, var9 + 8.0F, var11, var10 - 16.0F, var16)) {
            category = var15;
            search = "";
            searchFocus = false;
            scrollTarget = 0.0F;
            Anim.set("scroll", 0.0F);
            return true;
         }

         var11 += var16 + 2.0F;
      }

      float var21 = Math.min(190.0F * var8, this.listW() * 0.46F);
      float var22 = this.listX() + this.listW() - var21;
      float var23 = this.winY() + 19.0F * var8;
      float var24 = 22.0F * var8;
      if (var3 >= (double)var22 && var3 <= (double)(var22 + var21) && var5 >= (double)var23 && var5 <= (double)(var23 + var24)) {
         searchFocus = true;
         this.editing = null;
         this.listening = null;
         return true;
      } else {
         searchFocus = false;
         if (!(var5 < (double)this.listTop()) && !(var5 > (double)this.listBottom())) {
            for(Row var17 : this.rows) {
               if (var17.hit(var3, var5) && !(var17.h < 1.0F)) {
                  switch (var17.kind.ordinal()) {
                     case 0:
                        Module var27 = var17.module;
                        if (var7 == 1) {
                           if (!var27.settings().isEmpty() && !expanded.remove(var27.name())) {
                              expanded.add(var27.name());
                           }

                           return true;
                        }

                        float var19 = var17.x + var17.w - 56.0F * var8;
                        float var20 = var17.x + var17.w - 36.0F * var8;
                        if (!var27.settings().isEmpty() && var3 >= (double)(var19 - 2.0F) && var3 <= (double)(var20 + 2.0F)) {
                           if (!expanded.remove(var27.name())) {
                              expanded.add(var27.name());
                           }

                           return true;
                        }

                        var27.toggle();
                        return true;
                     case 1:
                        ((BoolSetting)var17.setting).toggle();
                        return true;
                     case 2:
                        NumberSetting var26;
                        this.dragging = var26 = (NumberSetting)var17.setting;
                        this.applySlider(var26, var17, var3);
                        return true;
                     case 3:
                        ((ModeSetting)var17.setting).cycle(var7 == 1 ? -1 : 1);
                        return true;
                     case 4:
                        KeySetting var18 = (KeySetting)var17.setting;
                        if (var7 == 1) {
                           var18.clear();
                        } else {
                           this.listening = var18;
                        }

                        return true;
                     default:
                        this.editing = (StringSetting)var17.setting;
                        return true;
                  }
               }
            }

            this.editing = null;
            return super.method_25402(var1, var2);
         } else {
            return super.method_25402(var1, var2);
         }
      }
   }

   private void applySlider(NumberSetting var1, Row var2, double var3) {
      float var5 = this.scale();
      float var6 = var2.x + 12.0F * var5;
      float var7 = var2.w - 24.0F * var5;
      var1.setNormalized((var3 - (double)var6) / (double)Math.max(1.0F, var7));
   }

   public boolean method_25403(class_11909 var1, double var2, double var4) {
      this.mx = var1.comp_4798();
      this.my = var1.comp_4799();
      if (this.dragging != null) {
         for(Row var7 : this.rows) {
            if (var7.setting == this.dragging) {
               this.applySlider(this.dragging, var7, var1.comp_4798());
               return true;
            }
         }
      }

      return super.method_25403(var1, var2, var4);
   }

   public boolean method_25406(class_11909 var1) {
      this.dragging = null;
      return super.method_25406(var1);
   }

   public boolean method_25401(double var1, double var3, double var5, double var7) {
      float var9 = this.contentHeight();
      float var10 = this.listH();
      float var11 = Math.max(0.0F, var9 - var10);
      scrollTarget = Math.max(0.0F, Math.min(var11, scrollTarget - (float)var7 * 34.0F));
      return true;
   }

   public boolean method_25404(class_11908 var1) {
      int var2 = var1.comp_4795();
      if (this.listening != null) {
         if (var2 == 256) {
            this.listening.clear();
         } else {
            this.listening.bind(var2, false);
         }

         this.listening = null;
         return true;
      } else if (this.editing != null) {
         if (var2 == 259) {
            this.editing.backspace();
            return true;
         } else if (var2 != 257 && var2 != 256) {
            return true;
         } else {
            this.editing = null;
            return true;
         }
      } else {
         if (searchFocus) {
            if (var2 == 259) {
               if (!search.isEmpty()) {
                  search = search.substring(0, search.length() - 1);
               }

               return true;
            }

            if (var2 == 257) {
               searchFocus = false;
               return true;
            }

            if (var2 == 256) {
               search = "";
               searchFocus = false;
               return true;
            }
         }

         if (var2 == 256) {
            this.method_25419();
            return true;
         } else {
            return super.method_25404(var1);
         }
      }
   }

   public boolean method_25400(class_11905 var1) {
      int var2 = var1.comp_4793();
      if (var2 < 32) {
         return false;
      } else if (this.editing != null) {
         this.editing.append((char)var2);
         return true;
      } else if (searchFocus) {
         search = search + (char)var2;
         scrollTarget = 0.0F;
         return true;
      } else {
         searchFocus = true;
         search = String.valueOf((char)var2);
         scrollTarget = 0.0F;
         return true;
      }
   }

   public void method_25419() {
      this.listening = null;
      this.editing = null;
      this.dragging = null;
      searchFocus = false;
      Anim.set("open", 0.0F);
      if (SevenClient.get() != null && SevenClient.get().config != null) {
         SevenClient.get().config.save();
      }

      super.method_25419();
   }

   static {
      category = Category.COMBAT;
      expanded = new HashSet();
      search = "";
      searchFocus = false;
      scrollTarget = 0.0F;
      openedAt = 0L;
   }

   private static final class Row {
      Kind kind;
      Module module;
      Setting<?> setting;
      float x;
      float y;
      float w;
      float h;
      float fade = 1.0F;

      Row(Kind var1, Module var2, Setting<?> var3, float var4, float var5, float var6, float var7, float var8) {
         this.kind = var1;
         this.module = var2;
         this.setting = var3;
         this.x = var4;
         this.y = var5;
         this.w = var6;
         this.h = var7;
         this.fade = var8;
      }

      boolean hit(double var1, double var3) {
         return var1 >= (double)this.x && var1 <= (double)(this.x + this.w) && var3 >= (double)this.y && var3 <= (double)(this.y + this.h);
      }
   }

   private static enum Kind {
      MODULE,
      BOOL,
      NUMBER,
      MODE,
      KEY,
      STRING;

      // $FF: synthetic method
      private static Kind[] $values() {
         return new Kind[]{MODULE, BOOL, NUMBER, MODE, KEY, STRING};
      }
   }
}
