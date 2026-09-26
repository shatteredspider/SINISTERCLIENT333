package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.InventoryActions;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_9334;

/** Refill empty hotbar slots from the open player inventory. */
public final class QuickPotion extends Module {
    private final NumberSetting strengthSlot = reg(new NumberSetting("Strength Hotbar Slot", 7, 1, 9, 1));
    private final NumberSetting speedSlot = reg(new NumberSetting("Speed Hotbar Slot", 8, 1, 9, 1));
    private final ModeSetting selection = reg(new ModeSetting("Source", "Inventory", "Inventory", "Hover"));

    public QuickPotion() {
        super("QuickPotion", "Refills separate strength and speed hotbar slots from the open inventory.", Category.PLAYER);
        registerBindSettings();
    }

    @Override public void onTick() {
        class_1703 handler = InventoryActions.handler(mc, true);
        if (handler == null || strengthSlot.vali() == speedSlot.vali()) return;
        int hovered = selection.is("Hover") ? InventoryActions.hoveredSource(mc, handler) : -1;
        if (selection.is("Hover") && hovered < 0) return;
        if (refill(handler, hovered, strengthSlot.vali() - 1, true)) return;
        refill(handler, hovered, speedSlot.vali() - 1, false);
    }

    private boolean refill(class_1703 handler, int hovered, int hotbar, boolean strength) {
        if (!InventoryActions.empty(InventoryActions.stack(handler, 36 + hotbar))) return false;
        int start = selection.is("Hover") ? hovered : 9;
        int end = selection.is("Hover") ? hovered : 35;
        for (int slot = start; slot <= end; slot++) {
            if (matches(InventoryActions.stack(handler, slot), strength)) {
                return InventoryActions.swap(mc, handler, slot, hotbar, true);
            }
        }
        return false;
    }

    private boolean matches(class_1799 stack, boolean strength) {
        if (InventoryActions.empty(stack) || !(stack.method_31574(class_1802.field_8574)
                || stack.method_31574(class_1802.field_8436))) return false;
        class_1844 contents = stack.method_58694(class_9334.field_49651);
        if (contents == null) return false;
        return strength
                ? contents.method_57401(class_1847.field_8978)
                    || contents.method_57401(class_1847.field_8965)
                    || contents.method_57401(class_1847.field_8993)
                : contents.method_57401(class_1847.field_9005)
                    || contents.method_57401(class_1847.field_8983)
                    || contents.method_57401(class_1847.field_8966);
    }
}
