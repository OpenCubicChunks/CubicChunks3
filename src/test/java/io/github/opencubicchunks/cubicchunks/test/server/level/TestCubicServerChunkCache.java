package io.github.opencubicchunks.cubicchunks.test.server.level;

import io.github.opencubicchunks.cubicchunks.integrationtest.server.level.IntegrationTestCubicServerChunkCache;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;

/**
 * We do not unit test {@link ServerChunkCache} as it is very tightly coupled with {@link ChunkMap} and {@link ChunkHolder}.
 *
 * @see IntegrationTestCubicServerChunkCache integration tests
 */
public class TestCubicServerChunkCache {

}
