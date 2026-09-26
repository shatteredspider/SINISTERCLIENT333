package dev.sevenclient.module.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
   private final List<String> options;

   public ModeSetting(String name, String def, String... options) {
      super(name, def);
      this.options = Arrays.asList(options);
   }

   public List<String> options() {
      return this.options;
   }

   public boolean is(String mode) {
      return ((String)this.value).equalsIgnoreCase(mode);
   }

   public int index() {
      int i = this.options.indexOf(this.value);
      return i < 0 ? 0 : i;
   }

   public void cycle(int dir) {
      int i = (this.index() + dir) % this.options.size();
      if (i < 0) {
         i += this.options.size();
      }

      this.value = (String)this.options.get(i);
   }

   public String serialize() {
      return this.value;
   }

   public void deserialize(String raw) {
      if (this.options.contains(raw)) {
         this.value = raw;
      }

   }
}
