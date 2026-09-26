package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.InventoryActions;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

/** Manual offhand toggle plus automatic low-health protection. */
public final class SmpTotem extends Module {
    private final KeySetting manual = reg(new KeySetting("Manual Totem"));
    private final NumberSetting low = reg(new NumberSetting("Equip At Hearts", 3, 1, 10, 0.5));
    private final NumberSetting high = reg(new NumberSetting("Restore At Hearts", 6, 1, 20, 0.5));
    private class_1657 owner;
    private class_1799 previous, placed;
    private int source = -1, confirmationTicks;
    private boolean manualDown, manualHold, pendingManual, pendingRestore, confirmed;

    public SmpTotem() {
        super("SmpTotem", "Equips a totem at low health or on a manual key, and restores only its own swap.", Category.COMBAT);
        registerBindSettings();
    }

    @Override public void onEnable() {
        manualDown = manual.down();
        pendingManual = false;
        pendingRestore = false;
    }

    @Override public void onDisable() {
        if (confirmed) restore();
        manualHold = false;
        pendingManual = false;
        pendingRestore = false;
        manualDown = manual.down();
        // Never carry a restoration request into a new session.
        forget();
    }

    @Override public void onTick() {
        boolean raw = manual.down();
        boolean edge = raw && !manualDown;
        manualDown = raw; // GUI changes never synthesize a new edge.
        if (mc.field_1724 == null) { forget(); manualHold = false; pendingManual = false; return; }
        if (owner != null && owner != mc.field_1724) {
            forget(); manualHold = false; pendingManual = false;
        }
        if (edge && mc.field_1755 == null) pendingManual = true;
        if (mc.field_1755 != null) pendingManual = false;

        class_1703 handler = InventoryActions.handler(mc, false);
        if (handler == null) return;
        if (source >= 0 && !confirmed) {
            if (InventoryActions.same(InventoryActions.stack(handler, 45), placed)
                    && InventoryActions.same(InventoryActions.stack(handler, source), previous)) confirmed = true;
            else if (++confirmationTicks > 5) { forget(); manualHold = false; }
            else return;
        }
        if (source >= 0 && (!InventoryActions.same(InventoryActions.stack(handler, 45), placed)
                || !InventoryActions.same(InventoryActions.stack(handler, source), previous))) {
            forget(); manualHold = false; pendingRestore = false;
        }
        if (pendingManual) {
            if (source >= 0 && manualHold) {
                manualHold = false;
                pendingRestore = true;
                pendingManual = false;
            } else if (source >= 0) {
                manualHold = true;
                pendingManual = false;
            } else if (offhandTotem(handler)) {
                manualHold = true;
                pendingManual = false;
            } else if (equip(handler)) {
                manualHold = true;
                pendingManual = false;
            }
        }
        if (pendingRestore) { if (restore()) pendingRestore = false; return; }
        float hp = mc.field_1724.method_6032();
        if (source < 0 && hp <= low.valf() * 2 && low.valf() < high.valf()
                && !offhandTotem(handler)) equip(handler);
        else if (source >= 0 && !manualHold && hp >= high.valf() * 2) restore();
    }

    private boolean offhandTotem(class_1703 handler) {
        class_1799 hand = InventoryActions.stack(handler, 45);
        return !InventoryActions.empty(hand) && hand.method_31574(class_1802.field_8288);
    }

    private boolean equip(class_1703 handler) {
        for (int i = 9; i <= 44; i++) {
            class_1799 candidate = InventoryActions.stack(handler, i);
            if (!InventoryActions.empty(candidate) && candidate.method_31574(class_1802.field_8288)
                    && InventoryActions.canSwap(mc, handler, i, 40, false)) {
                class_1799 old = InventoryActions.snapshot(InventoryActions.stack(handler, 45));
                class_1799 totem = InventoryActions.snapshot(candidate);
                if (!InventoryActions.swap(mc, handler, i, 40, false)) return false;
                owner = mc.field_1724;
                source = i;
                previous = old;
                placed = totem;
                confirmed = false;
                confirmationTicks = 0;
                return true;
            }
        }
        return false;
    }

    private boolean restore() {
        class_1703 handler = InventoryActions.handler(mc, false);
        if (source < 0 || owner != mc.field_1724 || handler == null || !confirmed) return false;
        if (!InventoryActions.same(InventoryActions.stack(handler, 45), placed)
                || !InventoryActions.same(InventoryActions.stack(handler, source), previous)) {
            forget();
            return true;
        }
        if (!InventoryActions.canSwap(mc, handler, source, 40, false)) return false;
        if (!InventoryActions.swap(mc, handler, source, 40, false)) return false;
        forget();
        return true;
    }

    private void forget() {
        owner = null;
        source = -1;
        previous = null;
        placed = null;
        confirmed = false;
        confirmationTicks = 0;
    }
}
