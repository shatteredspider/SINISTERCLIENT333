package dev.sevenclient.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_310.class})
public interface MinecraftClientAccessor {
   @Accessor("field_1752")
   void seven$setItemUseCooldown(int var1);

   @Accessor("field_1752")
   int seven$getItemUseCooldown();

   @Invoker("method_1536")
   boolean seven$doAttack();
}
