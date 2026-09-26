package dev.sevenclient.ui;

import net.minecraft.class_332;

public final class Icons {
   private Icons() {
   }

   public static void combat(class_332 ctx, float x, float y, float size, int color) {
      int x0 = (int)x;
      int y0 = (int)y;
      int s = (int)size;
      Render2D.line(ctx, x0 + 1, y0 + s - 2, x0 + s - 2, y0 + 1, color);
      Render2D.line(ctx, x0 + 2, y0 + s - 2, x0 + s - 1, y0 + 1, color);
      Render2D.line(ctx, x0 + 1, y0 + 1, x0 + s - 2, y0 + s - 2, color);
      Render2D.line(ctx, x0 + 2, y0 + 1, x0 + s - 1, y0 + s - 2, color);
   }

   public static void movement(class_332 ctx, float x, float y, float size, int color) {
      int cx = (int)(x + size / 2.0F);
      int cy = (int)(y + size / 2.0F);
      int h = (int)(size * 0.34F);

      for(int o = 0; o < 2; ++o) {
         int ox = cx - 4 + o * 5;
         Render2D.line(ctx, ox, cy - h, ox + 3, cy, color);
         Render2D.line(ctx, ox + 3, cy, ox, cy + h, color);
      }

   }

   public static void player(class_332 ctx, float x, float y, float size, int color) {
      float cx = x + size / 2.0F;
      Render2D.ring(ctx, cx, y + size * 0.32F, size * 0.19F, color);
      int by = (int)(y + size * 0.62F);
      int bw = (int)(size * 0.32F);
      Render2D.line(ctx, (int)cx - bw, by + 3, (int)cx - bw, by + 1, color);
      Render2D.line(ctx, (int)cx - bw, by + 1, (int)cx + bw, by + 1, color);
      Render2D.line(ctx, (int)cx + bw, by + 1, (int)cx + bw, by + 3, color);
   }

   public static void render(class_332 ctx, float x, float y, float size, int color) {
      float cx = x + size / 2.0F;
      float cy = y + size / 2.0F;
      int half = (int)(size * 0.42F);

      for(int i = -half; i <= half; ++i) {
         double t = (double)i / (double)half;
         int dy = (int)Math.round(Math.cos(t * Math.PI / (double)2.0F) * (double)size * (double)0.26F);
         ctx.method_25294((int)cx + i, (int)cy - dy, (int)cx + i + 1, (int)cy - dy + 1, color);
         ctx.method_25294((int)cx + i, (int)cy + dy, (int)cx + i + 1, (int)cy + dy + 1, color);
      }

      Render2D.disc(ctx, cx, cy, size * 0.13F, color);
   }

   public static void keyboard(class_332 ctx, float x, float y, float size, int color) {
      float w = size * 0.86F;
      float h = size * 0.58F;
      float bx = x + (size - w) / 2.0F;
      float by = y + (size - h) / 2.0F;
      Render2D.roundedBorder(ctx, bx, by, w, h, 2.0F, color);

      for(int i = 0; i < 3; ++i) {
         int px = (int)(bx + 3.0F + (float)(i * 3));
         ctx.method_25294(px, (int)(by + 3.0F), px + 1, (int)(by + 4.0F), color);
      }

      ctx.method_25294((int)(bx + 3.0F), (int)(by + h - 4.0F), (int)(bx + w - 3.0F), (int)(by + h - 3.0F), color);
   }

   public static void sliders(class_332 ctx, float x, float y, float size, int color) {
      for(int i = 0; i < 3; ++i) {
         int ly = (int)(y + size * 0.25F + (float)i * size * 0.25F);
         ctx.method_25294((int)x + 1, ly, (int)(x + size - 1.0F), ly + 1, color);
         int knob = (int)(x + 2.0F + (float)(i * 4 % (int)(size - 5.0F)));
         Render2D.disc(ctx, (float)knob + 1.5F, (float)ly + 0.5F, 1.8F, color);
      }

   }

   public static void mark(class_332 ctx, float x, float y, float size, int color) {
      float cx = x + size / 2.0F;
      float cy = y + size / 2.0F;
      int half = (int)(size * 0.34F);

      for(int i = -half; i <= half; ++i) {
         int span = half - Math.abs(i);
         ctx.method_25294((int)(cx - (float)span), (int)cy + i, (int)(cx + (float)span + 1.0F), (int)cy + i + 1, color);
      }

   }
}
