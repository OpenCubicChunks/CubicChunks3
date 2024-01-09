package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.TicketType;

public interface CubicDistanceManager {
    @UsedFromASM
    <T> void addTicket(TicketType<T> p_140793_, CloPos p_140794_, int p_140795_, T p_140796_);

    @UsedFromASM
    <T> void removeTicket(TicketType<T> p_140824_, CloPos p_140825_, int p_140826_, T p_140827_);

    @UsedFromASM
    <T> void addRegionTicket(TicketType<T> p_140841_, CloPos p_140842_, int p_140843_, T p_140844_);

    @UsedFromASM
    <T> void addRegionTicket(TicketType<T> p_140841_, CloPos p_140842_, int p_140843_, T p_140844_, boolean forceTicks);

    @UsedFromASM
    <T> void removeRegionTicket(TicketType<T> p_140850_, CloPos p_140851_, int p_140852_, T p_140853_);

    @UsedFromASM
    <T> void removeRegionTicket(TicketType<T> p_140850_, CloPos p_140851_, int p_140852_, T p_140853_, boolean forceTicks);
}
