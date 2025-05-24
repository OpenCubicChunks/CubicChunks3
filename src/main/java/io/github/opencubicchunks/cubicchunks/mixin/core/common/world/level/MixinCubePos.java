package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import io.github.opencubicchunks.cc_core.annotation.Public;
import io.github.opencubicchunks.cc_core.api.CubePos;
import org.spongepowered.asm.mixin.Mixin;

/**
 * When DASM redirects ChunkPos to CubePos, some methods do not have reasonable equivalents that can be automatically redirected to.
 * In these cases, we call dummy methods that throw errors, and require that mixins are used to manually replace these method calls with whatever the correct method is.
 */
@Mixin(CubePos.class)
public class MixinCubePos {
    @Public private static CubePos dummy_fromChunkCoords(int x, int z) {
        throw new IllegalStateException(
            "This method should never be called, and indicates that something has been transformed incorrectly"
                + " - all calls to this method should be manually redirected to CubePos.of(x, y, z)");
    }

    @Public private static long dummy_chunkAsLong(int x, int z) {
        throw new IllegalStateException(
            "This method should never be called, and indicates that something has been transformed incorrectly"
                + " - all calls to this method should be manually redirected to CubePos.asLong(x, y, z)");
    }
}
