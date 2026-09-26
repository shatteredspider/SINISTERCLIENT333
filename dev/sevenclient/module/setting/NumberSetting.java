package dev.sevenclient.module.setting;

public class NumberSetting extends Setting<Double> {
   private final double min;
   private final double max;
   private final double step;

   public NumberSetting(String name, double def, double min, double max, double step) {
      super(name, def);
      this.min = min;
      this.max = max;
      this.step = step;
   }

   public double min() {
      return this.min;
   }

   public double max() {
      return this.max;
   }

   public double step() {
      return this.step;
   }

   public double val() {
      return (Double)this.value;
   }

   public float valf() {
      return ((Double)this.value).floatValue();
   }

   public int vali() {
      return (int)Math.round((Double)this.value);
   }

   public void set(Double v) {
      double clamped = Math.max(this.min, Math.min(this.max, v));
      double snapped = (double)Math.round(clamped / this.step) * this.step;
      this.value = Math.max(this.min, Math.min(this.max, snapped));
   }

   public double normalized() {
      return ((Double)this.value - this.min) / (this.max - this.min);
   }

   public void setNormalized(double t) {
      this.set(this.min + (this.max - this.min) * Math.max((double)0.0F, Math.min((double)1.0F, t)));
   }

   public String serialize() {
      return Double.toString((Double)this.value);
   }

   public void deserialize(String raw) {
      try {
         this.set(Double.parseDouble(raw));
      } catch (NumberFormatException var3) {
      }

   }
}
