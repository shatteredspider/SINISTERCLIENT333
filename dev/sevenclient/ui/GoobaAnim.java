package dev.sevenclient.ui;

public final class GoobaAnim {
   public static final int TOGGLE_MS = 300;
   public static final int INTRO_MS = 450;
   public static final int FADE_MS = 420;
   public static final float HOVER_RATE = 0.18F;
   public static final float PRESS_RATE = 0.35F;
   public static final float SEARCH_FADE_RATE = 5.5F;
   public static final float PROGRESS_KEEP = 0.95F;
   public static final float PROGRESS_GAIN = 0.05F;
   public static final int FRAME_DT_CLAMP_MS = 50;
   public static final int ANIM_COLOR_TTL_S = 60;
   public static final float RAINBOW_PERIOD_DEG = 360.0F;
   public static final float SPEED_HOVER = 16.0F;
   public static final float SPEED_TOGGLE = 13.0F;
   public static final float SPEED_PANEL = 9.0F;
   public static final float SPEED_SCROLL = 14.0F;

   private GoobaAnim() {
   }

   public static float damp(float var0, float var1, float var2, float var3) {
      double var4 = (double)1.0F - Math.exp((double)(-Math.max(0.05F, var2) * Math.max(0.0F, var3)));
      return (float)((double)var0 + (double)(var1 - var0) * var4);
   }

   public static float dampClamped(float var0, float var1, float var2, float var3) {
      float var4 = var3 > 0.05F ? 0.05F : (var3 < 0.0F ? 0.0F : var3);
      return damp(var0, var1, var2, var4);
   }

   public static float tween01(float var0, float var1) {
      if (var1 <= 0.0F) {
         return 1.0F;
      } else {
         float var2 = var0 / var1;
         return var2 < 0.0F ? 0.0F : (var2 > 1.0F ? 1.0F : var2);
      }
   }

   public static float introEased(float var0) {
      return easeOutCubic(tween01(var0, 450.0F));
   }

   public static float fadeEased(float var0) {
      return smoothstep01(tween01(var0, 420.0F));
   }

   public static float toastOpen(float var0) {
      return easeOutCubic(var0);
   }

   public static float clamp01(float var0) {
      return var0 < 0.0F ? 0.0F : (var0 > 1.0F ? 1.0F : var0);
   }

   public static float smoothstep01(float var0) {
      float var1 = clamp01(var0);
      return var1 * var1 * (3.0F - 2.0F * var1);
   }

   public static float easeLinear(float var0) {
      return clamp01(var0);
   }

   public static float easeInQuad(float var0) {
      float var1 = clamp01(var0);
      return var1 * var1;
   }

   public static float easeOutQuad(float var0) {
      float var1 = clamp01(var0);
      return 1.0F - (1.0F - var1) * (1.0F - var1);
   }

   public static float easeInOutQuad(float var0) {
      float var1 = clamp01(var0);
      if (var1 < 0.5F) {
         return 2.0F * var1 * var1;
      } else {
         float var2 = -2.0F * var1 + 2.0F;
         return 1.0F - var2 * var2 / 2.0F;
      }
   }

   public static float easeInCubic(float var0) {
      float var1 = clamp01(var0);
      return var1 * var1 * var1;
   }

   public static float easeOutCubic(float var0) {
      float var1 = clamp01(var0);
      float var2 = 1.0F - var1;
      return 1.0F - var2 * var2 * var2;
   }

   public static float easeInOutCubic(float var0) {
      float var1 = clamp01(var0);
      if (var1 < 0.5F) {
         return 4.0F * var1 * var1 * var1;
      } else {
         float var2 = 2.0F * var1 - 2.0F;
         return 1.0F + var2 * var2 * var2 / 2.0F;
      }
   }

   public static float easeInQuart(float var0) {
      float var1 = clamp01(var0);
      return var1 * var1 * var1 * var1;
   }

   public static float easeOutQuart(float var0) {
      float var1 = clamp01(var0);
      float var2 = 1.0F - var1;
      return 1.0F - var2 * var2 * var2 * var2;
   }

   public static float easeInOutQuart(float var0) {
      float var1 = clamp01(var0);
      if (var1 < 0.5F) {
         return 8.0F * var1 * var1 * var1 * var1;
      } else {
         float var2 = 1.0F - var1;
         return 1.0F - 8.0F * var2 * var2 * var2 * var2;
      }
   }

   public static float easeInQuint(float var0) {
      float var1 = clamp01(var0);
      return var1 * var1 * var1 * var1 * var1;
   }

   public static float easeOutQuint(float var0) {
      float var1 = clamp01(var0);
      float var2 = 1.0F - var1;
      return 1.0F - var2 * var2 * var2 * var2 * var2;
   }

   public static float easeInOutQuint(float var0) {
      float var1 = clamp01(var0);
      if (var1 < 0.5F) {
         return 16.0F * var1 * var1 * var1 * var1 * var1;
      } else {
         float var2 = 2.0F * var1 - 2.0F;
         return 1.0F + var2 * var2 * var2 * var2 * var2 / 2.0F;
      }
   }

   public static float easeInSine(float var0) {
      float var1 = clamp01(var0);
      return 1.0F - (float)Math.cos((double)var1 * Math.PI / (double)2.0F);
   }

   public static float easeOutSine(float var0) {
      float var1 = clamp01(var0);
      return (float)Math.sin((double)var1 * Math.PI / (double)2.0F);
   }

   public static float easeInOutSine(float var0) {
      float var1 = clamp01(var0);
      return (float)(-(Math.cos(Math.PI * (double)var1) - (double)1.0F) / (double)2.0F);
   }

   public static float easeInExpo(float var0) {
      float var1 = clamp01(var0);
      return var1 <= 0.0F ? 0.0F : (float)Math.pow((double)2.0F, (double)10.0F * (double)var1 - (double)10.0F);
   }

   public static float easeOutExpo(float var0) {
      float var1 = clamp01(var0);
      return var1 >= 1.0F ? 1.0F : 1.0F - (float)Math.pow((double)2.0F, (double)-10.0F * (double)var1);
   }

   public static float easeInOutExpo(float var0) {
      float var1 = clamp01(var0);
      if (var1 <= 0.0F) {
         return 0.0F;
      } else if (var1 >= 1.0F) {
         return 1.0F;
      } else {
         return var1 < 0.5F ? (float)(Math.pow((double)2.0F, (double)20.0F * (double)var1 - (double)10.0F) / (double)2.0F) : (float)(((double)2.0F - Math.pow((double)2.0F, (double)-20.0F * (double)var1 + (double)10.0F)) / (double)2.0F);
      }
   }

   public static float easeInCirc(float var0) {
      float var1 = clamp01(var0);
      return 1.0F - (float)Math.sqrt(Math.max((double)0.0F, (double)1.0F - (double)var1 * (double)var1));
   }

   public static float easeOutCirc(float var0) {
      float var1 = clamp01(var0);
      float var2 = var1 - 1.0F;
      return (float)Math.sqrt(Math.max((double)0.0F, (double)1.0F - (double)var2 * (double)var2));
   }

   public static float easeInOutCirc(float var0) {
      float var1 = clamp01(var0);
      return var1 < 0.5F ? (float)(((double)1.0F - Math.sqrt(Math.max((double)0.0F, (double)1.0F - (double)4.0F * (double)var1 * (double)var1))) / (double)2.0F) : (float)((Math.sqrt(Math.max((double)0.0F, (double)1.0F - (double)4.0F * (double)(var1 - 1.0F) * (double)(var1 - 1.0F))) + (double)1.0F) / (double)2.0F);
   }

   public static float easeInBack(float var0) {
      float var1 = clamp01(var0);
      float var2 = 1.70158F;
      return (var2 + 1.0F) * var1 * var1 * var1 - var2 * var1 * var1;
   }

   public static float easeOutBack(float var0) {
      float var1 = clamp01(var0);
      float var2 = 1.70158F;
      float var3 = var1 - 1.0F;
      return 1.0F + (var2 + 1.0F) * var3 * var3 * var3 + var2 * var3 * var3;
   }

   public static float easeInOutBack(float var0) {
      float var1 = clamp01(var0);
      float var2 = 2.5949094F;
      if (var1 < 0.5F) {
         float var4 = 2.0F * var1;
         return var4 * var4 * ((var2 + 1.0F) * var4 - var2) / 2.0F;
      } else {
         float var3 = 2.0F * var1 - 2.0F;
         return (var3 * var3 * ((var2 + 1.0F) * var3 + var2) + 2.0F) / 2.0F;
      }
   }

   public static float easeOutBounce(float var0) {
      float var1 = clamp01(var0);
      if (var1 < 0.36363637F) {
         return 7.5625F * var1 * var1;
      } else if (var1 < 0.72727275F) {
         float var4 = var1 - 0.54545456F;
         return 7.5625F * var4 * var4 + 0.75F;
      } else if (var1 < 0.90909094F) {
         float var3 = var1 - 0.8181818F;
         return 7.5625F * var3 * var3 + 0.9375F;
      } else {
         float var2 = var1 - 0.95454544F;
         return 7.5625F * var2 * var2 + 0.984375F;
      }
   }

   public static float easeInBounce(float var0) {
      return 1.0F - easeOutBounce(1.0F - clamp01(var0));
   }

   public static float easeInOutBounce(float var0) {
      float var1 = clamp01(var0);
      return var1 < 0.5F ? (1.0F - easeOutBounce(1.0F - 2.0F * var1)) / 2.0F : (1.0F + easeOutBounce(2.0F * var1 - 1.0F)) / 2.0F;
   }

   public static float easeInElastic(float var0) {
      float var1 = clamp01(var0);
      if (var1 <= 0.0F) {
         return 0.0F;
      } else {
         return var1 >= 1.0F ? 1.0F : (float)(-Math.pow((double)2.0F, (double)10.0F * (double)var1 - (double)10.0F) * Math.sin(((double)var1 * (double)10.0F - (double)10.75F) * 2.0943951023931953));
      }
   }

   public static float easeOutElastic(float var0) {
      float var1 = clamp01(var0);
      if (var1 <= 0.0F) {
         return 0.0F;
      } else {
         return var1 >= 1.0F ? 1.0F : (float)(Math.pow((double)2.0F, (double)-10.0F * (double)var1) * Math.sin(((double)var1 * (double)10.0F - (double)0.75F) * 2.0943951023931953) + (double)1.0F);
      }
   }

   public static float easeInOutElastic(float var0) {
      float var1 = clamp01(var0);
      if (var1 <= 0.0F) {
         return 0.0F;
      } else if (var1 >= 1.0F) {
         return 1.0F;
      } else {
         return var1 < 0.5F ? (float)(-(Math.pow((double)2.0F, (double)20.0F * (double)var1 - (double)10.0F) * Math.sin(((double)20.0F * (double)var1 - (double)11.125F) * 1.3962634015954636)) / (double)2.0F) : (float)(Math.pow((double)2.0F, (double)-20.0F * (double)var1 + (double)10.0F) * Math.sin(((double)20.0F * (double)var1 - (double)11.125F) * 1.3962634015954636) / (double)2.0F + (double)1.0F);
      }
   }
}
