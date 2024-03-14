package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MixinMobTest {
    @Inject(method = "createMobAttributes", at = @At(value = "HEAD"), cancellable = true)
    private static void cubic_chunks_3$fixNeoForgeErrors(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(AttributeSupplier.builder());
    }
}
