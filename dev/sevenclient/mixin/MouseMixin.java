package dev.sevenclient.mixin;

import dev.sevenclient.util.FrameDispatcher;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public class MouseMixin {
   @Inject(
      method = {"method_1606"},
      at = {@At("TAIL")},
      require = 0
   )
   private void seven$onUpdateMouse(CallbackInfo ci) {
      FrameDispatcher.fromMouseMixin();
   }
}
