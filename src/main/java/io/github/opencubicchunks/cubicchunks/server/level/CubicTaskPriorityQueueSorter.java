package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;

public interface CubicTaskPriorityQueueSorter {
    @UsedFromASM
    <T> void onLevelChange(CloPos p_140616_, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_);
}
