package dev.sevenclient.ui;

import net.minecraft.class_332;

public final class Render2D {
   private static final int MAX_BANDS = 72;

   private Render2D() {
   }

   public static void rect(class_332 ctx, float x, float y, float w, float h, int color) {
      ctx.method_25294((int)x, (int)y, (int)(x + w), (int)(y + h), color);
   }

   public static void roundedRect(class_332 ctx, float x, float y, float w, float h, float radius, int color) {
      solid(ctx, Math.round(x), Math.round(y), Math.round(w), Math.round(h), radius, color, true);
   }

   private static void solid(class_332 ctx, int ix, int iy, int iw, int ih, float radius, int color, boolean aa) {
      if (iw > 0 && ih > 0) {
         int r = (int)Math.min(radius, (float)Math.min(iw, ih) / 2.0F);
         if (r <= 0) {
            ctx.method_25294(ix, iy, ix + iw, iy + ih, color);
         } else {
            ctx.method_25294(ix, iy + r, ix + iw, iy + ih - r, color);

            for(int row = 0; row < r; ++row) {
               cornerRow(ctx, ix, iy, iw, ih, r, row, color, aa);
            }
         }
      }

   }

   private static void cornerRow(class_332 ctx, int ix, int iy, int iw, int ih, int r, int row, int color, boolean aa) {
      double dy = (double)(r - row) - (double)0.5F;
      double half = Math.sqrt(Math.max((double)0.0F, (double)(r * r) - dy * dy));
      double insetF = (double)r - half;
      int inset = (int)Math.floor(insetF);
      int topY = iy + row;
      int botY = iy + ih - 1 - row;
      ctx.method_25294(ix + inset + 1, topY, ix + iw - inset - 1, topY + 1, color);
      ctx.method_25294(ix + inset + 1, botY, ix + iw - inset - 1, botY + 1, color);
      if (aa) {
         int edge = alpha(color, 1.0F - (float)(insetF - (double)inset));
         ctx.method_25294(ix + inset, topY, ix + inset + 1, topY + 1, edge);
         ctx.method_25294(ix + iw - inset - 1, topY, ix + iw - inset, topY + 1, edge);
         ctx.method_25294(ix + inset, botY, ix + inset + 1, botY + 1, edge);
         ctx.method_25294(ix + iw - inset - 1, botY, ix + iw - inset, botY + 1, edge);
      }

   }

   public static void roundedGradient(class_332 ctx, float x, float y, float w, float h, float radius, int top, int bottom) {
      int ix = Math.round(x);
      int iy = Math.round(y);
      int iw = Math.round(w);
      int ih = Math.round(h);
      if (iw > 0 && ih > 0) {
         if (top == bottom) {
            solid(ctx, ix, iy, iw, ih, radius, top, true);
         } else {
            int r = (int)Math.min(radius, (float)Math.min(iw, ih) / 2.0F);
            int midTop = iy + r;
            int midBottom = iy + ih - r;
            if (midBottom > midTop) {
               ctx.method_25296(ix, midTop, ix + iw, midBottom, Theme.mix(top, bottom, (float)r / Math.max(1.0F, (float)ih - 1.0F)), Theme.mix(top, bottom, (float)(ih - r) / Math.max(1.0F, (float)ih - 1.0F)));
            }

            for(int row = 0; row < r; ++row) {
               float tTop = (float)row / Math.max(1.0F, (float)ih - 1.0F);
               float tBot = (float)(ih - 1 - row) / Math.max(1.0F, (float)ih - 1.0F);
               cornerRowSplit(ctx, ix, iy, iw, ih, r, row, Theme.mix(top, bottom, tTop), Theme.mix(top, bottom, tBot));
            }

         }
      }
   }

   private static void cornerRowSplit(class_332 ctx, int ix, int iy, int iw, int ih, int r, int row, int topCol, int botCol) {
      double dy = (double)(r - row) - (double)0.5F;
      double half = Math.sqrt(Math.max((double)0.0F, (double)(r * r) - dy * dy));
      double insetF = (double)r - half;
      int inset = (int)Math.floor(insetF);
      int topY = iy + row;
      int botY = iy + ih - 1 - row;
      ctx.method_25294(ix + inset + 1, topY, ix + iw - inset - 1, topY + 1, topCol);
      ctx.method_25294(ix + inset + 1, botY, ix + iw - inset - 1, botY + 1, botCol);
      float edge = 1.0F - (float)(insetF - (double)inset);
      int eT = alpha(topCol, edge);
      int eB = alpha(botCol, edge);
      ctx.method_25294(ix + inset, topY, ix + inset + 1, topY + 1, eT);
      ctx.method_25294(ix + iw - inset - 1, topY, ix + iw - inset, topY + 1, eT);
      ctx.method_25294(ix + inset, botY, ix + inset + 1, botY + 1, eB);
      ctx.method_25294(ix + iw - inset - 1, botY, ix + iw - inset, botY + 1, eB);
   }

   private static int bandCount(int top, int bottom, int height) {
      int d = Math.max(Math.max(Math.abs((top >>> 24 & 255) - (bottom >>> 24 & 255)), Math.abs((top >> 16 & 255) - (bottom >> 16 & 255))), Math.max(Math.abs((top >> 8 & 255) - (bottom >> 8 & 255)), Math.abs((top & 255) - (bottom & 255))));
      return Math.max(1, Math.min(height, Math.max(72, d)));
   }

   public static void roundedBorder(class_332 ctx, float x, float y, float w, float h, float radius, int color) {
      int ix = Math.round(x);
      int iy = Math.round(y);
      int iw = Math.round(w);
      int ih = Math.round(h);
      if (iw > 0 && ih > 0) {
         int r = (int)Math.min(radius, (float)Math.min(iw, ih) / 2.0F);
         ctx.method_25294(ix + r, iy, ix + iw - r, iy + 1, color);
         ctx.method_25294(ix + r, iy + ih - 1, ix + iw - r, iy + ih, color);
         ctx.method_25294(ix, iy + r, ix + 1, iy + ih - r, color);
         ctx.method_25294(ix + iw - 1, iy + r, ix + iw, iy + ih - r, color);

         for(int row = 0; row < r; ++row) {
            double dy = (double)(r - row) - (double)0.5F;
            double insetF = (double)r - Math.sqrt(Math.max((double)0.0F, (double)(r * r) - dy * dy));
            int inset = (int)Math.round(insetF);
            int topY = iy + row;
            int botY = iy + ih - 1 - row;
            ctx.method_25294(ix + inset, topY, ix + inset + 1, topY + 1, color);
            ctx.method_25294(ix + iw - inset - 1, topY, ix + iw - inset, topY + 1, color);
            ctx.method_25294(ix + inset, botY, ix + inset + 1, botY + 1, color);
            ctx.method_25294(ix + iw - inset - 1, botY, ix + iw - inset, botY + 1, color);
         }
      }

   }

   public static void panel(class_332 ctx, float x, float y, float w, float h, float radius, int fill, int border) {
      roundedRect(ctx, x, y, w, h, radius, fill);
      roundedBorder(ctx, x, y, w, h, radius, border);
   }

   public static void panelGradient(class_332 ctx, float x, float y, float w, float h, float radius, int top, int bottom, int border) {
      roundedGradient(ctx, x, y, w, h, radius, top, bottom);
      roundedBorder(ctx, x, y, w, h, radius, border);
   }

   public static void innerTopLight(class_332 ctx, float x, float y, float w, float radius, int color) {
      int r = (int)Math.max(1.0F, radius);
      ctx.method_25294((int)x + r, (int)y + 1, (int)(x + w) - r, (int)y + 2, color);
   }

   public static void shadow(class_332 ctx, float x, float y, float w, float h, float radius, int spread, int color) {
      for(int i = spread; i >= 1; --i) {
         float t = 1.0F - (float)i / (float)spread;
         int a = (int)((float)(color >>> 24 & 255) * t * t * 0.5F);
         if (a > 2) {
            solid(ctx, Math.round(x - (float)i), Math.round(y - (float)i + 1.0F), Math.round(w + (float)(i * 2)), Math.round(h + (float)(i * 2)), radius + (float)i, Theme.withAlpha(color, a), false);
         }
      }

   }

   public static void softGlow(class_332 ctx, float cx, float cy, float rx, float ry, int color, int steps) {
      int baseA = color >>> 24 & 255;
      if (baseA > 1 && !(rx < 1.0F) && !(ry < 1.0F)) {
         int n = Math.max(6, steps);
         float bandH = ry * 2.0F / (float)n;

         for(int i = 0; i < n; ++i) {
            float dy = ((float)i + 0.5F) / (float)n * 2.0F - 1.0F;
            float k = 1.0F - dy * dy;
            if (!(k <= 0.0F)) {
               float halfW = (float)Math.sqrt((double)k) * rx;
               float falloff = k * k;
               int a = (int)((float)baseA * falloff);
               if (a >= 2 && !(halfW < 0.5F)) {
                  float yy = cy + dy * ry;
                  ctx.method_25294((int)(cx - halfW), (int)yy, (int)(cx + halfW), (int)(yy + bandH + 1.0F), Theme.withAlpha(color, a));
               }
            }
         }

      }
   }

   public static void dot(class_332 ctx, float x, float y, float size, int color, int a) {
      if (a >= 3) {
         int cx = (int)x;
         int cy = (int)y;
         if (size <= 1.05F) {
            ctx.method_25294(cx, cy, cx + 1, cy + 1, Theme.withAlpha(color, a));
         } else {
            int s = (int)Math.max(1.0F, size);
            ctx.method_25294(cx - 1, cy - 1, cx + s + 1, cy + s + 1, Theme.withAlpha(color, (int)((float)a * 0.22F)));
            ctx.method_25294(cx, cy, cx + s, cy + s, Theme.withAlpha(color, a));
         }
      }
   }

   public static void verticalGradient(class_332 ctx, float x, float y, float w, float h, int top, int bottom) {
      ctx.method_25296(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), top, bottom);
   }

   public static void line(class_332 ctx, int x0, int y0, int x1, int y1, int color) {
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx - dy;
      int guard = 0;

      while(guard++ < 512) {
         ctx.method_25294(x0, y0, x0 + 1, y0 + 1, color);
         if (x0 == x1 && y0 == y1) {
            break;
         }

         int e2 = err << 1;
         if (e2 > -dy) {
            err -= dy;
            x0 += sx;
         }

         if (e2 < dx) {
            err += dx;
            y0 += sy;
         }
      }

   }

   public static void ring(class_332 ctx, float cx, float cy, float radius, int color) {
      int steps = Math.max(10, (int)(radius * 7.0F));

      for(int i = 0; i < steps; ++i) {
         double a = (Math.PI * 2D) * (double)i / (double)steps;
         int px = (int)Math.round((double)cx + Math.cos(a) * (double)radius);
         int py = (int)Math.round((double)cy + Math.sin(a) * (double)radius);
         ctx.method_25294(px, py, px + 1, py + 1, color);
      }

   }

   public static void disc(class_332 ctx, float cx, float cy, float radius, int color) {
      roundedRect(ctx, cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, color);
   }

   public static void discGradient(class_332 ctx, float cx, float cy, float radius, int top, int bottom) {
      roundedGradient(ctx, cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, top, bottom);
   }

   public static void chevron(class_332 ctx, float cx, float cy, float size, float open, int color) {
      float dir = 1.0F - open * 2.0F;

      for(int t = 0; t < 2; ++t) {
         line(ctx, (int)(cx - size), (int)(cy - size * 0.45F * dir) + t, (int)cx, (int)(cy + size * 0.45F * dir) + t, color);
         line(ctx, (int)cx, (int)(cy + size * 0.45F * dir) + t, (int)(cx + size), (int)(cy - size * 0.45F * dir) + t, color);
      }

   }

   public static void magnifier(class_332 ctx, float cx, float cy, int color) {
      ring(ctx, cx, cy - 0.5F, 3.1F, color);
      line(ctx, (int)cx + 2, (int)cy + 2, (int)cx + 4, (int)cy + 4, color);
   }

   private static int alpha(int color, float a) {
      int out = (int)((float)(color >>> 24 & 255) * Math.max(0.0F, Math.min(1.0F, a)));
      return out << 24 | color & 16777215;
   }

   public static boolean hovered(double mx, double my, float x, float y, float w, float h) {
      return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
   }
}
