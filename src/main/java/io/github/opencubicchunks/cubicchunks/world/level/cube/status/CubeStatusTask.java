package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import java.util.concurrent.CompletableFuture;

import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.status.WorldGenContext;

@FunctionalInterface
public interface CubeStatusTask {
    CompletableFuture<CubeAccess> doWork(WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube);
}
