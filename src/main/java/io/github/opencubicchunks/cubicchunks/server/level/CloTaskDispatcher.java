package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.opencubicchunks.cc_core.world.level.CloPos;

public interface CloTaskDispatcher {
    void cc_onLevelChange(CloPos cloPos, IntSupplier queueLevelGetter, int ticketLevel, IntConsumer queueLevelSetter);
}
