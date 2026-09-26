package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.mixin.KeyBindingAccessor;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.util.ActionBudget;
import dev.sevenclient.util.Human;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.SlotUtil;
import dev.sevenclient.util.SprintGuard;
import dev.sevenclient.util.TargetPriority;
import dev.sevenclient.util.TargetUtil;
import java.lang.reflect.Field;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_746;

public class Triggerbot extends Module {
   private final ModeSetting mode = (ModeSetting)this.reg(new ModeSetting("Mode", "Hold Key", new String[]{"Hold Key", "Always"}));
   private final NumberSetting range = (NumberSetting)this.reg(new NumberSetting("Max Range", (double)3.5F, (double)1.0F, (double)6.0F, 0.1));
   private final BoolSetting playersOnly = (BoolSetting)this.reg(new BoolSetting("Players Only", true));
   private final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   private final BoolSetting weaponsOnly = (BoolSetting)this.reg(new BoolSetting("Limit To Weapons", true));
   private final ModeSetting delayMode = (ModeSetting)this.reg(new ModeSetting("Delay", "Dynamic", new String[]{"Dynamic", "Manual"}));
   private final NumberSetting reactMin = (NumberSetting)this.reg(new NumberSetting("Reaction Min MS", (double)90.0F, (double)0.0F, (double)400.0F, (double)5.0F));
   private final NumberSetting reactMax = (NumberSetting)this.reg(new NumberSetting("Reaction Max MS", (double)190.0F, (double)0.0F, (double)600.0F, (double)5.0F));
   private final NumberSetting reflexes = (NumberSetting)this.reg(new NumberSetting("Reflexes", (double)100.0F, (double)40.0F, (double)200.0F, (double)5.0F));
   private final NumberSetting earlyHit = (NumberSetting)this.reg(new NumberSetting("Early Hit Chance", (double)18.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final NumberSetting missChance = (NumberSetting)this.reg(new NumberSetting("Miss Chance", (double)3.0F, (double)0.0F, (double)50.0F, (double)1.0F));
   private final ModeSetting critMode = (ModeSetting)this.reg(new ModeSetting("Crits", "Prefer", new String[]{"Off", "Prefer", "P-Crit Only"}));
   private final NumberSetting minCharge = (NumberSetting)this.reg(new NumberSetting("Min Charge %", (double)90.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final BoolSetting breakSprint = (BoolSetting)this.reg(new BoolSetting("Break Sprint For Crit", true));
   private final BoolSetting yieldToManual = (BoolSetting)this.reg(new BoolSetting("Yield To Manual Clicks", true));
   private final ModeSetting airborneCrits = (ModeSetting)this.reg(new ModeSetting("Airborne Crits", "Wait", new String[]{"Off", "Wait", "Strict"}));
   private final NumberSetting maxCritWait = (NumberSetting)this.reg(new NumberSetting("Max Crit Wait MS", (double)420.0F, (double)60.0F, (double)900.0F, (double)20.0F));
   private final BoolSetting skipShielded = (BoolSetting)this.reg(new BoolSetting("Skip Shielded", false));
   private final NumberSetting whiffChance = (NumberSetting)this.reg(new NumberSetting("Whiff Chance", (double)0.0F, (double)0.0F, (double)40.0F, (double)1.0F));
   private int armedOn = -1;
   private long fireAt = Long.MAX_VALUE;
   private long opportunityStart = 0L;
   private boolean willTake = false;
   private boolean earlyThisOne = false;
   private long pcritUntil = 0L;
   private int lastHurtTime = 0;
   private long airborneSince = 0L;
   private boolean whiffPending = false;
   private static Field boundKeyFallback;
   private static boolean fallbackResolved = false;
   private static boolean accessorWorks = true;
   private static final double GRAVITY = 0.08;
   private static final float CRIT_CHARGE = 0.848F;

   public Triggerbot() {
      super("Triggerbot", "Clicks on crosshair targets, and schedules crits.", Category.COMBAT);

      for(Setting<?> s : new Setting[]{this.reactMin, this.reactMax}) {
         s.visibleWhen(() -> this.delayMode.is("Manual"));
      }

      this.reflexes.visibleWhen(() -> this.delayMode.is("Dynamic"));
      this.breakSprint.visibleWhen(() -> !this.critMode.is("Off"));
      this.maxCritWait.visibleWhen(() -> this.airborneCrits.is("Wait"));
      this.registerBindSettings();
   }

   public void onDisable() {
      this.disarm();
      this.pcritUntil = 0L;
   }

   private void disarm() {
      this.whiffPending = false;
      this.armedOn = -1;
      this.fireAt = Long.MAX_VALUE;
      this.willTake = false;
      this.earlyThisOne = false;
   }

   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         long now = System.currentTimeMillis();
         this.trackKnockback(now);
         if (mc.field_1724.method_24828()) {
            this.airborneSince = 0L;
         } else if (this.airborneSince == 0L) {
            this.airborneSince = now;
         }

         if (!this.active()) {
            this.disarm();
            HumanDiag.triggerState = "inactive";
         } else if (this.yieldToManual.is() && mc.field_1690.field_1886.method_1434()) {
            this.disarm();
            HumanDiag.triggerState = "yielding, you are clicking";
         } else if (this.weaponsOnly.is() && !isWeapon(mc.field_1724.method_6047())) {
            this.disarm();
            HumanDiag.triggerState = "held item is not a weapon";
         } else {
            class_1297 hit = TargetUtil.crosshairEntity();
            if (!TargetPriority.isAllowed(hit, this.range.val(), this.enemiesOnly.is())) {
               this.disarm();
               HumanDiag.triggerState = "crosshair is not on priority target";
            } else if (this.valid(hit) && !((double)mc.field_1724.method_5739(hit) > this.range.val())) {
               if (hit.method_5628() != this.armedOn) {
                  this.armedOn = hit.method_5628();
                  this.opportunityStart = now;
                  long d = this.delayMode.is("Manual") ? this.manualDelay() : this.dynamicDelay(hit);
                  this.fireAt = now + d + (long)Human.refractoryMs();
                  this.willTake = Human.roll((double)100.0F - this.missChance.val());
                  this.earlyThisOne = Human.flat(this.earlyHit.val());
               }

               float charge = mc.field_1724.method_7261(0.0F);
               if (!this.critMode.is("Off")) {
                  boolean pcrit = now < this.pcritUntil;
                  boolean wantWindow = !this.critMode.is("P-Crit Only") || pcrit;
                  if (wantWindow) {
                     if (this.breakSprint.is() && mc.field_1724.method_5624() && this.critReady(charge) && SprintGuard.hold(now)) {
                        mc.field_1724.method_5728(false);
                        ++HumanDiag.sprintBroken;
                     }

                     if (this.critNow() && ActionBudget.claim("attack", 60L, now)) {
                        if (this.click()) {
                           ++HumanDiag.triggerClicks;
                           ++HumanDiag.triggerCrits;
                           if (pcrit) {
                              ++HumanDiag.triggerPCrits;
                              this.pcritUntil = 0L;
                           }

                           Human.act(45L);
                           HumanDiag.triggerState = "CRIT (instant)";
                           this.armedOn = -1;
                        }

                        return;
                     }
                  }

                  if (!this.airborneCrits.is("Off") && this.critPending(now)) {
                     HumanDiag.triggerState = "airborne, holding for crit (" + this.ticksToApex() + "t to apex)";
                     return;
                  }

                  if (this.critMode.is("P-Crit Only") && !pcrit) {
                     HumanDiag.triggerState = "p-crit only, no window";
                     return;
                  }
               }

               if ((double)charge < this.minCharge.val() / (double)100.0F) {
                  HumanDiag.triggerState = String.format("cooldown %.0f%%", charge * 100.0F);
               } else if (!this.willTake) {
                  HumanDiag.triggerState = "chose not to swing";
               } else {
                  long effectiveFireAt = this.earlyThisOne ? this.fireAt - (long)((double)(this.fireAt - this.opportunityStart) * 0.33) : this.fireAt;
                  if (now < effectiveFireAt) {
                     HumanDiag.triggerState = "reacting (" + (effectiveFireAt - now) + "ms)" + (this.earlyThisOne ? " early" : "");
                  } else if (this.whiffChance.val() > (double)0.0F && !this.whiffPending && Human.flat(this.whiffChance.val())) {
                     this.whiffPending = true;
                     HumanDiag.triggerState = "whiffing";
                  } else if (!ActionBudget.claim("attack", 60L, now)) {
                     HumanDiag.triggerState = "rate limited";
                  } else {
                     if (this.click()) {
                        ++HumanDiag.triggerClicks;
                        Human.act(45L);
                        HumanDiag.triggerState = "CLICK";
                        this.armedOn = -1;
                     }

                  }
               }
            } else {
               if (this.whiffPending) {
                  this.whiffPending = false;
                  if (ActionBudget.claim("attack", 60L, now) && this.click()) {
                     ++HumanDiag.triggerClicks;
                     Human.act(45L);
                     HumanDiag.triggerState = "WHIFF";
                     return;
                  }
               }

               this.disarm();
               HumanDiag.triggerState = "no target on crosshair";
            }
         }
      } else {
         this.disarm();
      }
   }

   private boolean critReady(float charge) {
      class_746 p = mc.field_1724;
      return !p.method_24828() && !p.method_5799() && !p.method_5771() && !p.method_6101() && !p.method_5765() && charge >= 0.848F;
   }

   private void trackKnockback(long now) {
      int hurt = mc.field_1724.field_6235;
      if (hurt > this.lastHurtTime) {
         class_243 v = mc.field_1724.method_18798();
         if (v.field_1351 > 0.08) {
            this.pcritUntil = now + 1600L;
            ++HumanDiag.pcritArmed;
         }
      }

      this.lastHurtTime = hurt;
   }

   private boolean critPending(long now) {
      class_746 p = mc.field_1724;
      if (p != null && !p.method_24828() && !p.method_5799() && !p.method_5771() && !p.method_6101() && !p.method_5765()) {
         if (descending(p)) {
            return false;
         } else if (!p.method_5715() && !p.method_6115()) {
            if (!p.method_6059(class_1294.field_5919) && !p.method_6059(class_1294.field_5906)) {
               if (p.method_7261(0.0F) < 0.848F) {
                  return false;
               } else if (p.method_5624() && !this.breakSprint.is()) {
                  return false;
               } else if (this.airborneCrits.is("Strict")) {
                  return true;
               } else {
                  return this.airborneSince != 0L && now - this.airborneSince <= (long)this.maxCritWait.val();
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean descending(class_746 p) {
      return p.method_23318() - p.field_6036 < (double)0.0F;
   }

   private boolean critNow() {
      class_746 p = mc.field_1724;
      return p != null && descending(p) && p.field_6017 > (double)0.0F && !p.method_24828() && !p.method_6101() && !p.method_5799() && !p.method_5771() && !p.method_5765() && !p.method_5715() && !p.method_6115() && !p.method_6059(class_1294.field_5919) && !p.method_6059(class_1294.field_5906) && (!p.method_5624() || this.breakSprint.is()) && p.method_7261(0.0F) >= 0.848F;
   }

   private int ticksToApex() {
      double vy = mc.field_1724.method_18798().field_1351;
      return vy <= (double)0.0F ? 0 : (int)Math.ceil(vy / 0.08);
   }

   private long manualDelay() {
      double lo = Math.min(this.reactMin.val(), this.reactMax.val());
      double hi = Math.max(lo + (double)1.0F, Math.max(this.reactMin.val(), this.reactMax.val()));
      double span = hi - lo;
      return (long)Math.max((double)0.0F, Human.exGauss(lo + span * (double)0.25F, span * 0.18, span * 0.35));
   }

   private long dynamicDelay(class_1297 target) {
      class_243 rel = target.method_18798().method_1020(mc.field_1724.method_18798());
      double dist = Math.max((double)0.5F, (double)mc.field_1724.method_5739(target));
      double crossing = class_3532.method_15350(rel.method_37267() / 0.35, (double)0.0F, (double)1.0F);
      double close = class_3532.method_15350((this.range.val() - dist) / this.range.val(), (double)0.0F, (double)1.0F);
      double urgency = this.critNow() ? (double)1.0F : (double)0.0F;
      double mu = (double)210.0F - (double)55.0F * crossing - (double)35.0F * close - (double)25.0F * urgency;
      mu *= this.reflexes.val() / (double)100.0F * Human.SIG_SPEED;
      mu = Math.max((double)45.0F, mu);
      return (long)Human.exGauss(mu, mu * 0.16 * Human.SIG_VAR, mu * 0.22 * Human.SIG_VAR);
   }

   private boolean click() {
      class_3675.class_306 key = resolveKey(mc.field_1690.field_1886);
      if (key == null) {
         return false;
      } else {
         class_304.method_1416(key, true);
         class_304.method_1420(key);
         class_304.method_1416(key, false);
         return true;
      }
   }

   private static class_3675.class_306 resolveKey(class_304 bind) {
      if (accessorWorks) {
         try {
            class_3675.class_306 k = ((KeyBindingAccessor)bind).seven$getBoundKey();
            if (k != null) {
               return k;
            }
         } catch (Throwable var7) {
            accessorWorks = false;
         }
      }

      if (!fallbackResolved) {
         fallbackResolved = true;

         for(Field f : class_304.class.getDeclaredFields()) {
            if (class_3675.class_306.class.isAssignableFrom(f.getType()) && (f.getName().equals("boundKey") || f.getName().equals("field_1655"))) {
               f.setAccessible(true);
               boundKeyFallback = f;
               break;
            }
         }
      }

      try {
         if (boundKeyFallback != null) {
            Object var10 = boundKeyFallback.get(bind);
            if (var10 instanceof class_3675.class_306) {
               class_3675.class_306 k = (class_3675.class_306)var10;
               return k;
            }
         }
      } catch (Throwable var6) {
      }

      try {
         return bind.method_1429();
      } catch (Throwable var5) {
         return null;
      }
   }

   private boolean active() {
      return this.mode.is("Always") || this.bind.down();
   }

   private boolean valid(class_1297 e) {
      if (e instanceof class_1309 le) {
         if (e != mc.field_1724 && e.method_5805() && !e.method_31481()) {
            if (this.playersOnly.is() && !(e instanceof class_1657)) {
               return false;
            } else {
               if (e instanceof class_1657) {
                  class_1657 p = (class_1657)e;
                  if (p.method_7325()) {
                     return false;
                  }
               }

               if (this.enemiesOnly.is() && !SevenClient.get().enemies.is(e)) {
                  return false;
               } else if (this.skipShielded.is() && TargetUtil.shieldUp(e, 5) && !canBreakShield()) {
                  return false;
               } else {
                  return le.method_6032() > 0.0F;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean canBreakShield() {
      if (SevenClient.get() != null && SevenClient.get().modules != null) {
         Module hitSwap = SevenClient.get().modules.byName("HitSwap");
         return hitSwap != null && hitSwap.isEnabled() && SlotUtil.findHotbar(TargetUtil::isAxe) >= 0;
      } else {
         return false;
      }
   }

   private static boolean isWeapon(class_1799 s) {
      return TargetUtil.isSword(s) || TargetUtil.isAxe(s);
   }
}
