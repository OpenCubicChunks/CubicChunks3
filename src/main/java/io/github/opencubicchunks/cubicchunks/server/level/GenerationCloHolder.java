package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;

public interface GenerationCloHolder {
    CloPos cc_getCloPos();

    void cc_replaceProtoCube(ImposterProtoCube cube);
}
