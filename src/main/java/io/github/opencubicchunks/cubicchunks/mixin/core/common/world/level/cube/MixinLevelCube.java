package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.cube;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Needed for DASM to apply
@Mixin(LevelCube.class)
public abstract class MixinLevelCube extends MixinCubeAccess {
    /**
     * Redirect to use cube section indexing instead of chunk section indexing
     */
    @Dynamic @Redirect(method = "cc_dasm$getBlockState", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks/world/level/cube/LevelCube;getSectionIndex(I)I"))
    private int cc_onGetBlockState_SectionIndex(LevelCube instance, int i, BlockPos pos) {
        return Coords.blockToIndex(pos);
    }

    /**
     * Redirect to use cube section indexing instead of chunk section indexing
     */
    @Dynamic @Redirect(method = "cc_dasm$getFluidState(III)Lnet/minecraft/world/level/material/FluidState;", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks"
        + "/world/level/cube/LevelCube;getSectionIndex(I)I"))
    private int cc_onGetFluidState_SectionIndex(LevelCube instance, int i, int x, int y, int z) {
        return Coords.blockToIndex(x, y, z);
    }
}
