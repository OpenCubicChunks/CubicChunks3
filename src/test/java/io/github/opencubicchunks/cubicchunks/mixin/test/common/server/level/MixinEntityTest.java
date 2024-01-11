package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntityTest {
    /**
     * This needs to be removed because NeoForge's registry is gone when we are testing certain classes (e.g. TestServerPlayer).
     */
    @Redirect(method = "Lnet/minecraft/world/entity/Entity;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;forgeFluidTypeOnEyes:Lnet/neoforged/neoforge/fluids/FluidType;"))
    public void cc_removeForgeFluidTypeOnEyes(Entity instance, FluidType value) {}
}
