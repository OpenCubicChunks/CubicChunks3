package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.TicketType;

public interface CubicTickingTracker {
    @UsedFromASM
    <T> void addTicket(TicketType<T> p_184155_, CloPos p_184156_, int p_184157_, T p_184158_);

    @UsedFromASM
    <T> void removeTicket(TicketType<T> p_184169_, CloPos p_184170_, int p_184171_, T p_184172_);

    int getLevel(CloPos p_184162_);
}
