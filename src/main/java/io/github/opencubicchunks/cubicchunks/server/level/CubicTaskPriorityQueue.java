package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;

public interface CubicTaskPriorityQueue {
    @UsedFromASM
    <T> void resortChunkTasks(int p_140522_, CloPos p_140523_, int p_140524_);
}
