package io.github.opencubicchunks.cubicchunks.test.server.level;

import io.github.opencubicchunks.cubicchunks.integrationtest.server.level.IntegrationTestCubicChunkMap;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerChunkCache;

/**
 * We do not unit test {@link GenerationChunkHolder} or {@link ChunkHolder} as it is very tightly coupled with {@link ChunkGenerationTask},
 * {@link ChunkMap} and {@link ServerChunkCache}.
 *
 * @see IntegrationTestCubicChunkMap integration tests
 */
public class TestCloHolder {

}
