package dev.sevenclient.ui;

import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import org.joml.Matrix3x2fStack;

public final class UiFont {
   public static final class_2960 FONT = safeId("sevenclient", "ui");
   public static final class_2960 FONT_BOLD = safeId("sevenclient", "ui_bold");
   private static final class_2960 FILE_MEDIUM = safeId("sevenclient", "font/inter_medium.ttf");
   private static final class_2960 FILE_SEMIBOLD = safeId("sevenclient", "font/inter_semibold.ttf");
   public static boolean useCustomFont = true;
   private static boolean found = false;
   private static long lastCheck = 0L;
   public static final float SS = 2.0F;

   private UiFont() {
   }

   private static class_2960 safeId(String namespace, String path) {
      try {
         return class_2960.method_60655(namespace, path);
      } catch (Throwable var3) {
         return null;
      }
   }

   public static void invalidate() {
      found = false;
      lastCheck = 0L;
   }

   private static boolean fontAvailable() {
      if (!useCustomFont) {
         return false;
      } else if (FONT != null && FONT_BOLD != null && FILE_MEDIUM != null && FILE_SEMIBOLD != null) {
         if (found) {
            return true;
         } else {
            long now = System.currentTimeMillis();
            if (now - lastCheck < 5000L) {
               return false;
            } else {
               lastCheck = now;
               class_310 mc = class_310.method_1551();
               if (mc != null && mc.method_1478() != null) {
                  try {
                     found = mc.method_1478().method_14486(FILE_MEDIUM).isPresent() && mc.method_1478().method_14486(FILE_SEMIBOLD).isPresent();
                  } catch (Throwable var4) {
                     found = false;
                  }

                  return found;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean usingCustomFont() {
      return fontAvailable();
   }

   public static class_2561 styled(String s, boolean bold) {
      class_5250 t = class_2561.method_43470(s);
      return !fontAvailable() ? t : t.method_10862(class_2583.field_24360.method_27704(new class_11719.class_11721(bold ? FONT_BOLD : FONT)));
   }

   public static class_2561 styled(String s) {
      return styled(s, false);
   }

   public static void draw(class_332 ctx, String s, float x, float y, int color) {
      drawWeighted(ctx, s, x, y, color, false);
   }

   public static void drawBold(class_332 ctx, String s, float x, float y, int color) {
      drawWeighted(ctx, s, x, y, color, true);
   }

   private static void drawWeighted(class_332 ctx, String s, float x, float y, int color, boolean bold) {
      class_310 mc = class_310.method_1551();
      Matrix3x2fStack m = ctx.method_51448();
      m.pushMatrix();
      m.scale(0.5F, 0.5F);
      ctx.method_51439(mc.field_1772, styled(s, bold), Math.round(x * 2.0F), Math.round(y * 2.0F), color, false);
      m.popMatrix();
   }

   public static void drawCentered(class_332 ctx, String s, float cx, float y, int color) {
      draw(ctx, s, cx - (float)width(s) / 2.0F, y, color);
   }

   public static void drawRight(class_332 ctx, String s, float rx, float y, int color) {
      draw(ctx, s, rx - (float)width(s), y, color);
   }

   public static void drawRightBold(class_332 ctx, String s, float rx, float y, int color) {
      drawBold(ctx, s, rx - (float)widthBold(s), y, color);
   }

   public static int width(String s) {
      return Math.round((float)class_310.method_1551().field_1772.method_27525(styled(s, false)) / 2.0F);
   }

   public static int widthBold(String s) {
      return Math.round((float)class_310.method_1551().field_1772.method_27525(styled(s, true)) / 2.0F);
   }

   public static int height() {
      return 9;
   }

   public static String trim(String s, int maxWidth) {
      if (width(s) <= maxWidth) {
         return s;
      } else {
         StringBuilder sb = new StringBuilder();

         for(char c : s.toCharArray()) {
            String var10000 = sb.toString();
            if (width(var10000 + c + "...") > maxWidth) {
               break;
            }

            sb.append(c);
         }

         return String.valueOf(sb) + "...";
      }
   }
}
