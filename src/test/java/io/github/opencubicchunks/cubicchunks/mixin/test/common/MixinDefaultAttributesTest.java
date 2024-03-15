package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: Remove this mixin class when NeoForge supports JUnit for tests
//  This mixin class is only required due to NeoForge not supporting JUnit for testing yet (a workaround is currently used, involving Loom)
@Mixin(DefaultAttributes.class)
public abstract class MixinDefaultAttributesTest {
    @Redirect(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/DefaultAttributes;SUPPLIERS:Ljava/util/Map;", shift = At.Shift.BY, by = -1))
    private static ImmutableMap<?, ?> cubic_chunks_3$fixNeoForgeRegistryError(ImmutableMap.Builder<?, ?> instance) {
        return ImmutableMap.builder().build();
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;"))
    private static AttributeSupplier.Builder cubic_chunks_3$fixNeoForgeRegistryError() {
        return AttributeSupplier.builder();
    }
}
