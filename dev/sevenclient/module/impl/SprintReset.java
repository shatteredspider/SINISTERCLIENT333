package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.ActionBudget;
import dev.sevenclient.util.Human;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.KeyPulse;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_304;

public abstract class SprintReset extends Module {
   protected final NumberSetting chance = (NumberSetting)this.reg(new NumberSetting("Chance", (double)100.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   protected final NumberSetting minMs = (NumberSetting)this.reg(new NumberSetting("Min MS", (double)40.0F, (double)0.0F, (double)300.0F, (double)5.0F));
   protected final NumberSetting maxMs = (NumberSetting)this.reg(new NumberSetting("Max MS", (double)110.0F, (double)0.0F, (double)300.0F, (double)5.0F));
   protected final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   protected final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", false));
   protected final BoolSetting requireSprint = (BoolSetting)this.reg(new BoolSetting("Require Sprint", true));
   private KeyPulse pulse;
   private int armedIn = -1;
   private long pendingHold = 0L;

   protected SprintReset(String name, String description) {
      super(name, description, Category.COMBAT);
   }

   protected abstract class_304 binding();

   private KeyPulse pulse() {
      if (this.pulse == null) {
         this.pulse = new KeyPulse(this.binding());
      }

      return this.pulse;
   }

   public void onEnable() {
      this.armedIn = -1;
   }

   public void onDisable() {
      this.pulse().release();
      this.armedIn = -1;
   }

   public void onTick() {
      this.pulse().tick();
      if (this.armedIn >= 0) {
         if (this.armedIn > 0) {
            --this.armedIn;
         } else {
            this.armedIn = -1;
            if (ActionBudget.claim(this.channel(), 90L, System.currentTimeMillis())) {
               this.pulse().press(this.pendingHold);
               Human.act(this.pendingHold);
               ++HumanDiag.sprintTaps;
            }
         }
      }

   }

   private String channel() {
      return "tap:" + this.name();
   }

   public void onAttack(class_1297 target) {
      if (mc.field_1724 != null && (!this.playersOnly.is() || target instanceof class_1657) && (!this.enemiesOnly.is() || SevenClient.get().enemies.is(target)) && (!this.requireSprint.is() || mc.field_1724.method_5624()) && (!this.binding().method_1434() || this.pulse().active()) && this.armedIn < 0 && Human.roll(this.chance.val())) {
         double lo = Math.min(this.minMs.val(), this.maxMs.val());
         double hi = Math.max(this.minMs.val(), this.maxMs.val());
         double span = Math.max((double)1.0F, hi - lo);
         double mu = lo + span * 0.33;
         double v = Human.exGauss(mu, span * 0.16 * Human.SIG_VAR, span * 0.28 * Human.SIG_VAR);
         this.pendingHold = (long)Math.max((double)50.0F, Math.min(hi * (double)2.0F, v));
         this.armedIn = Human.lateness();
         if (this.armedIn == 0) {
            this.armedIn = -1;
            if (!ActionBudget.claim(this.channel(), 90L, System.currentTimeMillis())) {
               return;
            }

            this.pulse().press(this.pendingHold);
            Human.act(this.pendingHold);
            ++HumanDiag.sprintTaps;
         }
      }

   }
}
