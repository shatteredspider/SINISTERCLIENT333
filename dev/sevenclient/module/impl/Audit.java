package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.UiFont;
import dev.sevenclient.util.PacketAudit;
import dev.sevenclient.util.SprintGuard;
import java.util.List;
import net.minecraft.class_332;

public class Audit extends Module {
   private final BoolSetting showOverGui = (BoolSetting)this.reg(new BoolSetting("Show Over GUI", true));
   private final BoolSetting showGuard = (BoolSetting)this.reg(new BoolSetting("Guard Lines", true));
   private final BoolSetting leftSide = (BoolSetting)this.reg(new BoolSetting("Left Side", true));

   public Audit() {
      super("Audit", "Runs the anticheats' checks against your own packets, live.", Category.RENDER);
      this.registerBindSettings();
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1755 == null || this.showOverGui.is()) {
         List<String> lines = PacketAudit.lines();
         if (this.showGuard.is()) {
            lines.add("-- guards --");
            lines.add("sprint guard  " + SprintGuard.status());
         }

         int w = 0;

         for(String s : lines) {
            w = Math.max(w, UiFont.width(s));
         }

         int lh = UiFont.height() + 3;
         float boxW = (float)(w + 20);
         float boxH = (float)(lines.size() * lh + 14);
         float x = this.leftSide.is() ? 6.0F : (float)mc.method_22683().method_4486() - boxW - 6.0F;
         float y = (float)mc.method_22683().method_4502() - boxH - 6.0F;
         Render2D.shadow(ctx, x, y, boxW, boxH, 6.0F, 6, -1442840576);
         Render2D.panel(ctx, x, y, boxW, boxH, 6.0F, -16250872, -14474461);
         float ty = y + 7.0F;

         for(int i = 0; i < lines.size(); ++i) {
            String s = (String)lines.get(i);
            int color = -6381922;
            if (i == 0 || s.startsWith("--")) {
               color = -328966;
            }

            if (s.contains("<--") || s.contains("MISMATCH")) {
               color = -1;
            }

            if (s.contains("clean") || s.contains("MATCH") && !s.contains("MISMATCH")) {
               color = -8336444;
            }

            UiFont.draw(ctx, s, x + 10.0F, ty, color);
            ty += (float)lh;
         }

      }
   }
}
