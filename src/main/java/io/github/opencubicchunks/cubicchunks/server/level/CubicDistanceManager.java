package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.TicketType;

public interface CubicDistanceManager {
    @UsedFromASM
    <T> void cc_addTicket(TicketType<T> type, CloPos pos, int level, T value);

    @UsedFromASM
    <T> void cc_removeTicket(TicketType<T> type, CloPos pos, int level, T value);

    @UsedFromASM
    <T> void cc_addRegionTicket(TicketType<T> type, CloPos pos, int distance, T value);

    @UsedFromASM
    <T> void cc_addRegionTicket(TicketType<T> type, CloPos pos, int distance, T value, boolean forceTicks);

    @UsedFromASM
    <T> void cc_removeRegionTicket(TicketType<T> type, CloPos pos, int distance, T value);

    @UsedFromASM
    <T> void cc_removeRegionTicket(TicketType<T> type, CloPos pos, int distance, T value, boolean forceTicks);
}
