package dev.sevenclient.module.impl;

import net.minecraft.class_304;

public class STap extends SprintReset {
   public STap() {
      super("S-Tap", "Taps backward on hit to reset sprint knockback.");
      this.registerBindSettings();
   }

   protected class_304 binding() {
      return mc.field_1690.field_1881;
   }
}
