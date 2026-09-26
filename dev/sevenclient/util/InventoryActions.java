package dev.sevenclient.util;

import java.lang.reflect.Method;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_465;
import net.minecraft.class_490;

/** Player inventory handler only. SWAP never puts a stack on the cursor. */
public final class InventoryActions {
    private InventoryActions() {}

    private static boolean layout(class_1703 handler, class_1661 inventory) {
        if (handler.field_7761.size() != 46) return false;
        for (int id = 5; id <= 45; id++) {
            class_1735 slot = handler.method_7611(id);
            int index = id <= 8 ? 44 - id : id <= 35 ? id : id <= 44 ? id - 36 : 40;
            if (slot == null || slot.field_7874 != id || slot.field_7871 != inventory
                    || slot.method_34266() != index) return false;
        }
        return true;
    }

    public static class_1703 handler(class_310 mc, boolean inventoryOpenOnly) {
        if (mc.field_1724 == null || mc.field_1761 == null) return null;
        class_1657 player = mc.field_1724;
        class_1703 handler = player.field_7498;
        if (handler == null || player.field_7512 != handler || handler.field_7763 != 0
                || !handler.method_34255().method_7960()
                || !layout(handler, player.method_31548())) return null;
        if (inventoryOpenOnly) {
            if (!(mc.field_1755 instanceof class_490 screen) || screen.method_17577() != handler) return null;
        } else if (mc.field_1755 != null
                && (!(mc.field_1755 instanceof class_490 screen) || screen.method_17577() != handler)) {
            return null;
        }
        return handler;
    }

    public static class_1799 stack(class_1703 handler, int slot) {
        return handler.method_7611(slot).method_7677();
    }

    public static boolean empty(class_1799 stack) {
        return stack == null || stack.method_7960();
    }

    public static boolean same(class_1799 a, class_1799 b) {
        return empty(a) ? empty(b) : !empty(b) && a.method_7947() == b.method_7947()
                && class_1799.method_31577(a, b);
    }

    public static class_1799 snapshot(class_1799 stack) {
        return empty(stack) ? null : stack.method_7972();
    }

    /** Does not spend the action budget. Checks both halves of the SWAP. */
    public static boolean canSwap(class_310 mc, class_1703 expected, int slot, int button, boolean openOnly) {
        class_1703 handler = handler(mc, openOnly);
        if (handler == null || handler != expected || !(slot >= 9 && slot <= 35
                || slot >= 5 && slot <= 8 || slot >= 36 && slot <= 44)
                || !(button == 40 || button >= 0 && button <= 8)) return false;
        int other = button == 40 ? 45 : 36 + button;
        if (slot == other || button == 40 && slot >= 5 && slot <= 8) return false;
        class_1735 source = handler.method_7611(slot);
        class_1735 target = handler.method_7611(other);
        class_1799 a = source.method_7677();
        class_1799 b = target.method_7677();
        if (empty(a) && empty(b)) return false;
        return (empty(a) || source.method_7674(mc.field_1724))
                && (empty(b) || target.method_7674(mc.field_1724))
                && (empty(a) || target.method_7680(a))
                && (empty(b) || source.method_7680(b));
    }

    public static boolean swap(class_310 mc, class_1703 expected, int slot, int button, boolean openOnly) {
        if (!canSwap(mc, expected, slot, button, openOnly)) return false;
        long now = System.currentTimeMillis();
        if (!ActionBudget.claim("inventory_swap", 75L, now)) return false;
        mc.field_1761.method_2906(expected.field_7763, slot, button, class_1713.field_7791, mc.field_1724);
        return true;
    }

    /** Invoke the mapped hit-test, rather than trust a focused slot from a previous render. */
    public static int hoveredSource(class_310 mc, class_1703 handler) {
        if (!(mc.field_1755 instanceof class_490 screen) || screen.method_17577() != handler) return -1;
        try {
            Method at = class_465.class.getDeclaredMethod("method_64240", double.class, double.class);
            at.setAccessible(true);
            double x = mc.field_1729.method_1603() * mc.method_22683().method_4486()
                    / mc.method_22683().method_4480();
            double y = mc.field_1729.method_1604() * mc.method_22683().method_4502()
                    / mc.method_22683().method_4507();
            class_1735 hovered = (class_1735) at.invoke(screen, x, y);
            return hovered != null && hovered.field_7874 >= 9 && hovered.field_7874 <= 35
                    && handler.method_7611(hovered.field_7874) == hovered ? hovered.field_7874 : -1;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            return -1;
        }
    }
}
