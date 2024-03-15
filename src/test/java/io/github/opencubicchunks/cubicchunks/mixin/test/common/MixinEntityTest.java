package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.fluids.FluidType;
import org.mockito.Mockito;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Remove this mixin class when NeoForge supports JUnit for tests
//  This mixin class is only required due to NeoForge not supporting JUnit for testing yet (a workaround is currently used, involving Loom)
@Mixin(Entity.class)
public abstract class MixinEntityTest {
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;forgeFluidTypeOnEyes:Lnet/neoforged/neoforge/fluids/FluidType;", shift = At.Shift.BY, by = -3))
    private Holder<FluidType> cubic_chunks_3$fixNeoForgeRegistryError() {
        return Mockito.mock();
    }

    @Inject(method = "toString", at = @At(value = "HEAD"), cancellable = true)
    private void cubic_chunks_3$fixNullPointerException(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("MockedEntity");
    }
}
