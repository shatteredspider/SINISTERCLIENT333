package dev.sevenclient.module.impl;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.KeySetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.ui.Render2D;
import dev.sevenclient.ui.UiFont;
import dev.sevenclient.util.EnemyManager;
import dev.sevenclient.util.FriendManager;
import dev.sevenclient.util.Notifications;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_332;
import net.minecraft.class_3966;

public class SocialList extends Module {
   private final ModeSetting corner = (ModeSetting)this.reg(new ModeSetting("Corner", "Left", new String[]{"Left", "Right"}));
   private final BoolSetting showFriends = (BoolSetting)this.reg(new BoolSetting("Show Friends", true));
   private final BoolSetting showEnemies = (BoolSetting)this.reg(new BoolSetting("Show Enemies", true));
   private final BoolSetting onlyInWorld = (BoolSetting)this.reg(new BoolSetting("Only Nearby", false));
   private final NumberSetting offsetY = (NumberSetting)this.reg(new NumberSetting("Y Offset", (double)30.0F, (double)0.0F, (double)300.0F, (double)2.0F));
   public final KeySetting friendBind = (KeySetting)this.reg(new KeySetting("Friend Bind"));
   private boolean lastDown = false;

   public SocialList() {
      super("SocialList", "Friend and enemy lists, with a bind to add players.", Category.PLAYER);
      this.registerBindSettings();
   }

   public void onTick() {
      if (mc.field_1724 != null) {
         boolean down = this.friendBind.isBound() && this.friendBind.down();
         if (down && !this.lastDown && mc.field_1755 == null) {
            class_1657 p = this.lookingAt();
            if (p != null) {
               boolean added = SevenClient.get().friends.toggle(p);
               String n = p.method_5477().getString();
               Notifications.push(added ? "Friend added: " + n : "Friend removed: " + n, added ? Notifications.Kind.ON : Notifications.Kind.OFF);
            } else {
               Notifications.warn("No player under crosshair");
            }
         }

         this.lastDown = down;
      }

   }

   private class_1657 lookingAt() {
      class_239 var3 = mc.field_1765;
      if (var3 instanceof class_3966 ehr) {
         class_1297 var4 = ehr.method_17782();
         if (var4 instanceof class_1657 p) {
            if (p != mc.field_1724 && !p.method_7325()) {
               return p;
            }
         }
      }

      return null;
   }

   public void onHudRender(class_332 ctx) {
      if (mc.field_1724 != null && !mc.field_1690.field_1842 && mc.field_1755 == null) {
         List<String> lines = new ArrayList();
         FriendManager friends = SevenClient.get().friends;
         EnemyManager enemies = SevenClient.get().enemies;
         if (this.showFriends.is() && friends.size() > 0) {
            lines.add("FRIENDS " + friends.size());
            synchronized(friends.names()) {
               for(String n : friends.names()) {
                  if (!this.onlyInWorld.is() || this.inWorld(n)) {
                     lines.add("  " + n + (this.inWorld(n) ? "  *" : ""));
                  }
               }
            }
         }

         if (this.showEnemies.is() && enemies.size() > 0) {
            if (!lines.isEmpty()) {
               lines.add("");
            }

            lines.add("ENEMIES " + enemies.size());
            synchronized(enemies.names()) {
               for(String nx : enemies.names()) {
                  if (!this.onlyInWorld.is() || this.inWorld(nx)) {
                     lines.add("  " + nx + (this.inWorld(nx) ? "  *" : ""));
                  }
               }
            }
         }

         if (!lines.isEmpty()) {
            int w = 0;

            for(String s : lines) {
               w = Math.max(w, UiFont.width(s));
            }

            int lh = UiFont.height() + 2;
            float boxW = (float)(w + 18);
            float boxH = (float)(lines.size() * lh + 12);
            float x = this.corner.is("Right") ? (float)mc.method_22683().method_4486() - boxW - 6.0F : 6.0F;
            float y = (float)this.offsetY.val();
            Render2D.shadow(ctx, x, y, boxW, boxH, 5.0F, 5, -1442840576);
            Render2D.panel(ctx, x, y, boxW, boxH, 5.0F, -16250872, -14474461);
            float ty = y + 6.0F;

            for(String s : lines) {
               int col = !s.startsWith("FRIENDS") && !s.startsWith("ENEMIES") ? -6381922 : -328966;
               if (s.endsWith("*")) {
                  col = -1;
               }

               UiFont.draw(ctx, s, x + 9.0F, ty, col);
               ty += (float)lh;
            }
         }
      }

   }

   private boolean inWorld(String name) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         for(class_1297 e : mc.field_1687.method_18112()) {
            if (e instanceof class_1657) {
               class_1657 p = (class_1657)e;
               if (p != mc.field_1724 && p.method_5477().getString().equalsIgnoreCase(name)) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}
