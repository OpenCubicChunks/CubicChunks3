package io.github.opencubicchunks.cubicchunks.world.level.entity;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.server.level.FullChunkStatus;

@FunctionalInterface
public interface CloStatusUpdateListener {
    void onChunkStatusChange(CloPos chunkPos, FullChunkStatus fullChunkStatus);
}
