package io.github.opencubicchunks.cubicchunks.test.world.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelReader;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicLevelReader extends BaseTest {
    public static class DummyLevelReader implements CubicLevelReader {
        private CubeAccess ca;
        private LongSet cubePositions;

        public DummyLevelReader(CubeAccess ca, LongSet cubePositions) {
            this.ca = ca;
            this.cubePositions = cubePositions;
        }

        @Override public @Nullable CubeAccess cc_getCube(int cubeX, int cubeY, int cubeZ, ChunkStatus chunkStatus, boolean forceLoad) {
            return cubePositions.contains(CubePos.asLong(cubeX, cubeY, cubeZ)) ? ca : null;
        }

        @Override public boolean cc_hasCube(int cubeX, int cubeY, int cubeZ) {
            return cubePositions.contains(CubePos.asLong(cubeX, cubeY, cubeZ));
        }
    }

    // This test is pretty trivial since it just tests methods that are effectively wrappers for hasCube and getCube
    @Test
    public void testSingleCubeMethods() {
        var random = new Random(9264);
        var cubePositions = new LongAVLTreeSet();
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (random.nextBoolean()) {
                        cubePositions.add(CubePos.asLong(x, y, z));
                    }
                }
            }
        }
        var levelReader = new DummyLevelReader(mock(), cubePositions);
        var radius = 6 * CubicConstants.DIAMETER_IN_BLOCKS;
        for (int i = 0; i < 500; i++) {
            var pos = new BlockPos(random.nextInt(2 * radius) - radius, random.nextInt(2 * radius) - radius, random.nextInt(2 * radius) - radius);
            var expectedResult = cubePositions.contains(CubePos.asLong(pos));
            assertEquals(expectedResult, levelReader.cc_getCube(pos) != null);
            assertEquals(expectedResult, levelReader.cc_hasCubeAt(pos));
        }
    }
    // TODO (P2): test cc_getCubeForCollisions

    @Test
    public void testHasCubesAt() {
        var random = new Random(9264);
        var cubePositions = new LongAVLTreeSet();
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    cubePositions.add(CubePos.asLong(x, y, z));
                }
            }
        }
        var levelReader = new DummyLevelReader(mock(), cubePositions);
        var radius = 6 * CubicConstants.DIAMETER_IN_BLOCKS;

        for (int i = 0; i < 2000; i++) {
            var pos1 = new BlockPos(random.nextInt(2 * radius) - radius, random.nextInt(2 * radius) - radius, random.nextInt(2 * radius) - radius);
            var pos2 = new BlockPos(random.nextInt(2 * radius) - radius, random.nextInt(2 * radius) - radius, random.nextInt(2 * radius) - radius);
            var minPos = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            var maxPos = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
            var expectedResult = Coords.blockToCube(minPos.getX()) >= -4 && Coords.blockToCube(maxPos.getX()) <= 4
                    && Coords.blockToCube(minPos.getY()) >= -4 && Coords.blockToCube(maxPos.getY()) <= 4 && Coords.blockToCube(minPos.getZ()) >= -4
                    && Coords.blockToCube(maxPos.getZ()) <= 4;
            assertEquals(expectedResult, levelReader.cc_hasCubesAt(minPos, maxPos));
        }
    }
}
