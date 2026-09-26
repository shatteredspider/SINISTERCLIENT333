package dev.sevenclient.util;

import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.UiFont;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class Notifications {
   public static volatile boolean enabled = true;
   public static volatile boolean playSound = true;
   public static volatile float soundVolume = 1.0F;
   public static volatile int lifetimeMs = 2200;
   public static volatile int maxVisible = 5;
   private static final Deque<Toast> toasts = new ArrayDeque();

   private Notifications() {
   }

   public static void push(String text, Kind kind) {
      if (enabled) {
         synchronized(toasts) {
            toasts.addLast(new Toast(text, kind, System.currentTimeMillis()));

            while(toasts.size() > maxVisible) {
               toasts.removeFirst();
            }
         }

         if (playSound) {
            sound(kind);
         }
      }

   }

   public static void moduleToggled(String name, boolean on) {
      push(name, on ? Notifications.Kind.ON : Notifications.Kind.OFF);
   }

   public static void info(String text) {
      push(text, Notifications.Kind.INFO);
   }

   public static void warn(String text) {
      push(text, Notifications.Kind.WARN);
   }

   private static void sound(Kind kind) {
      try {
         class_310 mc = class_310.method_1551();
         if (mc.method_1483() == null) {
            return;
         }

         float var10000;
         switch (kind.ordinal()) {
            case 0:
               var10000 = 1.45F;
               break;
            case 1:
               var10000 = 0.85F;
               break;
            case 2:
            default:
               var10000 = 1.15F;
               break;
            case 3:
               var10000 = 0.6F;
         }

         float pitch = var10000;
         mc.method_1483().method_4873(class_1109.method_4757((class_3414)class_3417.field_15015.comp_349(), soundVolume, pitch));
      } catch (Throwable var3) {
      }

   }

   public static void clear() {
      synchronized(toasts) {
         toasts.clear();
      }
   }

   public static void render(class_332 ctx, float dt, int screenW, float topY) {
      if (enabled) {
         long now = System.currentTimeMillis();
         synchronized(toasts) {
            toasts.removeIf((tx) -> now - tx.born > (long)lifetimeMs);
            if (!toasts.isEmpty()) {
               float y = topY;

               for(Toast t : toasts) {
                  long age = now - t.born;
                  float target = 1.0F;
                  if (age > (long)lifetimeMs - 400L) {
                     target = 0.0F;
                  }

                  t.anim += (target - t.anim) * (1.0F - (float)Math.exp((double)-11.0F * (double)dt));
                  if (!(t.anim < 0.01F)) {
                     String var10000;
                     switch (t.kind.ordinal()) {
                        case 0:
                           var10000 = "+ " + t.text;
                           break;
                        case 1:
                           var10000 = "- " + t.text;
                           break;
                        case 2:
                        default:
                           var10000 = t.text;
                           break;
                        case 3:
                           var10000 = "! " + t.text;
                     }

                     String label = var10000;
                     int tw = UiFont.width(label);
                     float w = (float)tw + 22.0F;
                     float h = (float)UiFont.height() + 12.0F;
                     float x = (float)screenW - 8.0F - w * t.anim;
                     Render2D.shadow(ctx, x, y, w, h, 5.0F, 5, -1442840576);
                     Render2D.panel(ctx, x, y, w, h, 5.0F, -16250872, -14474461);
                     int var21;
                     switch (t.kind.ordinal()) {
                        case 0:
                        case 3:
                           var21 = -328966;
                           break;
                        case 1:
                           var21 = -6381922;
                           break;
                        case 2:
                        default:
                           var21 = -328966;
                     }

                     int col = var21;
                     Render2D.roundedRect(ctx, x + 6.0F, y + h / 2.0F - 1.5F, 3.0F, 3.0F, 1.5F, t.kind == Notifications.Kind.OFF ? -6381922 : -1);
                     UiFont.draw(ctx, label, x + 14.0F, y + (h - (float)UiFont.height()) / 2.0F + 0.5F, col);
                     y += h + 4.0F;
                  }
               }
            }
         }
      }

   }

   public static enum Kind {
      ON,
      OFF,
      INFO,
      WARN;

      // $FF: synthetic method
      private static Kind[] $values() {
         return new Kind[]{ON, OFF, INFO, WARN};
      }
   }

   private static final class Toast {
      final String text;
      final Kind kind;
      final long born;
      float anim;
      float shift;

      Toast(String text, Kind kind, long born) {
         this.text = text;
         this.kind = kind;
         this.born = born;
      }
   }
}
