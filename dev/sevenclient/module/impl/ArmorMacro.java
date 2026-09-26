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
        for (int i = 0; i < 4; i++) { queued[i] = false; wasDown[i] = keys[i].down(); clear(i); }
        owner = null; // Never re-equip on disable.
    }

    @Override public void onTick() {
        boolean[] edge = new boolean[4];
        for (int i = 0; i < 4; i++) {
            boolean raw = keys[i].down();
            edge[i] = raw && !wasDown[i];
            wasDown[i] = raw; // Sample raw state even while a GUI is open.
        }
        if (mc.field_1724 == null) { reset(); return; }
        if (owner != null && owner != mc.field_1724) reset();
        boolean allowed = mc.field_1755 == null || InventoryActions.handler(mc, true) != null;
        for (int i = 0; i < 4; i++) if (allowed && edge[i]) queued[i] = true;
        class_1703 handler = InventoryActions.handler(mc, requireOpen.is());
        if (handler == null) {
            if (mc.field_1755 != null && !allowed) for (int i = 0; i < 4; i++) queued[i] = false;
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (!queued[i]) continue;
            // Keep a valid key edge queued when the shared budget is temporarily full.
            boolean done = savedHotbar[i] >= 0 && !removeOnly.is()
                    ? restore(handler, i) : remove(handler, i);
            if (done) queued[i] = false;
            return;
        }
    }

    private boolean remove(class_1703 handler, int piece) {
        int armor = 5 + piece;
        class_1799 worn = InventoryActions.stack(handler, armor);
        if (InventoryActions.empty(worn)) return true;
        for (int h = 0; h < 9; h++) {
            if (!InventoryActions.empty(InventoryActions.stack(handler, 36 + h))) continue;
            if (!InventoryActions.canSwap(mc, handler, armor, h, requireOpen.is())) return true;
            class_1799 copy = InventoryActions.snapshot(worn);
            if (!InventoryActions.swap(mc, handler, armor, h, requireOpen.is())) return false;
            owner = mc.field_1724;
            savedHotbar[piece] = h;
            removed[piece] = copy;
            return true;
        }
        return true;
    }

    private boolean restore(class_1703 handler, int piece) {
        int h = savedHotbar[piece];
        if (owner != mc.field_1724 || !InventoryActions.empty(InventoryActions.stack(handler, 5 + piece))
                || !InventoryActions.same(InventoryActions.stack(handler, 36 + h), removed[piece])) {
            clear(piece);
            return true;
        }
        if (!InventoryActions.canSwap(mc, handler, 5 + piece, h, requireOpen.is())) return true;
        if (!InventoryActions.swap(mc, handler, 5 + piece, h, requireOpen.is())) return false;
        clear(piece);
        return true;
    }

    private void clear(int piece) { savedHotbar[piece] = -1; removed[piece] = null; }

    private void reset() {
        for (int i = 0; i < 4; i++) { queued[i] = false; clear(i); }
        owner = null;
    }
}
