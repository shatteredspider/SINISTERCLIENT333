package dev.sevenclient.mixin;

import dev.sevenclient.util.TargetPriority;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_8143;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Attribute only a fresh server damage event on the local player with an identifiable player source. */
@Mixin(class_634.class)
public class IncomingDamageMixin {
   @Inject(method = "method_49034(Lnet/minecraft/class_8143;)V", at = @At("TAIL"), require = 0)
   private void seven$onIncomingDamage(class_8143 packet, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1687 == null || packet.comp_1267() != mc.field_1724.method_5628()) return;
      class_1282 source = packet.method_49071(mc.field_1687);
      if (source == null) return;
      class_1297 attacker = source.method_5529();
      if (attacker instanceof class_1657 player && player != mc.field_1724) {
         TargetPriority.onIncomingPlayerHit(player);
      }
   }
}
