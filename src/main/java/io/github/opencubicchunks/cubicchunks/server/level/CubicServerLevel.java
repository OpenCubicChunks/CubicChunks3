package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;

public interface CubicServerLevel extends CubicLevel {
    @UsedFromASM
    boolean isNaturalSpawningAllowed(CloPos cloPos);

    @UsedFromASM
    void invalidateCapabilities(CloPos cloPos);
}
