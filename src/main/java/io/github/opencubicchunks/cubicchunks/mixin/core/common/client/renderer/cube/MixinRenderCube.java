package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer.cube;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Needed for DASM to apply
@Mixin(RenderCube.class)
public abstract class MixinRenderCube {
    /**
     * Redirect to use cube section indexing instead of chunk section indexing
     */
    @Dynamic @Redirect(method = "getBlockState", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks/world/level/cube/LevelCube;getSectionIndex(I)I"))
    private int cc_onGetBlockState_SectionIndex(LevelCube instance, int y, BlockPos pos) {
        return Coords.blockToIndex(pos);
    }
}
