package dev.sevenclient.mixin;

import dev.sevenclient.module.impl.PingSpoof;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public class MouseScrollMixin {
   @Inject(
      method = {"method_1598"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void seven$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (PingSpoof.handleScroll(vertical)) {
         ci.cancel();
      }

   }
}
