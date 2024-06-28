package io.github.opencubicchunks.cubicchunks.world.level.cube;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface CubicChunkSource {
    @Nullable CubeAccess cc_getCube(int x, int y, int z, ChunkStatus status, boolean forceLoad);

    @Nullable LevelCube cc_getCube(int x, int y, int z, boolean forceLoad);

    @Nullable LevelCube cc_getCubeNow(int x, int y, int z);

    // TODO: Phase 2 - getCubeForLighting

    boolean cc_hasCube(int x, int y, int z);

    int cc_getLoadedCubeCount();

    void cc_updateCubeForced(CubePos cubePos, boolean forced);
}
