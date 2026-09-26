package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.Notifications;
import net.minecraft.class_332;

public class NotificationsModule extends Module {
   private final BoolSetting sound = (BoolSetting)this.reg(new BoolSetting("Sound", true));
   private final NumberSetting volume = (NumberSetting)this.reg(new NumberSetting("Volume", (double)100.0F, (double)0.0F, (double)100.0F, (double)5.0F));
   private final NumberSetting lifetime = (NumberSetting)this.reg(new NumberSetting("Lifetime MS", (double)2200.0F, (double)600.0F, (double)6000.0F, (double)100.0F));
   private final NumberSetting maxVisible = (NumberSetting)this.reg(new NumberSetting("Max Visible", (double)5.0F, (double)1.0F, (double)10.0F, (double)1.0F));
   private final NumberSetting topOffset = (NumberSetting)this.reg(new NumberSetting("Top Offset", (double)6.0F, (double)0.0F, (double)200.0F, (double)2.0F));
   private final BoolSetting showOverGui = (BoolSetting)this.reg(new BoolSetting("Show Over GUI", true));
   private long lastFrame = 0L;

   public NotificationsModule() {
      super("Notifications", "Toast popups for module toggles and events.", Category.RENDER);
      this.setEnabled(true);
      this.registerBindSettings();
   }

   public void onEnable() {
      Notifications.enabled = true;
   }

   public void onDisable() {
      Notifications.enabled = false;
      Notifications.clear();
   }

   public void onTick() {
      Notifications.enabled = true;
      Notifications.playSound = this.sound.is();
      Notifications.soundVolume = (float)(this.volume.val() / (double)100.0F);
      Notifications.lifetimeMs = (int)this.lifetime.val();
      Notifications.maxVisible = (int)this.maxVisible.val();
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1755 == null || this.showOverGui.is()) {
         long now = System.nanoTime();
         float dt = this.lastFrame == 0L ? 0.016666668F : (float)((double)(now - this.lastFrame) / (double)1.0E9F);
         this.lastFrame = now;
         if (dt > 0.25F) {
            dt = 0.25F;
         }

         Notifications.render(ctx, dt, mc.method_22683().method_4486(), (float)this.topOffset.val());
      }

   }
}
