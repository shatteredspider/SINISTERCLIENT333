package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.UiFont;
import net.minecraft.class_332;

public class Watermark extends Module {
   private final BoolSetting showFps = (BoolSetting)this.reg(new BoolSetting("Show FPS", true));
   private final BoolSetting showEnemies = (BoolSetting)this.reg(new BoolSetting("Show Enemy Count", true));

   public Watermark() {
      super("Watermark", "Minimal corner watermark.", Category.RENDER);
      this.setEnabled(true);
      this.registerBindSettings();
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1755 == null) {
         StringBuilder sb = new StringBuilder("777");
         if (this.showFps.is()) {
            sb.append("  ").append(mc.method_47599()).append(" fps");
         }

         if (this.showEnemies.is()) {
            int n = SevenClient.get().enemies.size();
            if (n > 0) {
               sb.append("  ").append(n).append(" enemy").append(n == 1 ? "" : "s");
            }
         }

         String text = sb.toString();
         int w = UiFont.width(text) + 26;
         int h = 18;
         Render2D.shadow(ctx, 6.0F, 6.0F, (float)w, (float)h, 6.0F, 5, -1442840576);
         Render2D.panel(ctx, 6.0F, 6.0F, (float)w, (float)h, 6.0F, -16250872, -14474461);
         Render2D.roundedRect(ctx, 11.0F, 6.0F + (float)h / 2.0F - 1.5F, 3.0F, 3.0F, 1.5F, -1);
         UiFont.draw(ctx, text, 18.0F, 6.0F + (float)(h - UiFont.height()) / 2.0F + 0.5F, -328966);
      }

   }
}
