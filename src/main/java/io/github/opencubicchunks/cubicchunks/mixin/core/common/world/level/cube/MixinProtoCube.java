package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.cube;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Needed for DASM to apply
@Mixin(ProtoCube.class)
public abstract class MixinProtoCube extends MixinCubeAccess {
    /**
     * Redirect to use cube section indexing instead of chunk section indexing
     */
    @Dynamic @Redirect(method = {"markPosForPostprocessing", "cc_dasm$getBlockState", "cc_dasm$getFluidState"}, at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks"
        + "/world/level/cube/ProtoCube;getSectionIndex(I)I"))
    private int cc_onGetBlockState_SectionIndex(ProtoCube instance, int i, BlockPos pos) {
        return Coords.blockToIndex(pos);
    }
}
