package dev.sevenclient.mixin;

import dev.sevenclient.module.impl.Chams;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_310.class})
public class MinecraftClientOutlineMixin {
   @Inject(
      method = {"method_27022"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void seven$forceOutline(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
      if (Chams.wantsOutline(entity)) {
         cir.setReturnValue(true);
      }

   }
}
