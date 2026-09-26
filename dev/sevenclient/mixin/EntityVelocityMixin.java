package dev.sevenclient.mixin;

import dev.sevenclient.module.impl.JumpReset;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class})
public class EntityVelocityMixin {
   @Inject(
      method = {"method_5750"},
      at = {@At("HEAD")},
      require = 0
   )
   private void seven$onServerVelocity(class_243 velocity, CallbackInfo ci) {
      class_1297 self = (class_1297)this;
      if (velocity != null && self == class_310.method_1551().field_1724) {
         JumpReset.onServerKnockback(velocity);
      }

   }
}
