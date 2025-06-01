package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.concurrent.CompletableFuture;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStep;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface GeneratingCubeMap extends GeneratingChunkMap {
    CompletableFuture<CubeAccess> cc_applyCubeStep(GenerationChunkHolder chunk, CubeStep step, StaticCache3D<GenerationChunkHolder> cache);

    ChunkGenerationTask cc_scheduleGenerationTask(ChunkStatus targetStatus, CubePos pos);
}
