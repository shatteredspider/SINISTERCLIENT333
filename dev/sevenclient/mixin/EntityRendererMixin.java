package dev.sevenclient.mixin;

import dev.sevenclient.module.impl.Chams;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_897.class})
public class EntityRendererMixin {
   @Inject(
      method = {"method_62354"},
      at = {@At("TAIL")},
      require = 0
   )
   private void seven$tintOutline(class_1297 entity, class_10017 state, float tickDelta, CallbackInfo ci) {
      int colour = Chams.outlineColour(entity);
      if (colour != 0) {
         state.field_61821 = colour;
      }

   }
}
