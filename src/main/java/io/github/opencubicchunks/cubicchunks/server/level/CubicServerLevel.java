package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;

public interface CubicServerLevel {
    @UsedFromASM
    boolean isNaturalSpawningAllowed(CloPos cloPos);

    @UsedFromASM
    void invalidateCapabilities(CloPos cloPos);
}
