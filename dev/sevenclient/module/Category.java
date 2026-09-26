package dev.sevenclient.module;

public enum Category {
   COMBAT("Combat"),
   MOVEMENT("Movement"),
   PLAYER("Player"),
   RENDER("Render"),
   VISUALS("Visuals");

   private final String label;

   private Category(String label) {
      this.label = label;
   }

   public String label() {
      return this.label;
   }

   // $FF: synthetic method
   private static Category[] $values() {
      return new Category[]{COMBAT, MOVEMENT, PLAYER, RENDER, VISUALS};
   }
}
