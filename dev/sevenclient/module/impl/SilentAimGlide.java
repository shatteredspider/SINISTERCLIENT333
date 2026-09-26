package dev.sevenclient.module.impl;

import java.util.Random;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_746;
import dev.sevenclient.util.RotationSync;

public final class SilentAimGlide {
    private SilentAimGlide() {}

    // Legacy callers receive a request, not a player-rotation mutation.
    public static void approach(class_746 player, double yawDelta, double pitchDelta, double fraction) {
        if (player == null || !Double.isFinite(yawDelta) || !Double.isFinite(pitchDelta)) return;
        double f = Double.isFinite(fraction) ? Math.max(0.05, Math.min(1.0, fraction)) : 0.05;
        RotationSync.requestSilent("SilentAim", RotationSync.PRIORITY_SILENT_AIM,
                (float) (player.method_36454() + yawDelta * f), true,
                (float) (player.method_36455() + pitchDelta * f));
    }

    public static double[] jitteredPoint(Random rng, class_243 eye, class_238 box, double budget, double jitter) {
        double minX = box.field_1323, maxX = box.field_1320;
        double minY = box.field_1322, maxY = box.field_1325;
        double minZ = box.field_1321, maxZ = box.field_1324;
        double x = clamp(eye.field_1352, minX, maxX);
        double y = clamp(eye.field_1351, minY, maxY);
        double z = clamp(eye.field_1350, minZ, maxZ);
        double j = Math.max(0.0, Math.min(jitter, 0.3));
        x = clamp(x + (rng.nextDouble() * 2.0 - 1.0) * j, minX, maxX);
        y = clamp(y + (rng.nextDouble() * 2.0 - 1.0) * j, minY, maxY);
        z = clamp(z + (rng.nextDouble() * 2.0 - 1.0) * j, minZ, maxZ);
        double dx = x - eye.field_1352, dy = y - eye.field_1351, dz = z - eye.field_1350;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > budget && dist > 1.0E-6) {
            double scale = budget / dist;
            x = eye.field_1352 + dx * scale;
            y = eye.field_1351 + dy * scale;
            z = eye.field_1350 + dz * scale;
        }
        return new double[]{x, y, z};
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
