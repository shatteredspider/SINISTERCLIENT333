package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.UiFont;
import dev.sevenclient.util.ActionBudget;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.Human;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.Rotations;
import dev.sevenclient.util.SlotGuard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;

public class Debug extends Module {
   private final BoolSetting toggleMessages = (BoolSetting)this.reg(new BoolSetting("Toggle Messages", true));
   private final BoolSetting showWhenGuiOpen = (BoolSetting)this.reg(new BoolSetting("Show Over GUI", true));
   private final BoolSetting behaviourLines = (BoolSetting)this.reg(new BoolSetting("Behaviour Lines", true));

   public Debug() {
      super("Debug", "Live diagnostic readout. Turn this off once things work.", Category.RENDER);
      this.setEnabled(true);
      this.registerBindSettings();
   }

   public void onTick() {
      Module.notifyToggles = this.toggleMessages.is();
   }

   public void onDisable() {
      Module.notifyToggles = true;
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1755 == null || this.showWhenGuiOpen.is()) {
         int active = 0;

         for(Module m : SevenClient.get().modules.all()) {
            if (m.isEnabled()) {
               ++active;
            }
         }

         List<String> lines = new ArrayList();
         lines.add("777 DIAGNOSTICS");
         String var10001 = UiFont.usingCustomFont() ? "custom TTF" : "vanilla (no TTF found)";
         lines.add("font " + var10001);
         long var18 = Diagnostics.ticks;
         lines.add("ticks " + var18 + (Diagnostics.ticks > 0L ? " ok" : " TICK HOOK DEAD"));
         var18 = Diagnostics.frames;
         lines.add("frames " + var18 + " " + String.format("%.0f/s", Diagnostics.fps) + (Diagnostics.frames > 0L ? "" : " FRAME HOOK DEAD"));
         lines.add("frame src " + (Diagnostics.frameSourceIsMixin ? "Mouse mixin" : "render fallback"));
         String var20 = String.format("%.2f ms", Diagnostics.lastDt * 1000.0F);
         lines.add("dt " + var20 + String.format(" (~%.0f fps)", Diagnostics.lastDt > 0.0F ? 1.0F / Diagnostics.lastDt : 0.0F));
         var20 = HumanDiag.gcdSyncStatus();
         lines.add("gcd sync " + var20);
         var20 = String.format("%.4f", safeGcd());
         lines.add("gcd step " + var20);
         long var23 = Diagnostics.hitFlickArmed;
         lines.add("hitflick armed " + var23 + "x");
         var23 = Diagnostics.attackEvents;
         lines.add("attacks " + var23 + (Diagnostics.attackEvents > 0L ? "" : " (hit something to test)"));
         String var25 = Diagnostics.aimTarget;
         lines.add("aim tgt " + var25);
         var25 = String.format("%.2f deg", Diagnostics.aimError);
         lines.add("aim err " + var25);
         var25 = String.format("%.2f", Diagnostics.aimAuthority);
         lines.add("authority " + var25 + (Diagnostics.aimAuthority < 0.02 ? " (calm / in hitbox)" : ""));
         var25 = String.format("%.2f", Diagnostics.aimCoverage);
         lines.add("coverage " + var25 + (Diagnostics.aimCoverage > 0.9 ? " (you've got it)" : ""));
         var25 = String.format("%.2f", Diagnostics.aimFlick);
         lines.add("flick " + var25 + String.format(" urgency x%.2f", Diagnostics.aimUrgency));
         var25 = String.format("%.2f", Diagnostics.aimSpin);
         lines.add("spin " + var25 + (Diagnostics.aimSpin > 0.3 ? " SPIN" : ""));
         var25 = latchName(HumanDiag.turnLatch);
         lines.add("turn latch " + var25 + String.format("  unwrapped %.1f deg", HumanDiag.unwrappedErr) + String.format("  far %.2f", HumanDiag.farness));
         Object[] var10002 = new Object[]{Diagnostics.lastYawStep};
         lines.add("yaw step " + String.format("%.4f", var10002));
         lines.add("rot/tick " + HumanDiag.rotStatus());
         lines.add("slot pkts " + SlotGuard.status());
         lines.add("action pkts " + ActionBudget.status());
         if (this.behaviourLines.is()) {
            lines.add("-- behaviour model --");
            lines.add("human " + HumanDiag.humanState());
            var10002 = new Object[]{Human.SIG_SPEED, Human.SIG_VAR};
            lines.add("signature " + String.format("speed x%.2f  var x%.2f", var10002));
            lines.add("hitswap " + HumanDiag.hitSwapState);
            long var32 = HumanDiag.hitSwapSwaps;
            lines.add("hitswap n " + var32 + " swaps / " + HumanDiag.hitSwapFeints + " feints / " + HumanDiag.hitSwapMisses + " missed" + String.format("  last %.0fms", HumanDiag.hitSwapLastDelay));
            lines.add("trigger " + HumanDiag.triggerState);
            lines.add("trigger n " + HumanDiag.triggerStats());
            lines.add("pingspoof " + HumanDiag.pingSpoofState);
            lines.add("jumpreset " + HumanDiag.jumpResets + "  (drives mc.options.jumpKey -- inputs, not packets)");
            lines.add("sprint tap " + HumanDiag.sprintTaps + "  (drives back/sneak keys -- inputs, not packets)");
         }

         lines.add("modules " + active + " enabled");
         var25 = Diagnostics.lastError.equals("none") ? "none" : Diagnostics.lastErrorModule + " -> " + Diagnostics.lastError;
         lines.add("error " + var25);
         int w = 0;

         for(String s : lines) {
            w = Math.max(w, UiFont.width(s));
         }

         int lh = UiFont.height() + 3;
         float boxW = (float)(w + 20);
         float boxH = (float)(lines.size() * lh + 14);
         float x = (float)mc.method_22683().method_4486() - boxW - 6.0F;
         float y = 6.0F;
         Render2D.shadow(ctx, x, y, boxW, boxH, 6.0F, 6, -1442840576);
         Render2D.panel(ctx, x, y, boxW, boxH, 6.0F, -16250872, -14474461);
         float ty = y + 7.0F;

         for(int i = 0; i < lines.size(); ++i) {
            String s = (String)lines.get(i);
            int color = -6381922;
            if (i == 0) {
               color = -328966;
            }

            if (s.startsWith("--")) {
               color = -328966;
            }

            if (s.contains("DEAD")) {
               color = -1;
            }

            if (s.startsWith("error") && !s.endsWith("none")) {
               color = -1;
            }

            UiFont.draw(ctx, s, x + 10.0F, ty, color);
            ty += (float)lh;
         }
      }

   }

   private static String latchName(int v) {
      if (v > 0) {
         return "RIGHT";
      } else {
         return v < 0 ? "LEFT" : "-";
      }
   }

   private static double safeGcd() {
      try {
         return Rotations.gcd();
      } catch (Throwable var1) {
         return (double)-1.0F;
      }
   }
}
