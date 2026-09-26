package dev.sevenclient.mixin;

import dev.sevenclient.util.RotationSync;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_636.class})
public class InteractionRotationMixin {
   @Inject(
      method = {"method_2919"},
      at = {@At("HEAD")},
      require = 0
   )
   private void seven$latticeBeforeUseItem(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      class_746 self = class_310.method_1551().field_1724;
      if (self != null && player == self) {
         RotationSync.quantiseNow(self);
      }

   }
}
