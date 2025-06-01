package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.concurrent.CompletableFuture;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface ServerCubeCache extends CubeSource {
    CompletableFuture<ChunkResult<CubeAccess>> cc_getCubeFuture(
        int pX, int pY, int pZ, ChunkStatus pChunkStatus, boolean pLoad
    );

    void cc_blockChanged(BlockPos pos);
    void cc_onLightUpdate(LightLayer pType, SectionPos pPos);

    <T> void cc_addRegionTicket(TicketType<T> pType, CloPos pPos, int pDistance, T pValue);
    <T> void cc_addRegionTicket(TicketType<T> p_8388_, CloPos p_8389_, int p_8390_, T p_8391_, boolean forceTicks);

    <T> void cc_removeRegionTicket(TicketType<T> pType, CloPos pPos, int pDistance, T pValue);
    <T> void cc_removeRegionTicket(TicketType<T> p_8439_, CloPos p_8440_, int p_8441_, T p_8442_, boolean forceTicks);

    // Stored on this interface since we can't add inner records in mixins
    record CloAndHolder(LevelClo chunk, ChunkHolder holder) {}
}
