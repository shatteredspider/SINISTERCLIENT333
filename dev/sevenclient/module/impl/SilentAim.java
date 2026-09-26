package dev.sevenclient.module.impl;

import dev.sevenclient.module.Category;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.setting.BoolSetting;
import dev.sevenclient.module.setting.ModeSetting;
import dev.sevenclient.module.setting.NumberSetting;
import dev.sevenclient.module.setting.Setting;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.HumanDiag;
import dev.sevenclient.util.RotationSync;
import dev.sevenclient.util.Rotations;
import dev.sevenclient.util.TargetUtil;
import java.util.Optional;
import java.util.Random;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class SilentAim extends Module {
    private final ModeSetting technique = reg(new ModeSetting("Technique", "Camera", new String[]{"Camera", "Glide", "Packet"}));
    private final ModeSetting activation = reg(new ModeSetting("Activation", "On Attack", new String[]{"On Attack", "Always"}));
    private final NumberSetting range = reg(new NumberSetting("Range", 3.5, 1.0, 6.0, 0.1));
    private final NumberSetting maxOffset = reg(new NumberSetting("Max Offset Deg", 18.0, 1.0, 60.0, 1.0));
    private final NumberSetting inset = reg(new NumberSetting("Hitbox Inset", 0.08, 0.0, 0.4, 0.01));
    private final BoolSetting playersOnly = reg(new BoolSetting("Players Only", true));
    private final BoolSetting enemiesOnly = reg(new BoolSetting("Enemies Only", false));
    private final BoolSetting weaponsOnly = reg(new BoolSetting("Limit To Weapons", true));
    private final BoolSetting requireStill = reg(new BoolSetting("Require Still", true));
    private final NumberSetting cameraSpeed = reg(new NumberSetting("Camera Speed", 65.0, 5.0, 100.0, 1.0));
    private final NumberSetting recentre = reg(new NumberSetting("Recentre Rate", 6.0, 0.5, 20.0, 0.5));
    private final NumberSetting glideSpeed = reg(new NumberSetting("Glide Speed", 40.0, 1.0, 100.0, 1.0));
    private final NumberSetting glideJitter = reg(new NumberSetting("Glide Jitter", 0.08, 0.0, 0.3, 0.01));
    private final NumberSetting attackGate = reg(new NumberSetting("Attack Gate Deg", 3.0, 0.5, 10.0, 0.5));
    private final BoolSetting shieldCheck = reg(new BoolSetting("Shield Check", false));
    private static final String OWNER = "SilentAim";
    private final Random rng = new Random();
    private float glideYaw = Float.NaN;
    private float glidePitch = Float.NaN;

    public SilentAim() {
        super("SilentAim", "Sends aim with attacks while leaving the visible camera unchanged.", Category.COMBAT);
        requireStill.visibleWhen(() -> technique.is("Packet"));
        for (Setting<?> s : new Setting[]{cameraSpeed, recentre}) s.visibleWhen(() -> technique.is("Camera"));
        glideSpeed.visibleWhen(() -> technique.is("Glide"));
        glideJitter.visibleWhen(() -> technique.is("Glide"));
        attackGate.visibleWhen(() -> technique.is("Packet"));
        registerBindSettings();
    }

    @Override public void onEnable() { RotationSync.releaseSilent(OWNER); RotationSync.clearHidden(); }
    @Override public void onDisable() {
        RotationSync.releaseSilent(OWNER);
        RotationSync.clearHidden();
        glideYaw = glidePitch = Float.NaN;
        HumanDiag.silentAimActive = false;
        HumanDiag.silentAimState = "off";
    }

    @Override public void onTick() {
        RotationSync.releaseSilent(OWNER);
        RotationSync.clearHidden();
        if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1755 != null) { stand("no world or screen open"); return; }
        if (!active() || weaponsOnly.is() && !weaponOk()) { stand("inactive or no weapon"); return; }
        class_1309 target = TargetUtil.find(range.val(), enemiesOnly.is(), playersOnly.is());
        if (target == null) { stand("no target"); return; }
        if (shieldCheck.is() && TargetUtil.shieldUp(target, 5) && !TargetUtil.isAxe(mc.field_1724.method_6047())) {
            stand("shield up"); return;
        }
        float visibleYaw = mc.field_1724.method_36454();
        float visiblePitch = mc.field_1724.method_36455();
        class_243 eye = mc.field_1724.method_33571();
        class_238 box = target.method_5829();
        float[] window = Rotations.angularWindow(eye, box, inset.val(), visibleYaw);
        double targetYaw = class_3532.method_15350(visibleYaw, window[0], window[1]);
        double targetPitch = class_3532.method_15350(visiblePitch, window[2], window[3]);
        double dy = Rotations.wrap((float) (targetYaw - visibleYaw));
        double dp = targetPitch - visiblePitch;
        double offset = Math.hypot(dy, dp);
        if (!Double.isFinite(offset) || offset > maxOffset.val()) { stand("outside offset budget"); return; }
        if (technique.is("Packet") && requireStill.is() && !still()) { stand("packet requires stillness"); return; }
        if (technique.is("Packet") && offset > attackGate.val()) { stand("outside attack gate"); return; }
        float wantedYaw = (float) (visibleYaw + dy);
        float wantedPitch = class_3532.method_15363((float) (visiblePitch + dp), -90.0F, 90.0F);
        if (technique.is("Camera")) {
            float fraction = (float) class_3532.method_15350(cameraSpeed.val() / 100.0, 0.05, 1.0);
            wantedYaw = visibleYaw + (float) dy * fraction;
            wantedPitch = visiblePitch + (float) dp * fraction;
        } else if (technique.is("Glide")) {
            double[] p = SilentAimGlide.jitteredPoint(rng, eye, box, range.val() + 0.5, glideJitter.val());
            float[] r = Rotations.to(eye, new class_243(p[0], p[1], p[2]));
            float fraction = (float) class_3532.method_15350(glideSpeed.val() / 100.0, 0.05, 1.0);
            if (!Float.isFinite(glideYaw)) { glideYaw = visibleYaw; glidePitch = visiblePitch; }
            glideYaw += Rotations.wrap(r[0] - glideYaw) * fraction;
            glidePitch += (r[1] - glidePitch) * fraction;
            wantedYaw = glideYaw;
            wantedPitch = glidePitch;
        }
        // A server look is useful only when the entire ray intersects the selected hitbox.
        // If smoothing is still in flight, do not claim a silent hit or rotate a manual attack.
        if (!raycastHits(eye, wantedYaw, wantedPitch, box)) { stand("aim ray misses target"); return; }
        boolean granted = RotationSync.requestForTarget(OWNER, RotationSync.PRIORITY_SILENT_AIM, wantedYaw, true, wantedPitch, target);
        HumanDiag.silentAimActive = granted;
        HumanDiag.silentAimState = granted ? technique.get() + " request for " + target.method_5477().getString() : "yielded to " + RotationSync.silentOwner();
        if (granted) Diagnostics.aimTarget = target.method_5477().getString() + " (silent request)";
    }

    private boolean raycastHits(class_243 eye, float yaw, float pitch, class_238 box) {
        double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch), c = Math.cos(pr);
        class_243 direction = new class_243(-Math.sin(yr) * c, -Math.sin(pr), Math.cos(yr) * c);
        class_243 end = eye.method_1019(direction.method_1021(range.val() + 0.5));
        Optional<class_243> hit = box.method_1014(-inset.val() * 0.5).method_992(eye, end);
        return hit.isPresent();
    }

    private boolean still() {
        class_243 v = mc.field_1724.method_18798();
        return v.field_1352 * v.field_1352 + v.field_1350 * v.field_1350 < 0.0016
                && !mc.field_1690.field_1894.method_1434() && !mc.field_1690.field_1881.method_1434()
                && !mc.field_1690.field_1913.method_1434() && !mc.field_1690.field_1849.method_1434()
                && !mc.field_1690.field_1903.method_1434();
    }

    private void stand(String why) {
        RotationSync.releaseSilent(OWNER);
        HumanDiag.silentAimActive = false;
        HumanDiag.silentAimState = why;
    }

    private boolean active() {
        return activation.is("Always") || mc.field_1690.field_1886.method_1434() || bind.down();
    }

    private boolean weaponOk() {
        class_1799 held = mc.field_1724.method_6047();
        return TargetUtil.isSword(held) || TargetUtil.isAxe(held);
    }
}
