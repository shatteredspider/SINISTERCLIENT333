package dev.sevenclient.util;

import java.util.function.Predicate;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_310;

public final class SlotUtil {
   private SlotUtil() {
   }

   public static int selected() {
      class_310 mc = class_310.method_1551();
      return mc.field_1724 == null ? 0 : mc.field_1724.method_31548().method_67532();
   }

   public static void select(int slot) {
      if (slot >= 0 && slot <= 8) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null) {
            class_1661 inv = mc.field_1724.method_31548();
            if (inv.method_67532() != slot) {
               inv.method_61496(slot);
            }
         }
      }

   }

   public static int findHotbar(Predicate<class_1799> test) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for(int i = 0; i < 9; ++i) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack != null && !stack.method_7960() && test.test(stack)) {
               return i;
            }
         }

         return -1;
      }
   }
}
