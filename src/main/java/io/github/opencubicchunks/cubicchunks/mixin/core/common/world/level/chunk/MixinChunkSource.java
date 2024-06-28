package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubicChunkSource;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkSource.class)
public abstract class MixinChunkSource implements CubicChunkSource, MarkableAsCubic {
    private boolean cc_isCubic;

    @Override public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
    }

    public @Nullable LevelCube cc_getCube(int x, int y, int z, boolean forceLoad) {
        return (LevelCube)this.cc_getCube(x, y, z, ChunkStatus.FULL, forceLoad);
    }

    public @Nullable LevelCube cc_getCubeNow(int x, int y, int z) {
        return this.cc_getCube(x, y, z,false);
    }

    // TODO: Phase 2 - getCubeForLighting

    public boolean cc_hasCube(int x, int y, int z) {
        return this.cc_getCube(x, y, z, ChunkStatus.FULL, false) != null;
    }

    public abstract @Nullable CubeAccess cc_getCube(int x, int y, int z, ChunkStatus status, boolean forceLoad);

    public abstract int cc_getLoadedCubeCount();

    public void cc_updateCubeForced(CubePos cubePos, boolean forced) {}
}
