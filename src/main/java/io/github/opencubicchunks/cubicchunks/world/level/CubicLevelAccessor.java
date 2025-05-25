package io.github.opencubicchunks.cubicchunks.world.level;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeSource;

public interface CubicLevelAccessor extends CubicLevelReader {
    CubeSource cc_getCubeSource();

    @Override
    default boolean cc_hasCube(int cubeX, int cubeY, int cubeZ) {
        return this.cc_getCubeSource().cc_hasCube(cubeX, cubeY, cubeZ);
    }
}
