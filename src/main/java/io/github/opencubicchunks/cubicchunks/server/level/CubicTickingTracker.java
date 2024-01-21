package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.TicketType;

public interface CubicTickingTracker {
    @UsedFromASM
    <T> void addTicket(TicketType<T> type, CloPos cloPos, int ticketLevel, T key);

    @UsedFromASM
    <T> void removeTicket(TicketType<T> type, CloPos cloPos, int ticketLevel, T key);

    int getLevel(CloPos cloPos);
}
