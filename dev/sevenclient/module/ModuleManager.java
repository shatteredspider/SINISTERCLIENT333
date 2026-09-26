package dev.sevenclient.module;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.impl.AimAssist;
import dev.sevenclient.module.impl.AimAssist2;
import dev.sevenclient.module.impl.ArmorMacro;
import dev.sevenclient.module.impl.Audit;
import dev.sevenclient.module.impl.Chams;
import dev.sevenclient.module.impl.ClickGuiModule;
import dev.sevenclient.module.impl.Crosshair;
import dev.sevenclient.module.impl.Debug;
import dev.sevenclient.module.impl.EnemyMarker;
import dev.sevenclient.module.impl.FastXP;
import dev.sevenclient.module.impl.HitFlick;
import dev.sevenclient.module.impl.HitParticles;
import dev.sevenclient.module.impl.HitSwap;
import dev.sevenclient.module.impl.JumpReset;
import dev.sevenclient.module.impl.Nametags;
import dev.sevenclient.module.impl.NotificationsModule;
import dev.sevenclient.module.impl.PingSpoof;
import dev.sevenclient.module.impl.QuickPotion;
import dev.sevenclient.module.impl.STap;
import dev.sevenclient.module.impl.ShiftTap;
import dev.sevenclient.module.impl.SilentAim;
import dev.sevenclient.module.impl.SmpTotem;
import dev.sevenclient.module.impl.SocialList;
import dev.sevenclient.module.impl.TargetESP;
import dev.sevenclient.module.impl.Triggerbot;
import dev.sevenclient.module.impl.Watermark;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.Human;
import dev.sevenclient.util.PacketAudit;
import dev.sevenclient.util.SlotGuard;
import dev.sevenclient.util.SprintGuard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class ModuleManager {
   private final List<Module> modules = new ArrayList();
   private final Map<Module, Boolean> lastBindState = new HashMap();

   public ModuleManager() {
      this.add(new AimAssist());
      this.add(new AimAssist2());
      this.add(new HitSwap());
      this.add(new HitFlick());
      this.add(new Triggerbot());
      this.add(new SilentAim());
      this.add(new STap());
      this.add(new ShiftTap());
      this.add(new JumpReset());
      this.add(new SmpTotem());
      this.add(new QuickPotion());
      this.add(new ArmorMacro());
      this.add(new FastXP());
      this.add(new PingSpoof());
      this.add(new EnemyMarker());
      this.add(new SocialList());
      this.add(new TargetESP());
      this.add(new Crosshair());
      this.add(new NotificationsModule());
      this.add(new Watermark());
      this.add(new Chams());
      this.add(new Nametags());
      this.add(new HitParticles());
      this.add(new Debug());
      this.add(new Audit());
      this.add(new ClickGuiModule());
   }

   private void add(Module m) {
      this.modules.add(m);
      this.lastBindState.put(m, false);
   }

   public List<Module> all() {
      return this.modules;
   }

   public List<Module> byCategory(Category c) {
      List<Module> out = new ArrayList();

      for(Module m : this.modules) {
         if (m.category() == c) {
            out.add(m);
         }
      }

      return out;
   }

   public Module byName(String name) {
      for(Module m : this.modules) {
         if (m.name().equalsIgnoreCase(name)) {
            return m;
         }
      }

      return null;
   }

   public void pollBinds() {
      boolean screenOpen = class_310.method_1551().field_1755 != null;

      for(Module m : this.modules) {
         if (m.bind.isBound()) {
            boolean down = m.bind.down();
            boolean was = (Boolean)this.lastBindState.getOrDefault(m, false);
            this.lastBindState.put(m, down);
            if (!screenOpen) {
               if (m.mode() == BindMode.HOLD) {
                  if (down != m.isEnabled()) {
                     m.setEnabled(down);
                  }
               } else if (down && !was) {
                  m.toggle();
               }
            }
         }
      }

   }

   public void onTick() {
      ++Diagnostics.ticks;
      long now = System.currentTimeMillis();
      Human.tick();
      SlotGuard.observe(now);
      SprintGuard.tick(now);

      for(Module m : this.modules) {
         if (m.isEnabled()) {
            try {
               m.onTick();
            } catch (Throwable t) {
               this.crash(m, t);
            }
         }
      }

      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         PacketAudit.tickState(now, mc.field_1724.method_5624(), mc.field_1724.method_5715());
      }

   }

   public void onPostTick() {
      for(Module m : this.modules) {
         if (m.isEnabled()) {
            try {
               m.onPostTick();
            } catch (Throwable t) {
               this.crash(m, t);
            }
         }
      }

   }

   public void onFrame(float dt) {
      for(Module m : this.modules) {
         if (m.isEnabled()) {
            try {
               m.onFrame(dt);
            } catch (Throwable t) {
               this.crash(m, t);
            }
         }
      }

   }

   public void onHudRender(class_332 ctx) {
      for(Module m : this.modules) {
         if (m.isEnabled()) {
            try {
               m.onHudRender(ctx);
            } catch (Throwable t) {
               this.crash(m, t);
            }
         }
      }

   }

   public void onAttack(class_1297 target) {
      for(Module m : this.modules) {
         if (m.isEnabled()) {
            try {
               m.onAttack(target);
            } catch (Throwable t) {
               this.crash(m, t);
            }
         }
      }

   }

   private void crash(Module m, Throwable t) {
      SevenClient.LOG.error("Module {} threw, disabling.", m.name(), t);
      Diagnostics.error(m.name(), t);
      String var10000 = m.name();
      SevenClient.chat("§cError in §f" + var10000 + "§c, disabled: §7" + t.getClass().getSimpleName());
      m.setEnabled(false);
   }
}
