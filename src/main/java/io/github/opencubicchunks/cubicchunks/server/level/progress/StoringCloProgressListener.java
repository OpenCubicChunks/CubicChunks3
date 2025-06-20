package io.github.opencubicchunks.cubicchunks.server.level.progress;

import javax.annotation.Nullable;

import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface StoringCloProgressListener {
    @Nullable ChunkStatus cc_getStatus(int cubeX, int cubeY, int cubeZ);
}
