package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.EnemyManager;
import dev.sevenclient.util.InputUtil2;
import dev.sevenclient.util.TargetPriority;
import dev.sevenclient.util.TargetUtil;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;

public class EnemyMarker extends Module {
   private final NumberSetting reach = (NumberSetting)this.reg(new NumberSetting("Pick Range", 24.0, 4.0, 128.0, 1.0));
   private final BoolSetting feedback = (BoolSetting)this.reg(new BoolSetting("Chat Feedback", true));
   private final KeySetting clearPriorityKey = (KeySetting)this.reg(new KeySetting("Clear Priority Key", 259));
   private boolean lastDown;

   public EnemyMarker() {
      super("EnemyMarker", "Middle-click players to toggle them as enemies.", Category.PLAYER);
      this.registerBindSettings();
   }

   public void onTick() {
      TargetPriority.setClearKeyCode(this.clearPriorityKey.isMouse() ? -1 : this.clearPriorityKey.code());
      TargetPriority.onWorldTick();
      if (mc.field_1724 == null || mc.field_1687 == null) {
         this.lastDown = false;
         return;
      }
      if (mc.field_1755 != null) {
         this.lastDown = false;
         return;
      }
      boolean down = InputUtil2.isDown(2, true);
      if (down && !this.lastDown) {
         this.pick();
      }
      this.lastDown = down;
   }

   private void pick() {
      class_1297 hit = TargetUtil.crosshairEntity();
      if (hit == null) {
         hit = this.raycast();
      }
      if (hit instanceof class_1657 player && player != mc.field_1724) {
         EnemyManager enemies = SevenClient.get().enemies;
         boolean added = enemies.toggle(player);
         if (this.feedback.is()) {
            String name = player.method_5477().getString();
            SevenClient.chat((added ? "§aAdded §f" : "§cRemoved §f") + name + " §7as an enemy.");
         }
      }
   }

   private class_1297 raycast() {
      class_243 eye = mc.field_1724.method_33571();
      class_243 dir = mc.field_1724.method_5828(1.0F);
      double max = this.reach.val();
      class_243 end = eye.method_1019(dir.method_1021(max));
      class_1297 best = null;
      double bestDist = Double.MAX_VALUE;
      for (class_1297 e : mc.field_1687.method_18112()) {
         if (e instanceof class_1657 && e != mc.field_1724 && !e.method_7325() && e.method_5805()) {
            Optional<class_243> hit = e.method_5829().method_1014(0.15).method_992(eye, end);
            if (!hit.isEmpty()) {
               double d = eye.method_1025(hit.get());
               if (d < bestDist) {
                  bestDist = d;
                  best = e;
               }
            }
         }
      }
      return best;
   }
}
