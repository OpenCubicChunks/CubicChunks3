package io.github.opencubicchunks.cubicchunks.world.server;

import java.util.concurrent.CompletableFuture;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.server.level.CubeTaskPriorityQueueSorter;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.CubeAccess;
import net.minecraft.util.thread.ProcessorHandle;

public interface CubicThreadedLevelLightEngine {
    void postConstructorSetup(CubeTaskPriorityQueueSorter sorter,
                              ProcessorHandle<CubeTaskPriorityQueueSorter.Message<Runnable>> taskExecutor);

    void setCubeStatusEmpty(CubePos cubePos);

    CompletableFuture<CubeAccess> lightCube(CubeAccess cube, boolean b);
}