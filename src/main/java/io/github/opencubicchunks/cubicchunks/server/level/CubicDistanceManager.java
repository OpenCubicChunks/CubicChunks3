package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.TicketType;

public interface CubicDistanceManager {
    @UsedFromASM
    <T> void cc_addTicket(TicketType type, CloPos pos, int level, T value);

    @UsedFromASM
    <T> void cc_removeTicket(TicketType type, CloPos pos, int level, T value);

    @UsedFromASM
    <T> void cc_addRegionTicket(TicketType type, CloPos pos, int distance, T value);

    @UsedFromASM
    <T> void cc_addRegionTicket(TicketType type, CloPos pos, int distance, T value, boolean forceTicks);

    @UsedFromASM
    <T> void cc_removeRegionTicket(TicketType type, CloPos pos, int distance, T value);

    @UsedFromASM
    <T> void cc_removeRegionTicket(TicketType type, CloPos pos, int distance, T value, boolean forceTicks);

    @UsedFromASM
    boolean cc_runAllUpdates(ChunkMap chunkManager);
}
