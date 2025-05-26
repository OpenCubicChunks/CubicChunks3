package io.github.opencubicchunks.cubicchunks.integrationtest.server.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
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
public class IntegrationTestServerCubeCache extends BaseTest {
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
            var f = serverLevelMock.getClass().getSuperclass().getSuperclass().getDeclaredField("cc_isCubic");
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
            // Need to mock an implementation of the interface, so that it also implements CloProgressListener
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

    @Test
    public void getChunkNowVanilla() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(true)) {
            var serverChunkCache = serverChunkCacheRef.value();
            // Present chunk
            ChunkPos pos = new ChunkPos(5, -123);
            serverChunkCache.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            var chunkAccess = serverChunkCache.getChunkNow(pos.x, pos.z);
            assertNotNull(chunkAccess);
            assertSame(ChunkStatus.FULL, chunkAccess.getStatus());
            assertInstanceOf(LevelChunk.class, chunkAccess);

            // Neighbor chunk
            chunkAccess = serverChunkCache.getChunkNow(pos.x - 1, pos.z);
            assertNull(chunkAccess); // Expected to be null as getChunkNow requests at FULL

            // Non-present chunk
            chunkAccess = serverChunkCache.getChunkNow(0, 0);
            assertNull(chunkAccess);
        }
    }

    @Test
    public void hasChunkVanilla() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(true)) {
            var serverChunkCache = serverChunkCacheRef.value();
            // Non-present chunk
            ChunkPos pos = new ChunkPos(-12, 65);
            assertFalse(serverChunkCache.hasChunk(pos.x, pos.z));

            // Load a chunk
            serverChunkCache.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            // Retest chunk
            assertTrue(serverChunkCache.hasChunk(pos.x, pos.z));

            // Neighbor chunk, expected to be false as hasChunk checks for FULL
            assertFalse(serverChunkCache.hasChunk(pos.x - 1, pos.z)); // Ex
        }
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
     * getNow a single chunk in a cubic ServerChunkCache
     */
    @Test
    public void getChunkNow() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            // Present chunk
            serverChunkCache.getChunk(5, -123, ChunkStatus.FULL, true);
            var chunkAccess = serverChunkCache.getChunkNow(5, -123);
            assertNotNull(chunkAccess);
            assertSame(ChunkStatus.FULL, chunkAccess.getStatus());
            assertInstanceOf(LevelChunk.class, chunkAccess);

            // Neighbor chunk
            chunkAccess = serverChunkCache.getChunkNow(4, -123);
            assertNull(chunkAccess); // Expected to be null as getChunkNow requests at FULL

            // Non-present chunk
            chunkAccess = serverChunkCache.getChunkNow(0, 0);
            assertNull(chunkAccess);
        }
    }

    /**
     * test hasChunk a for single chunk in a cubic ServerChunkCache
     */
    @Test
    public void hasChunk() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            ChunkPos pos = new ChunkPos(-12, 65);
            // Non-present chunk
            assertFalse(serverChunkCache.hasChunk(pos.x, pos.z));

            // Load a chunk
            serverChunkCache.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            // Retest chunk
            assertTrue(serverChunkCache.hasChunk(pos.x, pos.z));

            // Neighbor chunk, expected to be false as hasChunk checks for FULL
            assertFalse(serverChunkCache.hasChunk(pos.x - 1, pos.z));
        }
    }

    /**
     * Get a single cube in a cubic ServerChunkCache
     */
    public void singleGetCube(ChunkStatus status) throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = ((ServerCubeCache) serverChunkCacheRef.value());
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
     * getNow a single cube in a cubic ServerChunkCache
     */
    @Test
    public void getCubeNow() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            var cubicServerChunkCache = ((ServerCubeCache) serverChunkCache);

            // Present chunk
            CubePos cubePos = CubePos.of(5, 1273, -123);
            cubicServerChunkCache.cc_getCube(cubePos.getX(), cubePos.getY(), cubePos.getZ(), ChunkStatus.FULL, true);
            var cubeAccess = cubicServerChunkCache.cc_getCubeNow(cubePos.getX(), cubePos.getY(), cubePos.getZ());
            assertNotNull(cubeAccess);
            assertSame(ChunkStatus.FULL, cubeAccess.getStatus());
            assertInstanceOf(LevelCube.class, cubeAccess);
            // check its chunks
            for (int localChunkX = 0; localChunkX < CubicConstants.DIAMETER_IN_SECTIONS; localChunkX++) {
                for (int localChunkZ = 0; localChunkZ < CubicConstants.DIAMETER_IN_SECTIONS; localChunkZ++) {
                    var chunkPos = cubePos.asChunkPos(localChunkX, localChunkZ);
                    var chunkAccess = serverChunkCache.getChunkNow(chunkPos.x, chunkPos.z);
                    assertNotNull(chunkAccess);
                    assertSame(ChunkStatus.FULL, chunkAccess.getStatus());
                    assertInstanceOf(LevelChunk.class, chunkAccess);
                }
            }

            // Neighbor cube
            cubePos = CubePos.of(cubePos.getX() - 1, cubePos.getY(), cubePos.getZ());
            cubeAccess = cubicServerChunkCache.cc_getCubeNow(cubePos.getX(), cubePos.getY(), cubePos.getZ());
            assertNull(cubeAccess); // Expected to be null as getCubeNow requests at FULL
            // check its chunks
            for (int localChunkX = 0; localChunkX < CubicConstants.DIAMETER_IN_SECTIONS; localChunkX++) {
                for (int localChunkZ = 0; localChunkZ < CubicConstants.DIAMETER_IN_SECTIONS; localChunkZ++) {
                    var chunkPos = cubePos.asChunkPos(localChunkX, localChunkZ);
                    var chunkAccess = serverChunkCache.getChunkNow(chunkPos.x, chunkPos.z);
                    assertNull(chunkAccess);
                }
            }

            // Non-present cube
            cubePos = CubePos.of(0, 0, 0);
            cubeAccess = cubicServerChunkCache.cc_getCubeNow(cubePos.getX(), cubePos.getY(), cubePos.getZ());
            assertNull(cubeAccess);
            // check its chunks
            for (int localChunkX = 0; localChunkX < CubicConstants.DIAMETER_IN_SECTIONS; localChunkX++) {
                for (int localChunkZ = 0; localChunkZ < CubicConstants.DIAMETER_IN_SECTIONS; localChunkZ++) {
                    var chunkPos = cubePos.asChunkPos(localChunkX, localChunkZ);
                    var chunkAccess = serverChunkCache.getChunkNow(chunkPos.x, chunkPos.z);
                    assertNull(chunkAccess);
                }
            }
        }
    }

    /**
     * test hasCube a for single cube in a cubic ServerChunkCache
     */
    @Test
    public void hasCube() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            ServerChunkCache serverChunkCache = serverChunkCacheRef.value();
            var serverCubeCache = ((ServerCubeCache) serverChunkCache);
            // Non-present cube
            CubePos cubePos = CubePos.of(-12,  98, 65);
            assertFalse(serverCubeCache.cc_hasCube(cubePos.getX(), cubePos.getY(), cubePos.getZ()));
            // check its chunks
            for (int localChunkX = 0; localChunkX < CubicConstants.DIAMETER_IN_SECTIONS; localChunkX++) {
                for (int localChunkZ = 0; localChunkZ < CubicConstants.DIAMETER_IN_SECTIONS; localChunkZ++) {
                    var chunkPos = cubePos.asChunkPos(localChunkX, localChunkZ);
                    var has = serverChunkCache.hasChunk(chunkPos.x, chunkPos.z);
                    assertFalse(has);
                }
            }

            // Load a cube
            serverCubeCache.cc_getCube(cubePos.getX(), cubePos.getY(), cubePos.getZ(), ChunkStatus.FULL, true);

            // Retest cube
            assertTrue(serverCubeCache.cc_hasCube(cubePos.getX(), cubePos.getY(), cubePos.getZ()));
            // check its chunks
            for (int localChunkX = 0; localChunkX < CubicConstants.DIAMETER_IN_SECTIONS; localChunkX++) {
                for (int localChunkZ = 0; localChunkZ < CubicConstants.DIAMETER_IN_SECTIONS; localChunkZ++) {
                    var chunkPos = cubePos.asChunkPos(localChunkX, localChunkZ);
                    var has = serverChunkCache.hasChunk(chunkPos.x, chunkPos.z);
                    assertTrue(has);
                }
            }

            // Neighbor cube, expected to be false as hasChunk checks for FULL
            cubePos = CubePos.of(cubePos.getX() - 1, cubePos.getY(), cubePos.getZ());
            assertFalse(serverCubeCache.cc_hasCube(cubePos.getX(), cubePos.getY(), cubePos.getZ())); // Expected false as hasCube checks for full status
            // check its chunks
            for (int localChunkX = 0; localChunkX < CubicConstants.DIAMETER_IN_SECTIONS; localChunkX++) {
                for (int localChunkZ = 0; localChunkZ < CubicConstants.DIAMETER_IN_SECTIONS; localChunkZ++) {
                    var chunkPos = cubePos.asChunkPos(localChunkX, localChunkZ);
                    var has = serverChunkCache.hasChunk(chunkPos.x, chunkPos.z);
                    assertFalse(has);
                }
            }
        }
    }

    /**
     * Get a cube and nearby cubes and chunks in a cubic ServerChunkCache
     */
    @Test public void getCubeAndNeighboringCubesAndChunks() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            var cubicServerChunkCache = ((ServerCubeCache) serverChunkCache);
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

    @Test public void testAddCubicRegionTicket() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache(false)) {
            var serverChunkCache = serverChunkCacheRef.value();
            var cubicServerChunkCache = ((ServerCubeCache) serverChunkCache);
            int spawnRadius = Coords.sectionToCube(11);
            cubicServerChunkCache.cc_addRegionTicket(TicketType.START, CloPos.cube(0, 0, 0), spawnRadius, Unit.INSTANCE);
            serverChunkCache.tick(()->true, false);
            var cubeAccess = cubicServerChunkCache.cc_getCube(0, 0, 0, ChunkStatus.FULL, true);
            assertNotNull(cubeAccess);
            assertTrue(cubeAccess.getStatus().isOrAfter(ChunkStatus.FULL));
            assertInstanceOf(LevelCube.class, cubeAccess);
        }
    }
}
