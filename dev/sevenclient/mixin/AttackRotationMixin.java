package dev.sevenclient.mixin;

import dev.sevenclient.util.RotationSync;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_636.class)
public class AttackRotationMixin {
    @Inject(method = "method_2918(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;)V", at = @At("HEAD"), require = 0)
    private void seven$beforeAttackRotation(class_1657 player, class_1297 target, CallbackInfo ci) {
        if (player instanceof class_746 local) RotationSync.beforeAttack(local, target);
    }
}
