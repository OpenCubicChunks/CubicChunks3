package io.github.opencubicchunks.cubicchunks.testutils;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class DummyChunkProgressListener implements ChunkProgressListener, CloProgressListener {
    @Override public void cc_updateSpawnPos(CloPos center) {

    }

    @Override public void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus) {

    }

    @Override public void updateSpawnPos(ChunkPos center) {

    }

    @Override public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {

    }

    @Override public void start() {

    }

    @Override public void stop() {

    }
}
