package io.github.opencubicchunks.cubicchunks.test.world.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.test.world.level.TestCubicLevelReader;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.entity.EntityCubePosGetter;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelReader;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.AdditionalAnswers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestEntity extends BaseTest {
    @Test public void testCubePos() {
        Level level = mock();
        when(((CanBeCubic) level).cc_isCubic()).thenReturn(true);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        // We use a giant for testing because it's funny
        var entity = EntityType.GIANT.create(level);
        var random = new Random(742);
        for (int i = 0; i < 1000; i++) {
            var pos = new BlockPos(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            assertEquals(CloPos.cube(pos), ((EntityCubePosGetter) entity).cc_cubePosition());
        }
    }

    // TODO (P2) test teleportToWithTicket

    // Not really a unit test since it depends on CubicLevelReader, but touchingUnloadedChunk is essentially just a wrapper around LevelReader.hasChunksAt anyway
    @Test public void testTouchingUnloadedChunk() {
        var random = new Random(743);
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
        Level level = mock();
        var levelReader = new TestCubicLevelReader.DummyLevelReader(mock(), cubePositions);
        when(((CanBeCubic) level).cc_isCubic()).thenReturn(true);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        when(((CubicLevelReader) level).cc_hasCubesAt(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).then(AdditionalAnswers.delegatesTo(levelReader));
        var entity = EntityType.GIANT.create(level);
        for (int i = 0; i < 500; i++) {
            var cubePos = CubePos.of(random.nextInt(10)-5, random.nextInt(10)-5, random.nextInt(10)-5);
            // Horizontal center of cube, at bottom
            var blockPos = cubePos.asBlockPos(CubicConstants.DIAMETER_IN_BLOCKS/2, 1, CubicConstants.DIAMETER_IN_BLOCKS/2);
            entity.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            assertEquals(!cubePositions.contains(cubePos.asLong()), entity.touchingUnloadedChunk());
            // Horizontal center of cube, at top (overlapping cube above)
            blockPos = cubePos.asBlockPos(CubicConstants.DIAMETER_IN_BLOCKS/2, CubicConstants.DIAMETER_IN_BLOCKS - 1, CubicConstants.DIAMETER_IN_BLOCKS/2);
            entity.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            assertEquals(!(cubePositions.contains(cubePos.asLong()) && cubePositions.contains(CubePos.asLong(cubePos.getX(), cubePos.getY() + 1, cubePos.getZ()))), entity.touchingUnloadedChunk());
        }
    }
}
