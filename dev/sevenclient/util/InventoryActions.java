package dev.sevenclient.util;

import java.lang.reflect.Field;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_465;
import net.minecraft.class_490;

/** Player-handler slot clicks only. No client-side inventory mutation or synthetic packets. */
public final class InventoryActions {
    private static long lastClick;

    private InventoryActions() {}

    public static class_1703 handler(class_310 mc, boolean inventoryOpenOnly) {
        if (mc.field_1724 == null || mc.field_1761 == null) return null;
        class_1657 player = mc.field_1724;
        class_1703 handler = player.field_7498;
        if (handler == null || player.field_7512 != handler || handler.field_7763 != 0
                || !handler.method_34255().method_7960() || handler.field_7761.size() != 46) return null;
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

    /** One atomic server-side SWAP, never a cursor pickup sequence. */
    public static boolean swap(class_310 mc, class_1703 expected, int slot, int button, boolean openOnly) {
        class_1703 handler = handler(mc, openOnly);
        long now = System.nanoTime();
        if (handler == null || handler != expected || slot < 5 || slot > 45
                || !(button == 40 || button >= 0 && button <= 8)
                || now - lastClick < 75_000_000L) return false;
        class_1735 target = handler.method_7611(slot);
        if (!target.method_7674(mc.field_1724)) return false;
        lastClick = now;
        mc.field_1761.method_2906(handler.field_7763, slot, button, class_1713.field_7791, mc.field_1724);
        return true;
    }

    /** Requires both an actual mouse hit and the GUI's current focused slot. */
    public static int hoveredSource(class_310 mc, class_1703 handler) {
        if (!(mc.field_1755 instanceof class_490 screen) || screen.method_17577() != handler) return -1;
        try {
            Field focus = class_465.class.getDeclaredField("field_2787");
            Field originX = class_465.class.getDeclaredField("field_2776");
            Field originY = class_465.class.getDeclaredField("field_2800");
            focus.setAccessible(true);
            originX.setAccessible(true);
            originY.setAccessible(true);
            class_1735 hovered = (class_1735) focus.get(screen);
            if (hovered == null || hovered.field_7874 < 9 || hovered.field_7874 > 35
                    || handler.method_7611(hovered.field_7874) != hovered) return -1;
            double x = mc.field_1729.method_1603() * mc.method_22683().method_4486()
                    / mc.method_22683().method_4480();
            double y = mc.field_1729.method_1604() * mc.method_22683().method_4502()
                    / mc.method_22683().method_4507();
            int left = originX.getInt(screen) + hovered.field_7873;
            int top = originY.getInt(screen) + hovered.field_7872;
            return x >= left && x < left + 16 && y >= top && y < top + 16
                    ? hovered.field_7874 : -1;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            return -1;
        }
    }
}
