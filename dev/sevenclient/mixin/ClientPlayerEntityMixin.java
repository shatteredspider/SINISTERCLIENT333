package dev.sevenclient.mixin;

import dev.sevenclient.util.RotationSync;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_746.class})
public class ClientPlayerEntityMixin {
   @Inject(
      method = {"method_3136"},
      at = {@At("HEAD")}
   )
   private void seven$beforeMovementPackets(CallbackInfo ci) {
      class_746 self = (class_746)this;
      RotationSync.beginSilent(self);
      RotationSync.snapBeforeSend(self);
   }

   @Inject(
      method = {"method_3136"},
      at = {@At("RETURN")}
   )
   private void seven$afterMovementPackets(CallbackInfo ci) {
      RotationSync.endSilent((class_746)this);
   }
}
