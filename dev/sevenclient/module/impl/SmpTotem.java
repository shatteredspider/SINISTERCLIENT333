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
    private int source = -1;
    private boolean manualDown, manualHold;

    public SmpTotem() {
        super("SmpTotem", "Equips a totem at low health or on a manual key, and restores only its own swap.", Category.COMBAT);
        registerBindSettings();
    }

    @Override public void onEnable() { manualDown = manual.down(); }

    @Override public void onDisable() {
        restore();
        manualHold = false;
        manualDown = false;
        // If unsafe, leave the item where it is. Never restore later from a stale session.
        forget();
    }

    @Override public void onTick() {
        if (mc.field_1724 == null) { forget(); manualHold = false; return; }
        if (owner != null && owner != mc.field_1724) { forget(); manualHold = false; }
        boolean down = mc.field_1755 == null && manual.down();
        boolean pressed = down && !manualDown;
        manualDown = down;
        class_1703 handler = InventoryActions.handler(mc, false);
        if (handler == null) return;
        if (source >= 0 && !InventoryActions.same(InventoryActions.stack(handler, 45), placed)) {
            forget(); manualHold = false;
        }
        if (pressed) {
            if (source >= 0 && manualHold) { manualHold = false; restore(); }
            else { manualHold = true; if (source < 0) equip(handler); }
        }
        float hp = mc.field_1724.method_6032();
        if (source < 0 && hp <= low.valf() * 2 && low.valf() < high.valf()) equip(handler);
        else if (source >= 0 && !manualHold && hp >= high.valf() * 2) restore();
    }

    private void equip(class_1703 handler) {
        if (!InventoryActions.empty(InventoryActions.stack(handler, 45))
                && InventoryActions.stack(handler, 45).method_31574(class_1802.field_8288)) return;
        for (int i = 9; i <= 44; i++) {
            if (!InventoryActions.empty(InventoryActions.stack(handler, i))
                    && InventoryActions.stack(handler, i).method_31574(class_1802.field_8288)) {
                class_1799 old = InventoryActions.snapshot(InventoryActions.stack(handler, 45));
                class_1799 totem = InventoryActions.snapshot(InventoryActions.stack(handler, i));
                if (InventoryActions.swap(mc, handler, i, 40, false)) {
                    owner = mc.field_1724;
                    source = i;
                    previous = old;
                    placed = totem;
                }
                return;
            }
        }
    }

    private void restore() {
        class_1703 handler = InventoryActions.handler(mc, false);
        if (source < 0 || owner != mc.field_1724 || handler == null) return;
        if (!InventoryActions.same(InventoryActions.stack(handler, 45), placed)
                || !InventoryActions.same(InventoryActions.stack(handler, source), previous)) {
            forget();
        } else if (InventoryActions.swap(mc, handler, source, 40, false)) {
            forget();
        }
    }

    private void forget() { owner = null; source = -1; previous = null; placed = null; }
}
