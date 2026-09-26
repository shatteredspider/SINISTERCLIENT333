package dev.sevenclient.module.setting;

public class BoolSetting extends Setting<Boolean> {
   public BoolSetting(String name, boolean def) {
      super(name, def);
   }

   public boolean is() {
      return (Boolean)this.value;
   }

   public void toggle() {
      this.value = !(Boolean)this.value;
   }

   public String serialize() {
      return Boolean.toString((Boolean)this.value);
   }

   public void deserialize(String raw) {
      this.value = Boolean.parseBoolean(raw);
   }
}
