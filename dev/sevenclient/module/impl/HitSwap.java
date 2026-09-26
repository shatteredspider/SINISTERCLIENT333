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
   private final BoolSetting enemiesOnly = (BoolSetting)this.reg(new BoolSetting("Enemies Only", false));
   private final NumberSetting shieldMinTicks = (NumberSetting)this.reg(new NumberSetting("Shield Min Ticks", 5.0, 0.0, 20.0, 1.0));
   private final BoolSetting doubleHit = (BoolSetting)this.reg(new BoolSetting("Double Hit", false));
   private final BoolSetting requireCharge = (BoolSetting)this.reg(new BoolSetting("Require Charge", true));
   private final NumberSetting minCharge = (NumberSetting)this.reg(new NumberSetting("Min Charge %", 90.0, 0.0, 100.0, 1.0));
   private final ModeSetting strikeTiming = (ModeSetting)this.reg(new ModeSetting("Strike Timing", "Same Tick", new String[]{"Same Tick", "Next Tick"}));
   private final BoolSetting stun = (BoolSetting)this.reg(new BoolSetting("Stun", true));
   private final NumberSetting stunCpsMin = (NumberSetting)this.reg(new NumberSetting("Stun CPS Min", 2.0, 1.0, 8.0, 1.0));
   private final NumberSetting stunCpsMax = (NumberSetting)this.reg(new NumberSetting("Stun CPS Max", 3.0, 1.0, 8.0, 1.0));
   private final NumberSetting backMinMs = (NumberSetting)this.reg(new NumberSetting("Swap Back Min MS", 60.0, 0.0, 300.0, 1.0));
   private final NumberSetting backMaxMs = (NumberSetting)this.reg(new NumberSetting("Swap Back Max MS", 91.0, 0.0, 300.0, 1.0));
   private final BoolSetting revertSlot = (BoolSetting)this.reg(new BoolSetting("Revert Slot", true));
   private final BoolSetting ignoreIfUsing = (BoolSetting)this.reg(new BoolSetting("Ignore If Using Item", true));
   private int savedSlot = -1;
   private long restoreAt;
   private long lastHit;
   private boolean prevLeftDown;
   private int pendingStrikeSlot = -1;
   private boolean followUpPending;
   private final Random rng = new Random();

   public HitSwap() {
      super("HitSwap", "Swaps to axe and strikes with the axe, in that packet order.", Category.COMBAT);
      this.minCharge.visibleWhen(this.requireCharge::is);
      this.shieldMinTicks.visibleWhen(this.onlyShield::is);
      this.stunCpsMin.visibleWhen(this.stun::is);
      this.stunCpsMax.visibleWhen(this.stun::is);
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
      if (mc.field_1724 == null || mc.field_1687 == null) return;
      long now = System.currentTimeMillis();
      boolean leftDown = mc.field_1729.method_1608();
      boolean freshClick = leftDown && !this.prevLeftDown;
      this.prevLeftDown = leftDown;
      if (this.followUpPending) {
         this.followUpPending = false;
         if (this.crosshairPlayer() != null && this.holdingPartner()
               && ActionBudget.claim("attack", 55L, now)) {
            try {
               ((MinecraftClientAccessor)mc).seven$doAttack();
               HumanDiag.hitSwapState = "follow-up hit";
            } catch (Throwable t) {
               HumanDiag.hitSwapState = "follow-up failed: " + t.getClass().getSimpleName();
            }
         }
      } else if (this.pendingStrikeSlot == -1) {
         if (this.savedSlot != -1 && now >= this.restoreAt) this.restoreNow();
         if (mc.field_1755 != null) {
            HumanDiag.hitSwapState = "screen open";
         } else if (this.requireClick.is() && !leftDown) {
            HumanDiag.hitSwapState = "waiting for click";
         } else if (this.ignoreIfUsing.is() && mc.field_1724.method_6115()) {
            HumanDiag.hitSwapState = "using item";
         } else {
            class_1657 target = this.crosshairPlayer();
            if (target == null) {
               HumanDiag.hitSwapState = "no eligible player on crosshair";
            } else if (this.onlyShield.is() && !TargetUtil.shieldUp(target, (int)this.shieldMinTicks.val())) {
               HumanDiag.hitSwapState = target.method_6039() ? "shield still winding up" : "target not blocking";
            } else {
               float charge = mc.field_1724.method_7261(0.0F);
               if (this.requireCharge.is() && charge < this.minCharge.val() / 100.0) {
                  HumanDiag.hitSwapState = String.format("charging %.0f%%", charge * 100.0F);
               } else {
                  int to = this.partner();
                  if (to == -1) {
                     HumanDiag.hitSwapState = "NO PARTNER IN HOTBAR (need an axe)";
                  } else {
                     double cps = 1.0;
                     if (this.stun.is()) {
                        double lo = Math.min(this.stunCpsMin.val(), this.stunCpsMax.val());
                        double hi = Math.max(this.stunCpsMin.val(), this.stunCpsMax.val());
                        cps = lo + this.rng.nextDouble() * (hi - lo);
                     }
                     long gap = (long)(1000.0 / Math.max(0.5, cps));
                     if (now - this.lastHit < gap) {
                        HumanDiag.hitSwapState = String.format("stun cadence (%.1f cps)", cps);
                     } else {
                        if (SlotUtil.selected() != to) {
                           if (this.savedSlot == -1) this.savedSlot = SlotUtil.selected();
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
         if (SlotUtil.selected() == want && this.holdingPartner() && this.crosshairPlayer() != null) {
            this.strike(now, false);
         } else {
            ++HumanDiag.hitSwapMisses;
            HumanDiag.hitSwapState = "deferred strike aborted, slot moved or target invalid";
         }
      }
   }

   private void strike(long now, boolean yieldToPlayer) {
      if (this.crosshairPlayer() == null || !this.holdingPartner()) {
         ++HumanDiag.hitSwapMisses;
         HumanDiag.hitSwapState = "target or partner invalid, refusing to swing";
         return;
      }
      this.lastHit = now;
      ++HumanDiag.hitSwapSwaps;
      HumanDiag.hitSwapLastDelay = this.restoreAt - now;
      if (yieldToPlayer) {
         HumanDiag.hitSwapState = "SWAP -> " + (SlotUtil.selected() + 1) + "  your click carries it  (" + HumanDiag.hitSwapSwaps + ")";
      } else if (!ActionBudget.claim("attack", 55L, now)) {
         HumanDiag.hitSwapState = "attack budget refused";
      } else {
         try {
            ((MinecraftClientAccessor)mc).seven$doAttack();
            if (this.doubleHit.is()) {
               this.followUpPending = true;
               this.restoreAt = Math.max(this.restoreAt, now + 60L);
            }
            HumanDiag.hitSwapState = "SWAP -> " + (SlotUtil.selected() + 1) + "  strike  (" + HumanDiag.hitSwapSwaps + ")";
         } catch (Throwable t) {
            HumanDiag.hitSwapState = "doAttack failed: " + t.getClass().getSimpleName();
         }
      }
   }

   private boolean syncSlot() {
      try {
         if (mc.field_1761 == null) return false;
         ((ClientPlayerInteractionManagerAccessor)mc.field_1761).seven$syncSelectedSlot();
         return true;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private long swapBackDelay() {
      double lo = Math.min(this.backMinMs.val(), this.backMaxMs.val());
      double hi = Math.max(this.backMinMs.val(), this.backMaxMs.val());
      return (long)(lo + this.rng.nextDouble() * Math.max(0.0, hi - lo));
   }

   private boolean holdingPartner() {
      if (mc.field_1724 == null) return false;
      return switch ((String)this.swapTo.get()) {
         case "Sword" -> TargetUtil.isSword(mc.field_1724.method_6047());
         case "Auto" -> TargetUtil.isAxe(mc.field_1724.method_6047()) || TargetUtil.isSword(mc.field_1724.method_6047());
         default -> TargetUtil.isAxe(mc.field_1724.method_6047());
      };
   }

   public void onPostTick() {
      if (this.savedSlot != -1 && this.restoreAt > 0L
            && System.currentTimeMillis() - this.restoreAt > 500L) this.restoreNow();
   }

   private void restoreNow() {
      if (this.savedSlot == -1) return;
      int back = this.savedSlot;
      this.savedSlot = -1;
      this.restoreAt = 0L;
      if (this.revertSlot.is()) SlotUtil.select(back);
   }

   private class_1657 crosshairPlayer() {
      class_239 hit = mc.field_1765;
      if (hit instanceof class_3966 entityHit) {
         class_1297 entity = entityHit.method_17782();
         if (entity instanceof class_1657 player && player != mc.field_1724
               && player.method_5805() && !player.method_7325()
               && !SevenClient.get().friends.is(player)
               && (!this.enemiesOnly.is() || SevenClient.get().enemies.is(player))) {
            return player;
         }
      }
      return null;
   }

   private int partner() {
      if (mc.field_1724 == null) return -1;
      return switch ((String)this.swapTo.get()) {
         case "Sword" -> SlotUtil.findHotbar(TargetUtil::isSword);
         case "Auto" -> TargetUtil.isAxe(mc.field_1724.method_6047())
               ? SlotUtil.findHotbar(TargetUtil::isSword) : SlotUtil.findHotbar(TargetUtil::isAxe);
         default -> SlotUtil.findHotbar(TargetUtil::isAxe);
      };
   }
}
