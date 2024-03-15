package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Remove this mixin class when NeoForge supports JUnit for tests
//  This mixin class is only required due to NeoForge not supporting JUnit for testing yet (a workaround is currently used, involving Loom)
@Mixin(Mob.class)
public abstract class MixinMobTest {
    @Inject(method = "createMobAttributes", at = @At(value = "HEAD"), cancellable = true)
    private static void cubic_chunks_3$fixNeoForgeErrors(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(AttributeSupplier.builder());
    }
}
