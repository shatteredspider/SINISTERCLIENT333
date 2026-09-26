package dev.sevenclient.mixin;

import dev.sevenclient.util.PingSpoofer;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_2827;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2535.class})
public class ClientConnectionMixin {
   @Inject(
      method = {"method_10743(Lnet/minecraft/class_2596;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void seven$onSend(class_2596<?> packet, CallbackInfo ci) {
      if (packet instanceof class_2827 keepAlive) {
         if (PingSpoofer.intercept((class_2535)this, keepAlive)) {
            ci.cancel();
         }
      }

   }
}
