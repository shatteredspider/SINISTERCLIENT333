package dev.sevenclient.module.impl;

import net.minecraft.class_304;

public class ShiftTap extends SprintReset {
   public ShiftTap() {
      super("Shift-Tap", "Taps sneak on hit to reset sprint knockback.");
      this.registerBindSettings();
   }

   protected class_304 binding() {
      return mc.field_1690.field_1832;
   }
}
