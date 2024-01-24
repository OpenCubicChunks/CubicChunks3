package io.github.opencubicchunks.cubicchunks.world.level.cube;

import io.github.opencubicchunks.cc_core.api.CubePos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface CubicChunkSource {
    CubeAccess cc_getCube(int x, int y, int z, ChunkStatus status, boolean forceLoad);

    LevelCube cc_getCube(int x, int y, int z, boolean forceLoad);

    LevelCube cc_getCubeNow(int x, int y, int z);

    // TODO: Phase 2 - getCubeForLighting

    boolean cc_hasCube(int x, int y, int z);

    int cc_getLoadedCubeCount();

    void cc_updateCubeForced(CubePos cubePos, boolean forced);
}
