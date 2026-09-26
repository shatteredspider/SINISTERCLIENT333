package dev.sevenclient.module.setting;

public class StringSetting extends Setting<String> {
   private final int maxLength;

   public StringSetting(String name, String def) {
      this(name, def, 64);
   }

   public StringSetting(String name, String def, int maxLength) {
      super(name, def);
      this.maxLength = maxLength;
   }

   public int maxLength() {
      return this.maxLength;
   }

   public void append(char c) {
      if (((String)this.value).length() < this.maxLength) {
         String var10001 = this.value;
         this.value = var10001 + c;
      }

   }

   public void backspace() {
      if (!((String)this.value).isEmpty()) {
         this.value = ((String)this.value).substring(0, ((String)this.value).length() - 1);
      }

   }

   public String serialize() {
      return this.value;
   }

   public void deserialize(String raw) {
      this.value = raw == null ? "" : raw;
   }
}
