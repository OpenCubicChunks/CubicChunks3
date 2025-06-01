package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cc_core.api.CubePos;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface CubicChunkMap {
    ChunkGenerationTask cc_scheduleGenerationTask(ChunkStatus chunkStatus, CubePos cubePos);

    void cc_onFullChunkStatusChange(CubePos cubePos, FullChunkStatus fullChunkStatus);

    boolean cc_isChunkTracked(ServerPlayer player, int x, int y, int z);
}
