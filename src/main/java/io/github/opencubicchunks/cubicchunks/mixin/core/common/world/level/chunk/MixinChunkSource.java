package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubicChunkSource;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The {@link ChunkSource} class is the abstract parent class of both {@link ClientChunkCache} and {@link ServerChunkCache}, and contains a few methods common to both classes.
 * This mixin adds versions of chunk-related methods for cubes where necessary, as the subclasses of this class are used to track both cubes and chunks when cubic chunks is enabled.
 */
@Mixin(ChunkSource.class)
public abstract class MixinChunkSource implements CubicChunkSource, MarkableAsCubic {
    protected boolean cc_isCubic;

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
