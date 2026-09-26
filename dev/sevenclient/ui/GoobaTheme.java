package dev.sevenclient.ui;

public final class GoobaTheme {
   public static final int BOOT_BG = -16118220;
   public static final int BOOT_BG_RGB = 658996;
   public static final int STAGE_TEXT = -855308;
   public static final int SPLASH_TRACK = -9012608;
   public static final int SPLASH_FILL = -328966;
   public static final int TEXT_MAIN = -855308;
   public static final int TEXT_BRIGHT = -328966;
   public static final int TEXT_WHITE = -1;
   public static final int SUGGEST_BOX = -436207616;
   public static final int HAIRLINE_TOP = 1090519039;
   public static final int ROW_SELECTED = 1442840575;
   public static final int SUGGEST_SELECTED = 1442840575;
   public static final int SUGGEST_TEXT_SEL = -171;
   public static final int SUGGEST_TEXT_UNSEL = -4473925;
   public static final int TOAST_TEXT = -1315858;
   public static final int TOAST_LIGHT = -1315858;
   public static final int ALERT_BASE = -1161658;
   public static final int SCRIM = 771751936;
   public static final int ACCENT_A = -1;
   public static final int ACCENT_B = -4605497;
   public static final int TOGGLE_ON = -1;
   public static final int TOGGLE_ON_GRAD = -2500126;
   public static final int TOGGLE_OFF = -12959925;
   public static final int TOGGLE_OFF_GRAD = -13749440;
   public static final int SIDEBAR_BG = -435482576;
   public static final int SIDEBAR_HEADER = -855308;
   public static final int SIDEBAR_FOOTER = -4473925;
   public static final int PANEL_GRAD_EST = -435153352;

   private GoobaTheme() {
   }

   public static int alertColor(float var0) {
      float var1 = var0 < 0.0F ? 0.0F : (var0 > 1.0F ? 1.0F : var0);
      int var2 = 70 + Math.round(25.0F * var1);
      if (var2 < 0) {
         var2 = 0;
      }

      if (var2 > 255) {
         var2 = 255;
      }

      return -1179648 | var2 << 8 | var2;
   }

   public static int alpha(int var0, float var1) {
      int var2 = var0 & 16777215;
      int var3 = var0 >>> 24 & 255;
      float var4 = var1 < 0.0F ? 0.0F : (var1 > 1.0F ? 1.0F : var1);
      int var5 = (int)((float)var3 * var4);
      return var5 << 24 | var2;
   }

   public static int withAlpha(int var0, int var1) {
      return (var1 & 255) << 24 | var0 & 16777215;
   }

   public static int mix(int var0, int var1, float var2) {
      float var3 = var2 < 0.0F ? 0.0F : (var2 > 1.0F ? 1.0F : var2);
      int var4 = var0 >>> 24 & 255;
      int var5 = var0 >> 16 & 255;
      int var6 = var0 >> 8 & 255;
      int var7 = var0 & 255;
      int var8 = var1 >>> 24 & 255;
      int var9 = var1 >> 16 & 255;
      int var10 = var1 >> 8 & 255;
      int var11 = var1 & 255;
      return (int)((float)var4 + (float)(var8 - var4) * var3) << 24 | (int)((float)var5 + (float)(var9 - var5) * var3) << 16 | (int)((float)var6 + (float)(var10 - var6) * var3) << 8 | (int)((float)var7 + (float)(var11 - var7) * var3);
   }
}
