package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.server.level.TicketType;

public interface CubicTickingTracker {
    @UsedFromASM
    <T> void cc_addTicket(TicketType<T> type, CloPos cloPos, int ticketLevel, T key);

    @UsedFromASM
    <T> void cc_removeTicket(TicketType<T> type, CloPos cloPos, int ticketLevel, T key);

    int cc_getLevel(CloPos cloPos);
}
