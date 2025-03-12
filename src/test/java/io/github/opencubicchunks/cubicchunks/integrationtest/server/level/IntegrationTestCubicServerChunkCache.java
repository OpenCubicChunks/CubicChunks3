package io.github.opencubicchunks.cubicchunks.integrationtest.server.level;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.server.level.CubicServerChunkCache;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mockito;

/**
 * Integration tests for getting chunks and cubes from {@link ServerChunkCache}.
 * <p>
 * This test is strongly dependent on {@link DistanceManager} and {@link ChunkMap} as well; errors here should probably be ignored unless {@link IntegrationTestCubicChunkMap} passes.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTestCubicServerChunkCache extends BaseTest {
    private Stream<ChunkStatus> chunkStatuses() {
        return ChunkStatus.getStatusList().stream();
    }

    private CloseableReference<ServerChunkCache> createServerChunkCache(boolean vanillaTest) throws IOException, NoSuchFieldException, IllegalAccessException {
        // Worldgen internals
        var randomStateMockedStatic = Mockito.mockStatic(RandomState.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        NoiseBasedChunkGenerator noiseBasedChunkGeneratorMock = mock();
        when(noiseBasedChunkGeneratorMock.generatorSettings()).thenReturn(mock());
        if (vanillaTest) {
            // These methods are currently only called when running vanilla tests
            when(noiseBasedChunkGeneratorMock.createBiomes(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
            when(noiseBasedChunkGeneratorMock.fillFromNoise(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
        }

        ServerLevel serverLevelMock;
        try (var ignored = Mockito.mockConstruction(ServerChunkCache.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS))) {
            // Server level
            serverLevelMock = mock(withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).extraInterfaces(CanBeCubic.class));
        }
        if (!vanillaTest) {
            var f = serverLevelMock.getClass().getSuperclass().getDeclaredField("cc_isCubic");
            f.setAccessible(true);
            f.set(serverLevelMock, true);
            when(((CanBeCubic) serverLevelMock).cc_isCubic()).thenReturn(true);
        }
        when(serverLevelMock.getHeight()).thenReturn(384);
        when(serverLevelMock.getSectionsCount()).thenReturn(24);
        // We seem to need an actual directory, not a mock
        LevelStorageSource.LevelStorageAccess levelStorageAccessMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(levelStorageAccessMock.getDimensionPath(any())).thenReturn(Files.createTempDirectory("cc_test"));
        var serverChunkCache = new ServerChunkCache(
            serverLevelMock,
            levelStorageAccessMock,
            mock(Mockito.RETURNS_DEEP_STUBS),
            mock(Mockito.RETURNS_DEEP_STUBS),
            // We run everything on the main thread as Mockito has race conditions when multiple threads call into it
            // (which occurs when using RETURNS_DEEP_STUBS)
            Runnable::run,
            noiseBasedChunkGeneratorMock,
            10, // server view distance
            10, // simulation distance
            false, // sync - not relevant for tests; false should be faster
            // Need to mock an implementation of the interface, so that it also implements CubicChunkProgressListener
            Mockito.<ProcessorChunkProgressListener>mock(Mockito.RETURNS_DEEP_STUBS),
            mock(Mockito.RETURNS_DEEP_STUBS),
            mock(Mockito.RETURNS_DEEP_STUBS)
        );
        var f = serverLevelMock.getClass().getSuperclass().getDeclaredField("chunkSource");
        f.setAccessible(true);
        f.set(serverLevelMock, serverChunkCache);
        when(serverLevelMock.getChunkSource()).thenReturn(serverChunkCache);
        return new CloseableReference<>(serverChunkCache, randomStateMockedStatic);
    }

    /**
     * Get a single chunk in a non-cubic ServerChunkCache
     */
    public void singleGetChunkVanilla(ChunkStatus status) throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(true)) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkAccess = serverChunkCache.getChunk(0, 0, status, true);
            assertNotNull(chunkAccess);
            assertTrue(chunkAccess.getStatus().isOrAfter(status));
            if (status.isOrAfter(ChunkStatus.FULL)) {
                assertInstanceOf(LevelChunk.class, chunkAccess);
            } else {
                assertInstanceOf(ProtoChunk.class, chunkAccess);
            }
        }
    }

    @ParameterizedTest @MethodSource("chunkStatuses")
    public void getChunkVanilla(ChunkStatus status) throws Exception {
        singleGetChunkVanilla(status);
    }

    // TODO (P2) test these methods:
    // isPositionTicking
    // tick
    // blockChanged
    // onLightUpdate

    /**
     * Get a single chunk in a cubic ServerChunkCache
     */
    public void singleGetChunk(ChunkStatus status) throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkAccess = serverChunkCache.getChunk(0, 0, status, true);
            assertNotNull(chunkAccess);
            assertTrue(chunkAccess.getStatus().isOrAfter(status));
            if (status.isOrAfter(ChunkStatus.FULL)) {
                assertInstanceOf(LevelChunk.class, chunkAccess);
            } else {
                assertInstanceOf(ProtoChunk.class, chunkAccess);
            }
        }
    }

    @ParameterizedTest @MethodSource("chunkStatuses")
    public void getChunk(ChunkStatus status) throws Exception {
        singleGetChunk(status);
    }

    /**
     * Get a single cube in a non-cubic ServerChunkCache
     */
    public void singleGetCube(ChunkStatus status) throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = ((CubicServerChunkCache) serverChunkCacheRef.value());
            var chunkAccess = serverChunkCache.cc_getCube(0, 0, 0, status, true);
            assertNotNull(chunkAccess);
            assertTrue(chunkAccess.getStatus().isOrAfter(status));
            if (status.isOrAfter(ChunkStatus.FULL)) {
                assertInstanceOf(LevelCube.class, chunkAccess);
            } else {
                assertInstanceOf(ProtoCube.class, chunkAccess);
            }
        }
    }

    @ParameterizedTest @MethodSource("chunkStatuses")
    public void getCube(ChunkStatus status) throws Exception {
        singleGetCube(status);
    }

    /**
     * Get a cube and nearby cubes and chunks in a cubic ServerChunkCache
     */
    @Test public void getCubeAndNeighboringCubesAndChunks() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            var cubicServerChunkCache = ((CubicServerChunkCache) serverChunkCache);
            var cubeAccess = cubicServerChunkCache.cc_getCube(0, 0, 0, ChunkStatus.FULL, true);
            assertNotNull(cubeAccess);
            assertTrue(cubeAccess.getStatus().isOrAfter(ChunkStatus.FULL));
            assertInstanceOf(LevelCube.class, cubeAccess);
            for (int i = 0; i < ChunkStatus.maxDistance(); i++) {
                var expectedStatus = ChunkStatus.getStatusAroundFullChunk(i);
                cubeAccess = cubicServerChunkCache.cc_getCube(i, -i, 0, expectedStatus, false);
                assertNotNull(cubeAccess);
                assertTrue(cubeAccess.getStatus().isOrAfter(expectedStatus));
                for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                    for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                        int x = -i * CubicConstants.DIAMETER_IN_SECTIONS + dx;
                        int z = i * CubicConstants.DIAMETER_IN_SECTIONS + dz;
                        if (expectedStatus.isOrAfter(ChunkStatus.FULL))
                            assertTrue(serverChunkCache.hasChunk(x, z));
                        var chunkAccess = serverChunkCache.getChunk(x, z, expectedStatus, false);
                        assertNotNull(chunkAccess);
                        assertTrue(chunkAccess.getStatus().isOrAfter(expectedStatus));
                    }
                }
            }
        }
    }
}
