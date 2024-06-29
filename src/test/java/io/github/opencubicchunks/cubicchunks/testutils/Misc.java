package io.github.opencubicchunks.cubicchunks.testutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

// TODO maybe split things off of this class instead of dumping everything in here?
public class Misc {
    public static int chebyshevDistance(Vec3i a, Vec3i b) {
        return Math.max(Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY())), Math.abs(a.getZ() - b.getZ()));
    }
    public static int chebyshevDistance(ChunkPos a, ChunkPos b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.z - b.z));
    }

    public static CloseableReference<ServerLevel> setupServerLevel() {
        MockedStatic<RandomState> randomStateMockedStatic = Mockito.mockStatic(RandomState.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        ChunkGenerator noiseBasedChunkGeneratorMock = mock(ChunkGenerator.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        when(noiseBasedChunkGeneratorMock.createBiomes(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
        when(noiseBasedChunkGeneratorMock.fillFromNoise(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
        LevelStem levelStemMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(levelStemMock.type().value().height()).thenReturn(384);
        LevelStorageSource.LevelStorageAccess levelStorageAccessMock = mock(Mockito.RETURNS_DEEP_STUBS);
        try {
            when(levelStorageAccessMock.getDimensionPath(any())).thenReturn(Files.createTempDirectory("cc_test"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CloseableReference<>(
            new ServerLevel(mock(RETURNS_DEEP_STUBS),
                // We run everything on the main thread as Mockito has race conditions when multiple threads call into it
                // (which occurs when using RETURNS_DEEP_STUBS)
                Runnable::run,
                levelStorageAccessMock,
                mock(RETURNS_DEEP_STUBS),
                mock(RETURNS_DEEP_STUBS),
                levelStemMock,
                // Need to mock an implementation of the interface, so that it also implements CubicChunkProgressListener
                Mockito.<LoggerChunkProgressListener>mock(RETURNS_DEEP_STUBS),
                false,
                0,
                mock(RETURNS_DEEP_STUBS),
                false,
                mock(RETURNS_DEEP_STUBS)),
            randomStateMockedStatic);
    }

    /**
     * Generate a LevelCube filled with random stone and dirt blocks
     */
    public static LevelCube generateRandomLevelCube(Level level, CloPos pos, Random random) {
        var cube = new LevelCube(level, pos);
        for (int i = 0; i < CubicConstants.SECTION_COUNT; i++) {
            var section = cube.getSection(i);
            for (int x = 0; x < CubicConstants.SECTION_DIAMETER; x++) {
                for (int y = 0; y < CubicConstants.SECTION_DIAMETER; y++) {
                    for (int z = 0; z < CubicConstants.SECTION_DIAMETER; z++) {
                        if (random.nextBoolean()) {
                            section.setBlockState(x, y, z, random.nextBoolean() ? Blocks.STONE.defaultBlockState() : Blocks.DIRT.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
        return cube;
    }

    public static <T> void assertDeepEquals(@Nullable T actual, @Nullable T expected) {
        assertThat(actual)
            .usingRecursiveComparison()
            .usingOverriddenEquals()
            .isEqualTo(expected);
    }
}
