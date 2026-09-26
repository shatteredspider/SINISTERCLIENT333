package dev.sevenclient.util;

import net.minecraft.class_1297;
import net.minecraft.class_2831;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

/** Rotation ownership is limited to one client tick. Never leave a player rotated after a hook. */
public final class RotationSync {
    public static final int PRIORITY_SILENT_AIM = 10;
    public static final int PRIORITY_HIT_FLICK = 20;
    public static volatile boolean dirty;
    public static volatile boolean enabled = true;
    // Kept for callers that inspect the old camera state. No entity rotation is hidden by a render hook.
    public static volatile boolean hideRotation;
    public static volatile float maxHiddenYaw = 30.0F;
    public static volatile float maxHiddenPitch = 20.0F;
    public static volatile long movementHooks;
    public static volatile long attackHooks;
    public static volatile long movementRotations;
    public static volatile long attackRotations;
    public static volatile long rejectedRotations;

    private static long requestTick = -1L;
    private static String owner;
    private static int priority = Integer.MIN_VALUE;
    private static float yaw;
    private static float pitch;
    private static boolean usePitch;
    private static class_1297 attackTarget;
    private static class_746 heldPlayer;
    private static float heldYaw;
    private static float heldPitch;
    private static float heldHeadYaw;
    private static boolean held;
    private static double lastSentYaw = Double.NaN;
    private static double lastSentPitch = Double.NaN;
    private static volatile float hiddenYaw;
    private static volatile float hiddenPitch;

    private RotationSync() {}

    private static void rollTick() {
        if (requestTick != Diagnostics.ticks) {
            requestTick = Diagnostics.ticks;
            owner = null;
            priority = Integer.MIN_VALUE;
            attackTarget = null;
        }
    }

    public static synchronized boolean requestSilent(String who, int rank, float requestedYaw) {
        return requestSilent(who, rank, requestedYaw, false, 0.0F);
    }

    public static synchronized boolean requestSilent(String who, int rank, float requestedYaw, boolean pitchEnabled, float requestedPitch) {
        return requestForTarget(who, rank, requestedYaw, pitchEnabled, requestedPitch, null);
    }

    public static synchronized boolean requestForTarget(String who, int rank, float requestedYaw, boolean pitchEnabled,
                                                         float requestedPitch, class_1297 target) {
        rollTick();
        if (who == null || !Float.isFinite(requestedYaw) || (pitchEnabled && !Float.isFinite(requestedPitch))
                || owner != null && !owner.equals(who) && rank <= priority) {
            rejectedRotations++;
            return false;
        }
        owner = who;
        priority = rank;
        yaw = class_3532.method_15393(requestedYaw);
        usePitch = pitchEnabled;
        pitch = class_3532.method_15363(requestedPitch, -90.0F, 90.0F);
        attackTarget = target;
        return true;
    }

    public static synchronized void releaseSilent(String who) {
        if (who != null && who.equals(owner)) {
            owner = null;
            attackTarget = null;
        }
    }

    public static synchronized boolean silentActive() {
        return owner != null && requestTick == Diagnostics.ticks;
    }

    public static synchronized String silentOwner() {
        return silentActive() ? owner : "-";
    }

    private static float[] rotation(class_746 player) {
        if (player == null || !enabled || !silentActive()) return null;
        double step = Rotations.gcd();
        if (!Double.isFinite(step) || step <= 0.0) {
            rejectedRotations++;
            return null;
        }
        double baseYaw = Double.isFinite(lastSentYaw) ? lastSentYaw : player.method_36454();
        double basePitch = Double.isFinite(lastSentPitch) ? lastSentPitch : player.method_36455();
        float outYaw = (float) (baseYaw + Math.round(class_3532.method_15393(yaw - (float) baseYaw) / step) * step);
        float outPitch = usePitch ? class_3532.method_15363((float) (basePitch + Math.round((pitch - basePitch) / step) * step), -90.0F, 90.0F)
                                  : player.method_36455();
        return Float.isFinite(outYaw) && Float.isFinite(outPitch) ? new float[]{outYaw, outPitch} : null;
    }

    public static synchronized void beginSilent(class_746 player) {
        movementHooks++;
        // A prior interrupted hook must not leave a rotated local entity.
        endSilent(heldPlayer);
        float[] r = rotation(player);
        if (r == null) return;
        heldPlayer = player;
        heldYaw = player.method_36454();
        heldPitch = player.method_36455();
        heldHeadYaw = player.method_5847();
        held = true;
        player.method_36456(r[0]);
        player.method_5847(r[0]);
        player.method_36457(r[1]);
        movementRotations++;
    }

    public static synchronized void endSilent(class_746 player) {
        if (!held) return;
        class_746 restore = heldPlayer;
        held = false;
        heldPlayer = null;
        if (restore != null) {
            restore.method_36456(heldYaw);
            restore.method_5847(heldHeadYaw);
            restore.method_36457(heldPitch);
        }
    }

    public static synchronized void snapBeforeSend(class_746 player) {
        if (player == null) return;
        HumanDiag.gcdSyncCalls++;
        HumanDiag.lastTickYaw = HumanDiag.tickYawSpent;
        HumanDiag.maxTickYaw = Math.max(HumanDiag.maxTickYaw, HumanDiag.tickYawSpent);
        HumanDiag.tickYawSpent = 0.0;
        // Quantize a request before the movement packet, not the visible player every tick.
        if (held) {
            float[] r = rotation(player);
            if (r != null) {
                player.method_36456(r[0]);
                player.method_5847(r[0]);
                player.method_36457(r[1]);
                lastSentYaw = r[0];
                lastSentPitch = r[1];
                HumanDiag.gcdSyncApplied++;
            }
        } else {
            lastSentYaw = player.method_36454();
            lastSentPitch = player.method_36455();
        }
        PacketAudit.onFlyingRotation(player.method_36454(), player.method_36455());
        dirty = false;
    }

    public static synchronized void quantiseNow(class_746 player) {
        // Called by the use-item hook. A use-item action must not rotate the visible camera.
        if (player != null) HumanDiag.useItemSnaps++;
    }

    /** Called at the HEAD of the vanilla attack method, before its attack packet. */
    public static synchronized void beforeAttack(class_746 player, class_1297 target) {
        attackHooks++;
        if (player == null || target == null || class_310.method_1551().field_1724 != player
                || class_310.method_1551().field_1687 == null || player.field_3944 == null
                || !silentActive() || attackTarget != target) return;
        float[] r = rotation(player);
        if (r == null) return;
        // This is queued on the same network handler before vanilla queues its attack.
        // No local look mutation: manual aim and rendering stay intact.
        player.field_3944.method_52787(new class_2831(r[0], r[1], player.method_24828(), player.field_5976));
        lastSentYaw = r[0];
        lastSentPitch = r[1];
        attackRotations++;
    }

    public static synchronized void reset() {
        endSilent(heldPlayer);
        owner = null;
        attackTarget = null;
        requestTick = -1L;
        lastSentYaw = Double.NaN;
        lastSentPitch = Double.NaN;
        dirty = false;
        clearHidden();
    }

    public static void applyHidden(class_746 player, double dYaw, double dPitch) {
        // Compatibility for older callers. Do not write to player rotation here.
        if (player != null && Double.isFinite(dYaw) && Double.isFinite(dPitch)) {
            hiddenYaw = (float) dYaw;
            hiddenPitch = (float) dPitch;
        }
    }

    public static void decayHidden(float dt, double rate) {
        clearHidden();
    }

    public static void clearHidden() {
        hiddenYaw = 0.0F;
        hiddenPitch = 0.0F;
        hideRotation = false;
    }

    public static boolean cameraHidden() { return false; }
    public static float cameraYaw(float realYaw) { return realYaw; }
    public static float cameraPitch(float realPitch) { return realPitch; }
    public static float hiddenYaw() { return hiddenYaw; }
    public static float hiddenPitch() { return hiddenPitch; }
}
