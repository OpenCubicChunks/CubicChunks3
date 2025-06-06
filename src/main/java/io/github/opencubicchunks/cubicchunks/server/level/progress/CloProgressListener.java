package io.github.opencubicchunks.cubicchunks.server.level.progress;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface CloProgressListener {
    void cc_updateSpawnPos(CloPos center);

    void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);

    default void cc_updateSpawnPos(CubePos center) {
        cc_updateSpawnPos(CloPos.cube(center));
    }

    default void cc_onStatusChange(CubePos chunkPosition, @Nullable ChunkStatus newStatus) {
        cc_onStatusChange(CloPos.cube(chunkPosition), newStatus);
    }
}
