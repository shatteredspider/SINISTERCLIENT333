package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.ui.Anim;
import java.util.Objects;

public class ClickGuiModule extends Module {
   private final BoolSetting animations = (BoolSetting)this.reg(new BoolSetting("Animations", true));
   private final NumberSetting animSpeed = (NumberSetting)this.reg(new NumberSetting("Animation Speed", (double)100.0F, (double)25.0F, (double)300.0F, (double)5.0F));

   public ClickGuiModule() {
      super("ClickGUI", "Opens the 777 interface.", Category.RENDER);
      NumberSetting var10000 = this.animSpeed;
      BoolSetting var10001 = this.animations;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::is);
      this.bind.bind(344, false);
      this.bindMode.set("Toggle");
      this.registerBindSettings();
   }

   public void onEnable() {
      this.setEnabled(false);
      sync();
      if (mc.field_1687 != null) {
         SevenClient.openClickGui();
      }

   }

   public static void sync() {
      if (SevenClient.get() != null && SevenClient.get().modules != null) {
         Module m = SevenClient.get().modules.byName("ClickGUI");
         if (m instanceof ClickGuiModule) {
            ClickGuiModule g = (ClickGuiModule)m;
            Anim.enabled = g.animations.is();
            Anim.speedScale = (float)(g.animSpeed.val() / (double)100.0F);
         }

      }
   }
}
