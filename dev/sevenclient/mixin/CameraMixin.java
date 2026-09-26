package dev.sevenclient.mixin;

import dev.sevenclient.util.RotationSync;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4184.class})
public class CameraMixin {
   @Unique
   private class_746 seven$held;
   @Unique
   private float seven$yaw;
   @Unique
   private float seven$pitch;
   @Unique
   private float seven$lastYaw;
   @Unique
   private float seven$lastPitch;

   @Inject(
      method = {"method_19321"},
      at = {@At("HEAD")},
      require = 0
   )
   private void seven$hideRotation(class_1937 area, class_1297 focused, boolean thirdPerson, boolean inverse, float tickDelta, CallbackInfo ci) {
      this.seven$held = null;
      if (RotationSync.cameraHidden() && focused instanceof class_746 player) {
         this.seven$held = player;
         this.seven$yaw = player.method_36454();
         this.seven$pitch = player.method_36455();
         this.seven$lastYaw = player.field_5982;
         this.seven$lastPitch = player.field_6004;
         player.method_36456(RotationSync.cameraYaw(this.seven$yaw));
         player.method_36457(RotationSync.cameraPitch(this.seven$pitch));
         player.field_5982 = RotationSync.cameraYaw(this.seven$lastYaw);
         player.field_6004 = RotationSync.cameraPitch(this.seven$lastPitch);
      }

   }

   @Inject(
      method = {"method_19321"},
      at = {@At("RETURN")},
      require = 0
   )
   private void seven$restoreRotation(class_1937 area, class_1297 focused, boolean thirdPerson, boolean inverse, float tickDelta, CallbackInfo ci) {
      class_746 player = this.seven$held;
      if (player != null) {
         this.seven$held = null;
         player.method_36456(this.seven$yaw);
         player.method_36457(this.seven$pitch);
         player.field_5982 = this.seven$lastYaw;
         player.field_6004 = this.seven$lastPitch;
      }

   }
}
