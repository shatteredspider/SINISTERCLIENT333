package dev.sevenclient.ui;

public final class Theme {
   public static final int SHADE = -939524096;
   public static final int SHADOW = -1275068416;
   public static final int WINDOW = -200866039;
   public static final int WINDOW_GRAD = -266724830;
   public static final int OUTLINE = 654311423;
   public static final int OUTLINE_SOFT = 318767103;
   public static final int HIGHLIGHT = 452984831;
   public static final int INNER_LIGHT = 352321535;
   public static final int SIDEBAR = -234552058;
   public static final int SIDEBAR_GRAD = -300674022;
   public static final int NAV_ACTIVE = -14869213;
   public static final int NAV_ACTIVE_GRAD = -15395559;
   public static final int PANEL = -435286510;
   public static final int PANEL_GRAD = -434496991;
   public static final int PANEL_HOVER = -266527453;
   public static final int PANEL_HOVER_GRAD = -267053799;
   public static final int HEADER = -267711730;
   public static final int TRACK = -14474455;
   public static final int TRACK_GRAD = -15066593;
   public static final int ON = -1;
   public static final int ON_GRAD = -2565928;
   public static final int OFF = -13948109;
   public static final int OFF_GRAD = -14606040;
   public static final int KNOB = -16250870;
   public static final int TEXT = -855308;
   public static final int TEXT_DIM = -5723992;
   public static final int TEXT_FAINT = -9737365;
   public static final int ACCENT = -1;
   public static final int ACCENT_GRAD = -2302756;
   public static final int ACCENT_HI = -1;
   public static final int ACCENT_SOFT = 1090519039;
   public static final int ACCENT_GLOW = 536870911;
   public static final int STAR_CORE = -1;
   public static final int STAR_MID = -3158058;
   public static final int STAR_FAR = -7697770;
   public static final int NEBULA = 336532512;

   private Theme() {
   }

   public static int alpha(int var0, float var1) {
      int var2 = var0 & 16777215;
      int var3 = var0 >>> 24 & 255;
      int var4 = (int)((float)var3 * Math.max(0.0F, Math.min(1.0F, var1)));
      return var4 << 24 | var2;
   }

   public static int withAlpha(int var0, int var1) {
      return (var1 & 255) << 24 | var0 & 16777215;
   }

   public static int mix(int var0, int var1, float var2) {
      var2 = Math.max(0.0F, Math.min(1.0F, var2));
      int var3 = var0 >>> 24 & 255;
      int var4 = var0 >> 16 & 255;
      int var5 = var0 >> 8 & 255;
      int var6 = var0 & 255;
      int var7 = var1 >>> 24 & 255;
      int var8 = var1 >> 16 & 255;
      int var9 = var1 >> 8 & 255;
      int var10 = var1 & 255;
      return (int)((float)var3 + (float)(var7 - var3) * var2) << 24 | (int)((float)var4 + (float)(var8 - var4) * var2) << 16 | (int)((float)var5 + (float)(var9 - var5) * var2) << 8 | (int)((float)var6 + (float)(var10 - var6) * var2);
   }
}
