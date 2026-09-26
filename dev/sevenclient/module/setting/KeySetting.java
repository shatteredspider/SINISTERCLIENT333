package dev.sevenclient.module.setting;

import dev.sevenclient.util.InputUtil2;

public class KeySetting extends Setting<Integer> {
   private boolean mouse;

   public KeySetting(String name) {
      super(name, -1);
   }

   public KeySetting(String name, int key) {
      super(name, key);
   }

   public int code() {
      return (Integer)this.value;
   }

   public boolean isMouse() {
      return this.mouse;
   }

   public void bind(int code, boolean isMouse) {
      this.value = code;
      this.mouse = isMouse;
   }

   public void clear() {
      this.value = -1;
      this.mouse = false;
   }

   public boolean isBound() {
      return (Integer)this.value != -1;
   }

   public boolean down() {
      return this.isBound() && InputUtil2.isDown((Integer)this.value, this.mouse);
   }

   public String label() {
      if (!this.isBound()) {
         return "NONE";
      } else {
         return this.mouse ? InputUtil2.mouseName((Integer)this.value) : InputUtil2.keyName((Integer)this.value);
      }
   }

   public String serialize() {
      String var10000 = this.mouse ? "M:" : "K:";
      return var10000 + String.valueOf(this.value);
   }

   public void deserialize(String raw) {
      try {
         if (raw.startsWith("M:")) {
            this.bind(Integer.parseInt(raw.substring(2)), true);
         } else if (raw.startsWith("K:")) {
            this.bind(Integer.parseInt(raw.substring(2)), false);
         }
      } catch (NumberFormatException var3) {
      }

   }
}
