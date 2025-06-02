package io.github.opencubicchunks.cubicchunks.client.renderer.cube;

import javax.annotation.Nullable;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public interface CubicRenderRegionCache {
    @Nullable RenderCubeRegion cc_createRegion(Level level, SectionPos sectionPos, boolean nullForEmpty);
}
