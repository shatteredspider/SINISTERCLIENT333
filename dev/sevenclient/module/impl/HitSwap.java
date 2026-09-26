package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.mixin.ClientPlayerInteractionManagerAccessor;
import dev.sevenclient.mixin.MinecraftClientAccessor;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.ActionBudget;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.SlotGuard;
import dev.sevenclient.util.SlotUtil;
import dev.sevenclient.util.TargetUtil;
import java.util.Objects;
import java.util.Random;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_3966;

public class HitSwap extends Module {
   private final ModeSetting swapTo = (ModeSetting)this.reg(new ModeSetting("Swap To", "Axe", new String[]{"Axe", "Sword", "Auto"}));
   private final BoolSetting requireClick = (BoolSetting)this.reg(new BoolSetting("Require Click", true));
   private final BoolSetting onlyShield = (BoolSetting)this.reg(new BoolSetting("Only On Shield", true));
   private final NumberSetting shieldMinTicks = (NumberSetting)this.reg(new NumberSetting("Shield Min Ticks", (double)5.0F, (double)0.0F, (double)20.0F, (double)1.0F));
   private final BoolSetting doubleHit = (BoolSetting)this.reg(new BoolSetting("Double Hit", false));
   private final BoolSetting requireCharge = (BoolSetting)this.reg(new BoolSetting("Require Charge", true));
   private final NumberSetting minCharge = (NumberSetting)this.reg(new NumberSetting("Min Charge %", (double)90.0F, (double)0.0F, (double)100.0F, (double)1.0F));
   private final ModeSetting strikeTiming = (ModeSetting)this.reg(new ModeSetting("Strike Timing", "Same Tick", new String[]{"Same Tick", "Next Tick"}));
   private final BoolSetting stun = (BoolSetting)this.reg(new BoolSetting("Stun", true));
   private final NumberSetting stunCpsMin = (NumberSetting)this.reg(new NumberSetting("Stun CPS Min", (double)2.0F, (double)1.0F, (double)8.0F, (double)1.0F));
   private final NumberSetting stunCpsMax = (NumberSetting)this.reg(new NumberSetting("Stun CPS Max", (double)3.0F, (double)1.0F, (double)8.0F, (double)1.0F));
   private final NumberSetting backMinMs = (NumberSetting)this.reg(new NumberSetting("Swap Back Min MS", (double)60.0F, (double)0.0F, (double)300.0F, (double)1.0F));
   private final NumberSetting backMaxMs = (NumberSetting)this.reg(new NumberSetting("Swap Back Max MS", (double)91.0F, (double)0.0F, (double)300.0F, (double)1.0F));
   private final BoolSetting revertSlot = (BoolSetting)this.reg(new BoolSetting("Revert Slot", true));
   private final BoolSetting ignoreIfUsing = (BoolSetting)this.reg(new BoolSetting("Ignore If Using Item", true));
   private static final int NONE = -1;
   private int savedSlot = -1;
   private long restoreAt = 0L;
   private long lastHit = 0L;
   private boolean prevLeftDown = false;
   private int pendingStrikeSlot = -1;
   private boolean followUpPending = false;
   private final Random rng = new Random();

   public HitSwap() {
      super("HitSwap", "Swaps to axe and strikes with the axe, in that packet order.", Category.COMBAT);
      NumberSetting var10000 = this.minCharge;
      BoolSetting var10001 = this.requireCharge;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.shieldMinTicks;
      var10001 = this.onlyShield;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.stunCpsMin;
      var10001 = this.stun;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      var10000 = this.stunCpsMax;
      var10001 = this.stun;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.registerBindSettings();
   }

   public void onEnable() {
      this.savedSlot = -1;
      this.pendingStrikeSlot = -1;
      this.prevLeftDown = false;
      if (mc.field_1724 != null && this.partner() == -1) {
         SevenClient.chat("§7HitSwap §8- §7no swap partner in the hotbar.");
      }

   }

   public void onDisable() {
      this.restoreNow();
      this.pendingStrikeSlot = -1;
      this.followUpPending = false;
      this.prevLeftDown = false;
   }

   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         boolean leftDown = mc.field_1729.method_1608();
         boolean freshClick = leftDown && !this.prevLeftDown;
         this.prevLeftDown = leftDown;
         if (this.followUpPending) {
            this.followUpPending = false;
            if (this.holdingPartner() && ActionBudget.claim("attack", 55L, now)) {
               try {
                  ((MinecraftClientAccessor)mc).seven$doAttack();
                  HumanDiag.hitSwapState = "follow-up hit";
               } catch (Throwable t) {
                  HumanDiag.hitSwapState = "follow-up failed: " + t.getClass().getSimpleName();
               }
            }

         } else if (this.pendingStrikeSlot == -1) {
            if (this.savedSlot != -1 && now >= this.restoreAt) {
               this.restoreNow();
            }

            if (mc.field_1755 != null) {
               HumanDiag.hitSwapState = "screen open";
            } else if (this.requireClick.is() && !leftDown) {
               HumanDiag.hitSwapState = "waiting for click";
            } else if (this.ignoreIfUsing.is() && mc.field_1724.method_6115()) {
               HumanDiag.hitSwapState = "using item";
            } else {
               class_1657 target = this.crosshairPlayer();
               if (target == null) {
                  HumanDiag.hitSwapState = "no player on crosshair";
               } else if (this.onlyShield.is() && !TargetUtil.shieldUp(target, (int)this.shieldMinTicks.val())) {
                  HumanDiag.hitSwapState = target.method_6039() ? "shield still winding up" : "target not blocking";
               } else {
                  float charge = mc.field_1724.method_7261(0.0F);
                  if (this.requireCharge.is() && (double)charge < this.minCharge.val() / (double)100.0F) {
                     HumanDiag.hitSwapState = String.format("charging %.0f%%", charge * 100.0F);
                  } else {
                     int to = this.partner();
                     if (to == -1) {
                        HumanDiag.hitSwapState = "NO PARTNER IN HOTBAR (need an axe)";
                     } else {
                        double targetCps = (double)1.0F;
                        if (this.stun.is()) {
                           double lo = Math.min(this.stunCpsMin.val(), this.stunCpsMax.val());
                           double hi = Math.max(this.stunCpsMin.val(), this.stunCpsMax.val());
                           targetCps = lo + this.rng.nextDouble() * (hi - lo);
                        }

                        long minClickGap = (long)((double)1000.0F / Math.max((double)0.5F, targetCps));
                        if (now - this.lastHit < minClickGap) {
                           HumanDiag.hitSwapState = String.format("stun cadence (%.1f cps)", targetCps);
                        } else {
                           if (SlotUtil.selected() != to) {
                              if (this.savedSlot == -1) {
                                 this.savedSlot = SlotUtil.selected();
                              }

                              if (!SlotGuard.select(to, now)) {
                                 HumanDiag.hitSwapState = "slot budget refused, holding fire";
                                 return;
                              }

                              if (!this.syncSlot()) {
                                 HumanDiag.hitSwapState = "slot sync invoker unavailable, holding fire";
                                 return;
                              }
                           }

                           this.restoreAt = now + this.swapBackDelay();
                           if (this.strikeTiming.is("Next Tick")) {
                              this.pendingStrikeSlot = to;
                              HumanDiag.hitSwapState = "swapped -> " + (to + 1) + ", striking next tick";
                           } else {
                              this.strike(now, freshClick);
                           }
                        }
                     }
                  }
               }
            }
         } else {
            int want = this.pendingStrikeSlot;
            this.pendingStrikeSlot = -1;
            if (SlotUtil.selected() == want && this.holdingPartner()) {
               this.strike(now, false);
            } else {
               ++HumanDiag.hitSwapMisses;
               HumanDiag.hitSwapState = "deferred strike aborted, slot moved";
            }

         }
      }
   }

   private void strike(long now, boolean yieldToPlayer) {
      if (!this.holdingPartner()) {
         ++HumanDiag.hitSwapMisses;
         HumanDiag.hitSwapState = "partner not in main hand, refusing to swing";
      } else {
         this.lastHit = now;
         ++HumanDiag.hitSwapSwaps;
         HumanDiag.hitSwapLastDelay = (double)(this.restoreAt - now);
         if (yieldToPlayer) {
            int var6 = SlotUtil.selected() + 1;
            HumanDiag.hitSwapState = "SWAP -> " + var6 + "  your click carries it  (" + HumanDiag.hitSwapSwaps + ")";
         } else if (!ActionBudget.claim("attack", 55L, now)) {
            HumanDiag.hitSwapState = "attack budget refused";
         } else {
            try {
               ((MinecraftClientAccessor)mc).seven$doAttack();
               if (this.doubleHit.is()) {
                  this.followUpPending = true;
                  this.restoreAt = Math.max(this.restoreAt, now + 60L);
               }

               int var10000 = SlotUtil.selected() + 1;
               HumanDiag.hitSwapState = "SWAP -> " + var10000 + "  strike  (" + HumanDiag.hitSwapSwaps + ")";
            } catch (Throwable t) {
               HumanDiag.hitSwapState = "doAttack failed: " + t.getClass().getSimpleName();
            }

         }
      }
   }

   private boolean syncSlot() {
      try {
         if (mc.field_1761 == null) {
            return false;
         } else {
            ((ClientPlayerInteractionManagerAccessor)mc.field_1761).seven$syncSelectedSlot();
            return true;
         }
      } catch (Throwable var2) {
         return false;
      }
   }

   private long swapBackDelay() {
      double lo = Math.min(this.backMinMs.val(), this.backMaxMs.val());
      double hi = Math.max(this.backMinMs.val(), this.backMaxMs.val());
      return (long)(lo + this.rng.nextDouble() * Math.max((double)0.0F, hi - lo));
   }

   private boolean holdingPartner() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         boolean var10000;
         switch ((String)this.swapTo.get()) {
            case "Sword" -> var10000 = TargetUtil.isSword(mc.field_1724.method_6047());
            case "Auto" -> var10000 = TargetUtil.isAxe(mc.field_1724.method_6047()) || TargetUtil.isSword(mc.field_1724.method_6047());
            default -> var10000 = TargetUtil.isAxe(mc.field_1724.method_6047());
         }

         return var10000;
      }
   }

   public void onPostTick() {
      if (this.savedSlot != -1 && this.restoreAt > 0L && System.currentTimeMillis() - this.restoreAt > 500L) {
         this.restoreNow();
      }

   }

   private void restoreNow() {
      if (this.savedSlot != -1) {
         int back = this.savedSlot;
         this.savedSlot = -1;
         this.restoreAt = 0L;
         if (this.revertSlot.is()) {
            SlotUtil.select(back);
         }
      }

   }

   private class_1657 crosshairPlayer() {
      class_239 var3 = mc.field_1765;
      if (var3 instanceof class_3966 ehr) {
         class_1297 var4 = ehr.method_17782();
         if (var4 instanceof class_1657 p) {
            if (p != mc.field_1724 && !p.method_7325() && !SevenClient.get().friends.is(p)) {
               return p;
            }
         }
      }

      return null;
   }

   private int partner() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         int var10000;
         switch ((String)this.swapTo.get()) {
            case "Sword" -> var10000 = SlotUtil.findHotbar(TargetUtil::isSword);
            case "Auto" -> var10000 = TargetUtil.isAxe(mc.field_1724.method_6047()) ? SlotUtil.findHotbar(TargetUtil::isSword) : SlotUtil.findHotbar(TargetUtil::isAxe);
            default -> var10000 = SlotUtil.findHotbar(TargetUtil::isAxe);
         }

         return var10000;
      }
   }
}
