package io.github.opencubicchunks.cubicchunks.test.server.level;

import io.github.opencubicchunks.cubicchunks.integrationtest.server.level.IntegrationTestCubicChunkMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;

/**
 * We do not unit test {@link ChunkHolder} as it is very tightly coupled with {@link ChunkMap} and {@link ServerChunkCache}.
 *
 * @see IntegrationTestCubicChunkMap integration tests
 */
public class TestCubicChunkHolder {

}
