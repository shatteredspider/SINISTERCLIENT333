package dev.sevenclient.module;

public enum BindMode {
   TOGGLE("Toggle"),
   HOLD("Hold");

   private final String label;

   private BindMode(String label) {
      this.label = label;
   }

   public String label() {
      return this.label;
   }

   // $FF: synthetic method
   private static BindMode[] $values() {
      return new BindMode[]{TOGGLE, HOLD};
   }
}
