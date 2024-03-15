package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.mockito.Mockito;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Remove this mixin class when NeoForge supports JUnit for tests
//  This mixin class is only required due to NeoForge not supporting JUnit for testing yet (a workaround is currently used, involving Loom)
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityTest {
    @Inject(method = "createLivingAttributes", at = @At(value = "HEAD"), cancellable = true)
    private static void cubic_chunks_3$mockLivingAttributesInitialization(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mockito.mock());
    }

    @Inject(method = "getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", at = @At(value = "HEAD"), cancellable = true)
    private void cubic_chunks_3$fixNeoForgeErrors(Attribute pAttribute, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(1d);
    }
}
