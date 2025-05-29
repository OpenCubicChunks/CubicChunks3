package io.github.opencubicchunks.cubicchunks.client.renderer.cube;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface CubicRenderRegionCache {
    @Nullable RenderCubeRegion cc_createRegion(Level level, BlockPos start, BlockPos end, int padding, boolean nullForEmpty);
}
