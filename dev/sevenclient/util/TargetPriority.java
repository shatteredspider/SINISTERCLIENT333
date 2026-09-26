package dev.sevenclient.util;

import dev.sevenclient.SevenClient;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;

/** Client-observable combat events only; call from the client thread. */
public final class TargetPriority {
    private static final long WINDOW_NANOS = 120_000_000_000L;
    private static final Map<UUID, ArrayDeque<Event>> EVENTS = new HashMap<>();
    private static Object world;
    private static int clearKeyCode = 259; // GLFW_KEY_BACKSPACE; 261 is GLFW_KEY_DELETE.
    private static boolean clearKeyDown;

    private TargetPriority() {
    }

    /** A client-observable outgoing attack on a player adds one point. */
    public static void onOutgoingAttack(class_1297 entity) {
        if (entity instanceof class_1657 player) {
            add(player, 2);
        }
    }

    /** An incoming hit adds half a point only when its player attacker is identified. */
    public static void onIncomingPlayerHit(class_1657 attacker) {
        add(attacker, 1);
    }

    /** Set the clear key from the registered key setting; -1 disables it. */
    public static void setClearKeyCode(int code) {
        if (clearKeyCode != code) {
            clearKeyDown = false;
            clearKeyCode = code;
        }
    }

    public static void onWorldReset() {
        EVENTS.clear();
        world = null;
        clearKeyDown = false;
    }

    /** Call each client tick, including ticks with no player, to handle world and key changes. */
    public static void onWorldTick() {
        class_310 mc = class_310.method_1551();
        if (world != mc.field_1687) {
            EVENTS.clear();
            world = mc.field_1687;
            clearKeyDown = false;
        }
        prune(System.nanoTime());
        boolean down = world != null && mc.field_1724 != null && mc.field_1755 == null
                && clearKeyCode >= 0 && InputUtil2.isDown(clearKeyCode, false);
        if (down && !clearKeyDown) {
            class_1297 hit = TargetUtil.crosshairEntity();
            if (hit instanceof class_1657 player) {
                EVENTS.remove(player.method_5667());
            }
        }
        clearKeyDown = down;
    }

    /** Highest rolling score among eligible players. Equal scores prefer the nearer player. */
    public static class_1657 winner(double range, boolean enemiesOnly) {
        class_310 mc = class_310.method_1551();
        syncWorld(mc);
        if (mc.field_1724 == null || world == null) {
            return null;
        }
        prune(System.nanoTime());
        class_1657 best = null;
        int bestScore = 0;
        double bestDistance = Double.MAX_VALUE;
        for (class_1297 entity : mc.field_1687.method_18112()) {
            if (!(entity instanceof class_1657 player) || !eligible(player, range, enemiesOnly)) {
                continue;
            }
            ArrayDeque<Event> events = EVENTS.get(player.method_5667());
            if (events == null) {
                continue;
            }
            int score = 0;
            for (Event event : events) {
                score += event.halfPoints;
            }
            double distance = mc.field_1724.method_33571().method_1022(player.method_33571());
            if (score > bestScore || score == bestScore && distance < bestDistance) {
                best = player;
                bestScore = score;
                bestDistance = distance;
            }
        }
        return best;
    }

    /** True if no scored eligible player exists, or this is the priority winner. */
    public static boolean isAllowed(class_1297 candidate, double range, boolean enemiesOnly) {
        class_1657 preferred = winner(range, enemiesOnly);
        return preferred == null || candidate != null && preferred.method_5667().equals(candidate.method_5667());
    }

    private static boolean eligible(class_1657 player, double range, boolean enemiesOnly) {
        class_310 mc = class_310.method_1551();
        return player != mc.field_1724 && player.method_5805() && !player.method_31481()
                && !player.method_7325() && !SevenClient.get().friends.is(player)
                && (!enemiesOnly || SevenClient.get().enemies.is(player))
                && mc.field_1724.method_33571().method_1022(player.method_33571()) <= range;
    }

    private static void add(class_1657 player, int halfPoints) {
        class_310 mc = class_310.method_1551();
        syncWorld(mc);
        if (player == null || mc.field_1724 == null || world == null
                || player == mc.field_1724 || !eligible(player, Double.MAX_VALUE, false)) {
            return;
        }
        long now = System.nanoTime();
        prune(now);
        EVENTS.computeIfAbsent(player.method_5667(), id -> new ArrayDeque<>())
                .addLast(new Event(now, halfPoints));
    }

    private static void syncWorld(class_310 mc) {
        if (world != mc.field_1687) {
            EVENTS.clear();
            world = mc.field_1687;
            clearKeyDown = false;
        }
    }

    private static void prune(long now) {
        EVENTS.values().removeIf(events -> {
            while (!events.isEmpty() && now - events.peekFirst().time >= WINDOW_NANOS) {
                events.removeFirst();
            }
            return events.isEmpty();
        });
    }

    private static final class Event {
        final long time;
        final int halfPoints;

        Event(long time, int halfPoints) {
            this.time = time;
            this.halfPoints = halfPoints;
        }
    }
}
