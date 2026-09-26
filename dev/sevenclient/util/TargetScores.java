package dev.sevenclient.util;

import dev.sevenclient.SevenClient;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class TargetScores {
   private TargetScores() {
   }

   public static double yawScore(class_746 player, class_243 eye, class_1309 target) {
      float[] rot = Rotations.to(eye, TargetUtil.nearestPoint(eye, target));
      return Math.abs(Rotations.wrap(rot[0] - player.method_36454()));
   }

   public static class_1309 findYaw(double range, boolean enemiesOnly, boolean playersOnly) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1687 == null) {
         return null;
      }
      class_1657 preferred = TargetPriority.winner(range, enemiesOnly);
      if (preferred != null) {
         return preferred;
      }
      class_243 eye = mc.field_1724.method_33571();
      float yaw = mc.field_1724.method_36454();
      class_1309 best = null;
      double bestYaw = Double.MAX_VALUE;
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (!(entity instanceof class_1309 living) || entity == mc.field_1724
               || !entity.method_5805() || entity.method_31481()
               || living.method_5767() && !(entity instanceof class_1657)
               || playersOnly && !(entity instanceof class_1657)) {
            continue;
         }
         if (entity instanceof class_1657 player
               && (player.method_7325() || SevenClient.get().friends.is(player))) {
            continue;
         }
         if (enemiesOnly && !SevenClient.get().enemies.is(entity)
               || eye.method_1022(entity.method_33571()) > range) {
            continue;
         }
         float[] rotation = Rotations.to(eye, TargetUtil.nearestPoint(eye, entity));
         double difference = Math.abs(Rotations.wrap(rotation[0] - yaw));
         if (difference < bestYaw) {
            bestYaw = difference;
            best = living;
         }
      }
      return best;
   }
}
