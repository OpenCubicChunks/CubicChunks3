package io.github.opencubicchunks.cubicchunks.integrationtest.server.level;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.ChunkHolderTestAccess;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.ChunkMapTestAccess;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.ServerChunkCacheTestAccess;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.Util;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTestCubicChunkMap extends BaseTest {
    private CloseableReference<ServerChunkCache> createServerChunkCache() throws IOException {
        // Worldgen internals
        var randomStateMockedStatic = Mockito.mockStatic(RandomState.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        NoiseBasedChunkGenerator noiseBasedChunkGeneratorMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(noiseBasedChunkGeneratorMock.createBiomes(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
        when(noiseBasedChunkGeneratorMock.fillFromNoise(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
        // Distance manager is responsible for updating chunk levels; we do this manually for testing
        var distanceManagerMockedConstruction = Mockito.mockConstruction(ChunkMap.DistanceManager.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        // Server level
        ServerLevel serverLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(serverLevelMock.getHeight()).thenReturn(384);
        when(serverLevelMock.getSectionsCount()).thenReturn(24);
        // This call MAGICALLY makes things not break, and we have no idea why
        // Probably due to threading issues?
        serverLevelMock.getServer().getWorldData().worldGenOptions().generateStructures();
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
        when(serverLevelMock.getChunkSource()).thenReturn(serverChunkCache);
        return new CloseableReference<>(serverChunkCache, randomStateMockedStatic, distanceManagerMockedConstruction);
    }

    /**
     * Load all dependencies for a single chunk at a given status (note that that chunk will only reach the status below)
     */
    public void singleChunkAllDependenciesForStatusVanilla(ChunkStatus status) throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache()) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkMap = serverChunkCache.chunkMap;

            var centerLevel = ChunkLevel.byStatus(status);

            var radius = ChunkLevel.MAX_LEVEL - centerLevel;

            ChunkHolder centerHolder = null;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    var holder = ((ChunkMapTestAccess) chunkMap).invokeUpdateChunkScheduling(
                        ChunkPos.asLong(x, z),
                        centerLevel + Math.max(Math.abs(x), Math.abs(z)),
                        null,
                        ChunkLevel.MAX_LEVEL + 1
                    );
                    if (x == 0 && z == 0) centerHolder = holder;
                }
            }

            var future = ((ChunkMapTestAccess) chunkMap).invokeGetChunkRangeFuture(centerHolder, status.getRange(),
                n -> ((ChunkMapTestAccess) chunkMap).invokeGetDependencyStatus(status, n)
            );

            while (!(future.isDone() || future.isCompletedExceptionally())) {
                ((ServerChunkCacheTestAccess) serverChunkCache).getMainThreadProcessor().pollTask();
            }
            var either = future.get();
            assertTrue(either.left().isPresent(), () -> status + " chunk dependency future Either should be successful, but was " + either.right().get());
        }
    }

    private Stream<ChunkStatus> chunkStatuses() {
        return ChunkStatus.getStatusList().stream();
    }

    @ParameterizedTest @MethodSource("chunkStatuses")
    public void testSingleChunkAllDependenciesForStatusVanilla(ChunkStatus status) throws Exception {
        singleChunkAllDependenciesForStatusVanilla(status);
    }

    /**
     * Load a single chunk at full status
     */
    @Test public void singleFullChunkVanilla() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache()) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkMap = serverChunkCache.chunkMap;

            var centerLevel = ChunkLevel.byStatus(ChunkStatus.FULL);

            var radius = ChunkLevel.MAX_LEVEL - centerLevel;

            ChunkHolder centerHolder = null;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    var holder = ((ChunkMapTestAccess) chunkMap).invokeUpdateChunkScheduling(
                        ChunkPos.asLong(x, z),
                        centerLevel + Math.max(Math.abs(x), Math.abs(z)),
                        null,
                        ChunkLevel.MAX_LEVEL + 1
                    );
                    if (x == 0 && z == 0) centerHolder = holder;
                }
            }

            var future = centerHolder.getOrScheduleFuture(ChunkStatus.FULL, chunkMap);

            while (!(future.isDone() || future.isCompletedExceptionally())) {
                ((ServerChunkCacheTestAccess) serverChunkCache).getMainThreadProcessor().pollTask();
            }
            var either = future.get();
            assertTrue(either.left().isPresent(), () -> "Full chunk future Either should be successful, but was " + either.right().get());
            assertInstanceOf(LevelChunk.class, either.left().get());
        }
    }

    /**
     * Load all dependencies for a single chunk at a given status (note that that chunk will only reach the status below)
     */
    public void singleChunkAllDependenciesForStatus(ChunkStatus status) throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache()) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkMap = serverChunkCache.chunkMap;

            var centerLevel = ChunkLevel.byStatus(status);

            var radius = ChunkLevel.MAX_LEVEL - centerLevel;

            ChunkHolder centerHolder = null;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    var holder = ((ChunkMapTestAccess) chunkMap).invokeCc_UpdateChunkScheduling(
                        ChunkPos.asLong(x, z),
                        centerLevel + Math.max(Math.abs(x), Math.abs(z)),
                        null,
                        ChunkLevel.MAX_LEVEL + 1
                    );
                    if (x == 0 && z == 0) centerHolder = holder;
                }
            }

            var future = ((ChunkMapTestAccess) chunkMap).invokeCc_GetChunkRangeFuture(centerHolder, status.getRange(),
                n -> ((ChunkMapTestAccess) chunkMap).invokeGetDependencyStatus(status, n)
            );

            while (!(future.isDone() || future.isCompletedExceptionally())) {
                ((ServerChunkCacheTestAccess) serverChunkCache).getMainThreadProcessor().pollTask();
            }
            var either = future.get();
            assertTrue(either.left().isPresent(), () -> status + " chunk dependency future Either should be successful, but was " + either.right().get());
        }
    }

    @ParameterizedTest @MethodSource("chunkStatuses")
    public void testSingleChunkAllDependenciesForStatus(ChunkStatus status) throws Exception {
        singleChunkAllDependenciesForStatus(status);
    }

    /**
     * Load a single chunk at full status
     */
    @Test public void singleFullChunk() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache()) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkMap = serverChunkCache.chunkMap;

            var centerLevel = ChunkLevel.byStatus(ChunkStatus.FULL);

            var radius = ChunkLevel.MAX_LEVEL - centerLevel;

            ChunkHolder centerHolder = null;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    var holder = ((ChunkMapTestAccess) chunkMap).invokeCc_UpdateChunkScheduling(
                        ChunkPos.asLong(x, z),
                        centerLevel + Math.max(Math.abs(x), Math.abs(z)),
                        null,
                        ChunkLevel.MAX_LEVEL + 1
                    );
                    if (x == 0 && z == 0) centerHolder = holder;
                }
            }

            var future = ((ChunkHolderTestAccess) centerHolder).invokeCc_GetOrScheduleFuture(ChunkStatus.FULL, chunkMap);

            while (!(future.isDone() || future.isCompletedExceptionally())) {
                ((ServerChunkCacheTestAccess) serverChunkCache).getMainThreadProcessor().pollTask();
            }
            var either = future.get();
            assertTrue(either.left().isPresent(), () -> "Full chunk future Either should be successful, but was " + either.right().get());
            assertInstanceOf(LevelChunk.class, either.left().get());
        }
    }

    // TODO might want to test 'dependencies for status' for cubes too

    /**
     * Load a single cube at full status
     */
    @Test public void singleFullCube() throws Exception {
        try(var serverChunkCacheRef = createServerChunkCache()) {
            var serverChunkCache = serverChunkCacheRef.value();
            var chunkMap = serverChunkCache.chunkMap;

            var centerLevel = ChunkLevel.byStatus(ChunkStatus.FULL);

            var radius = ChunkLevel.MAX_LEVEL - centerLevel;

            ChunkHolder centerHolder = null;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // We want the chunks intersecting this column of cubes to be loaded at the maximum level of any of those cubes;
                    // this occurs when dy=0, so we only consider x/z distance
                    int chunkDistance = Math.max(Math.abs(z), Math.abs(x));
                    for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
                        for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                            ((ChunkMapTestAccess) chunkMap).invokeCc_UpdateChunkScheduling(
                                CloPos.asLong(Coords.cubeToSection(x, sectionX), Coords.cubeToSection(z, sectionZ)),
                                centerLevel + chunkDistance,
                                null,
                                ChunkLevel.MAX_LEVEL + 1
                            );
                        }
                    }
                    for (int y = -radius; y <= radius; y++) {
                        var holder = ((ChunkMapTestAccess) chunkMap).invokeCc_UpdateChunkScheduling(
                            CloPos.asLong(x, y, z),
                            centerLevel + Math.max(Math.abs(y), chunkDistance),
                            null,
                            ChunkLevel.MAX_LEVEL + 1
                        );
                        if (x == 0 && z == 0 && y == 0) centerHolder = holder;
                    }
                }
            }

            var future = ((ChunkHolderTestAccess) centerHolder).invokeCc_GetOrScheduleFuture(ChunkStatus.FULL, chunkMap);

            while (!(future.isDone() || future.isCompletedExceptionally())) {
                ((ServerChunkCacheTestAccess) serverChunkCache).getMainThreadProcessor().pollTask();
            }
            var either = future.get();
            assertTrue(either.left().isPresent(), () -> "Full cube future Either should be successful, but was " + either.right().get());
            assertInstanceOf(LevelCube.class, either.left().get());
        }
    }
}
