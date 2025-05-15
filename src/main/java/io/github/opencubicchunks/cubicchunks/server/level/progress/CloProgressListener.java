package io.github.opencubicchunks.cubicchunks.server.level.progress;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface CloProgressListener {
    void cc_updateSpawnPos(CloPos center);

    void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
}
