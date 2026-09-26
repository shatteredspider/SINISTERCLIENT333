package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.HumanRandom;
import dev.sevenclient.util.RotationSync;
import dev.sevenclient.util.Rotations;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_3532;
import net.minecraft.class_3966;

public class HitFlick extends Module {
    private final NumberSetting angle = reg(new NumberSetting("Angle", 90.0, 0.0, 360.0, 1.0));
    private final NumberSetting chance = reg(new NumberSetting("Chance", 100.0, 0.0, 100.0, 1.0));
    private final NumberSetting flickDelay = reg(new NumberSetting("Flick Delay MS", 250.0, 0.0, 2000.0, 25.0));
    private final BoolSetting randomizeOffset = reg(new BoolSetting("Randomize Offset", false));
    private final NumberSetting randomizeRange = reg(new NumberSetting("Offset Range", 20.0, 0.0, 180.0, 1.0));
    private final BoolSetting strafeInvert = reg(new BoolSetting("Strafe Invert", false));
    private final BoolSetting selectHits = reg(new BoolSetting("Select Hits", true));
    private final NumberSetting flickSpeed = reg(new NumberSetting("Flick Speed", 48.0, 4.0, 200.0, 1.0));
    private final NumberSetting returnSpeed = reg(new NumberSetting("Return Speed", 30.0, 4.0, 200.0, 1.0));
    private final NumberSetting maxTicks = reg(new NumberSetting("Max Flick Ticks", 4.0, 1.0, 10.0, 1.0));
    private final BoolSetting returnAfter = reg(new BoolSetting("Return After", true));
    private static final String OWNER = "HitFlick";
    private long lastFlick;
    private boolean rolled;
    private int ticks;
    private float serverYaw = Float.NaN;
    private class_1657 selected;

    public HitFlick() {
        super("HitFlick", "Offsets the server yaw for a selected sprint attack without moving the camera.", Category.COMBAT);
        randomizeRange.visibleWhen(randomizeOffset::is);
        returnSpeed.visibleWhen(returnAfter::is);
        registerBindSettings();
    }

    @Override public void onDisable() { reset(); }

    private void reset() {
        RotationSync.releaseSilent(OWNER);
        rolled = false;
        selected = null;
        serverYaw = Float.NaN;
        ticks = 0;
    }

    @Override public void onTick() {
        RotationSync.releaseSilent(OWNER);
        if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1755 != null) { reset(); return; }
        if (!mc.field_1690.field_1886.method_1434() || !mc.field_1724.method_5624()) { reset(); return; }
        class_1657 target = crosshairPlayer();
        if (target == null) { reset(); return; }
        if (selected != target) { reset(); selected = target; }
        if (System.currentTimeMillis() - lastFlick < (long) flickDelay.val()) return;
        if (selectHits.is() && (target.field_6235 > 0 || mc.field_1724.method_7261(0.0F) < 0.88F)) return;
        if (!rolled) {
            rolled = true;
            if (!HumanRandom.chance(chance.val())) return;
            double bearing = angle.val();
            if (randomizeOffset.is()) bearing += HumanRandom.uniform(-randomizeRange.val() / 2.0, randomizeRange.val() / 2.0);
            if (strafeInvert.is() && mc.field_1690.field_1849.method_1434()) bearing = -bearing;
            float toTarget = Rotations.to(mc.field_1724.method_33571(), target.method_33571())[0];
            serverYaw = class_3532.method_15393((float) (toTarget + bearing));
            ticks = 0;
            Diagnostics.hitFlickArmed++;
        }
        if (!Float.isFinite(serverYaw)) return;
        // Reach the intended yaw in bounded tick steps. It is never written to the local view.
        if (++ticks > (int) maxTicks.val()) { reset(); return; }
        float current = mc.field_1724.method_36454();
        float fraction = (float) Math.min(1.0, flickSpeed.val() / 100.0);
        float requested = current + class_3532.method_15393(serverYaw - current) * fraction;
        RotationSync.requestForTarget(OWNER, RotationSync.PRIORITY_HIT_FLICK, requested, true,
                mc.field_1724.method_36455(), target);
    }

    @Override public void onAttack(class_1297 attacked) {
        if (attacked == selected && Float.isFinite(serverYaw)) {
            lastFlick = System.currentTimeMillis();
            reset();
        }
    }

    private class_1657 crosshairPlayer() {
        class_239 hit = mc.field_1765;
        if (hit instanceof class_3966 entityHit) {
            class_1297 entity = entityHit.method_17782();
            if (entity instanceof class_1657 player && player != mc.field_1724 && !player.method_7325()) return player;
        }
        return null;
    }
}
