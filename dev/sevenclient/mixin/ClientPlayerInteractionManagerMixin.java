package dev.sevenclient.mixin;

import dev.sevenclient.SevenClient;
import dev.sevenclient.util.Diagnostics;
import dev.sevenclient.util.PacketAudit;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_636.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"method_2918"},
      at = {@At("TAIL")}
   )
   private void seven$onAttackEntity(class_1657 player, class_1297 target, CallbackInfo ci) {
      ++Diagnostics.attackEvents;
      PacketAudit.onAttack(System.currentTimeMillis());
      if (SevenClient.get() != null) {
         SevenClient.get().modules.onAttack(target);
      }

   }
}
