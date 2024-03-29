package io.github.opencubicchunks.cubicchunks.world.lighting;

import io.github.opencubicchunks.cc_core.world.ColumnCubeMapGetter;

public interface SkyLightColumnChecker {
    /** all parameters are global coordinates */
    void checkSkyLightColumn(ColumnCubeMapGetter chunk, int x, int z, int oldHeight, int newHeight);
}
