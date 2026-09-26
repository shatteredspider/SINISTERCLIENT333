package dev.sevenclient.module;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.Notifications;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_332;

public abstract class Module {
   protected static final class_310 mc = class_310.method_1551();
   private final String name;
   private final String description;
   private final Category category;
   private final List<Setting<?>> settings = new ArrayList();
   public final KeySetting bind = new KeySetting("Bind");
   public final ModeSetting bindMode = new ModeSetting("Bind Mode", "Toggle", new String[]{"Toggle", "Hold"});
   private boolean enabled;
   public static boolean notifyToggles = true;

   protected Module(String name, String description, Category category) {
      this.name = name;
      this.description = description;
      this.category = category;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onTick() {
   }

   public void onPostTick() {
   }

   public void onFrame(float dt) {
   }

   public void onHudRender(class_332 ctx) {
   }

   public void onAttack(class_1297 target) {
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean state) {
      if (state != this.enabled) {
         this.enabled = state;

         try {
            if (state) {
               this.onEnable();
            } else {
               this.onDisable();
            }
         } catch (Throwable var3) {
            SevenClient.LOG.error("Module {} threw on state change", this.name, var3);
            Diagnostics.error(this.name, var3);
         }

         if (notifyToggles) {
            Notifications.moduleToggled(this.name, state);
         }
      }

   }

   public void toggle() {
      this.setEnabled(!this.enabled);
   }

   public BindMode mode() {
      return this.bindMode.is("Hold") ? BindMode.HOLD : BindMode.TOGGLE;
   }

   protected <T extends Setting<?>> T reg(T setting) {
      this.settings.add(setting);
      return setting;
   }

   protected void registerBindSettings() {
      this.settings.add(this.bindMode);
      this.settings.add(this.bind);
   }

   public List<Setting<?>> settings() {
      return this.settings;
   }

   public String name() {
      return this.name;
   }

   public String description() {
      return this.description;
   }

   public Category category() {
      return this.category;
   }
}
