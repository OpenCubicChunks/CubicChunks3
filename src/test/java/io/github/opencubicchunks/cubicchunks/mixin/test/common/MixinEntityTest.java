package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.fluids.FluidType;
import org.mockito.Mockito;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntityTest {
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;forgeFluidTypeOnEyes:Lnet/neoforged/neoforge/fluids/FluidType;", shift = At.Shift.BY, by = -3))
    private Holder<FluidType> cubic_chunks_3$fixNeoForgeRegistryError() {
        return Mockito.mock();
    }
}
