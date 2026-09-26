package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.util.InventoryActions;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1799;

/** Each armor key can share a binding. A single guarded SWAP is sent per tick. */
public final class ArmorMacro extends Module {
    private final KeySetting[] keys = {
        reg(new KeySetting("Helmet Key")), reg(new KeySetting("Chest Key")),
        reg(new KeySetting("Leggings Key")), reg(new KeySetting("Boots Key"))
    };
    private final BoolSetting removeOnly = reg(new BoolSetting("Remove Only", false));
    private final BoolSetting requireOpen = reg(new BoolSetting("Require Inventory Open", true));
    private final boolean[] wasDown = new boolean[4], queued = new boolean[4];
    private final class_1799[] removed = new class_1799[4];
    private final int[] savedHotbar = {-1, -1, -1, -1};
    private class_1657 owner;

    public ArmorMacro() {
        super("ArmorMacro", "Removes armor by piece, optionally toggling it back from its reserved hotbar slot.", Category.PLAYER);
        registerBindSettings();
    }

    @Override public void onEnable() {
        for (int i = 0; i < 4; i++) { queued[i] = false; wasDown[i] = keys[i].down(); }
    }

    @Override public void onDisable() {
        for (int i = 0; i < 4; i++) { queued[i] = false; wasDown[i] = false; clear(i); }
        owner = null; // Never re-equip behind the user's back on disable.
    }

    @Override public void onTick() {
        if (mc.field_1724 == null) { reset(); return; }
        if (owner != null && owner != mc.field_1724) reset();
        boolean allowed = mc.field_1755 == null || InventoryActions.handler(mc, true) != null;
        for (int i = 0; i < 4; i++) {
            boolean down = allowed && keys[i].down();
            if (down && !wasDown[i]) queued[i] = true;
            wasDown[i] = down;
        }
        class_1703 handler = InventoryActions.handler(mc, requireOpen.is());
        if (handler == null) { for (int i = 0; i < 4; i++) queued[i] = false; return; }
        for (int i = 0; i < 4; i++) {
            if (!queued[i]) continue;
            queued[i] = false;
            if (savedHotbar[i] >= 0) {
                if (!removeOnly.is()) restore(handler, i);
                else clear(i);
            } else remove(handler, i);
            // A shared key queues all pieces. Subsequent ticks process the rest.
            return;
        }
    }

    private void remove(class_1703 handler, int piece) {
        int armor = 5 + piece;
        class_1799 worn = InventoryActions.stack(handler, armor);
        if (InventoryActions.empty(worn)) return;
        // Reserve a distinct empty hotbar slot; never overwrite tools or other removed pieces.
        for (int h = 0; h < 9; h++) {
            if (!InventoryActions.empty(InventoryActions.stack(handler, 36 + h))) continue;
            class_1799 copy = InventoryActions.snapshot(worn);
            if (InventoryActions.swap(mc, handler, armor, h, requireOpen.is())) {
                owner = mc.field_1724;
                savedHotbar[piece] = h;
                removed[piece] = copy;
            }
            return;
        }
    }

    private void restore(class_1703 handler, int piece) {
        int h = savedHotbar[piece];
        if (owner != mc.field_1724 || !InventoryActions.empty(InventoryActions.stack(handler, 5 + piece))
                || !InventoryActions.same(InventoryActions.stack(handler, 36 + h), removed[piece])) {
            clear(piece);
            return;
        }
        if (InventoryActions.swap(mc, handler, 5 + piece, h, requireOpen.is())) clear(piece);
    }

    private void clear(int piece) { savedHotbar[piece] = -1; removed[piece] = null; }

    private void reset() {
        for (int i = 0; i < 4; i++) { queued[i] = false; wasDown[i] = false; clear(i); }
        owner = null;
    }
}
