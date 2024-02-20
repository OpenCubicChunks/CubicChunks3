package io.github.opencubicchunks.cubicchunks.test.world.level.cube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestLevelCube extends BaseTest {
    // TODO integration test for constructing from a ProtoCube

    // TODO replaceWithPacketData - probably needs to be an integration test
    // TODO (P2 or P3) postProcessGeneration - currently a method stub

    private void simpleGetSetBlockState(Random random) {
        CloPos cubePos = CloPos.cube(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        var cube = new LevelCube(mock(Answers.RETURNS_DEEP_STUBS), cubePos);
        Map<BlockPos, BlockState> states = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            var pos = cubePos.cubePos()
                .asBlockPos(random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS));
            var state = random.nextBoolean() ? Blocks.STONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
            states.put(pos, state);
            cube.setBlockState(pos, state, false);
        }

        for (var pos : states.keySet()) {
            assertEquals(states.get(pos), cube.getBlockState(pos));
        }
    }

    // Mojang's fluid stuff is so jank and half-implemented
    private void fluidState(Random random) {
        CloPos cubePos = CloPos.cube(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        var cube = new LevelCube(mock(Answers.RETURNS_DEEP_STUBS), cubePos);
        Set<BlockPos> positions = new HashSet<>();
        var state = Blocks.ANDESITE_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        for (int i = 0; i < 100; i++) {
            var pos = cubePos.cubePos()
                .asBlockPos(random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS));
            positions.add(pos);
            cube.setBlockState(pos, state, false);
        }

        for (var pos : positions) {
            assertEquals(state, cube.getBlockState(pos));
            assertEquals(Fluids.WATER.getSource(false), cube.getFluidState(pos));
        }
    }

    private void methodCallsAndBlockEntities(Random random) {
        CloPos cubePos = CloPos.cube(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        var pos = cubePos.cubePos()
            .asBlockPos(random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS));
        var cube = new LevelCube(mock(Answers.RETURNS_DEEP_STUBS), cubePos);
        BlockState state1 = spy(Blocks.FURNACE.defaultBlockState());
        BlockState state2 = spy(Blocks.STONE.defaultBlockState());

        cube.setBlockState(pos, state1, false);
        verify(state1, times(1)).onPlace(any(), eq(pos), eq(Blocks.AIR.defaultBlockState()), eq(false));
        assertNotNull(cube.getBlockEntity(pos));

        cube.setBlockState(pos, state2, false);
        verify(state1, times(1)).onRemove(any(), eq(pos), eq(state2), eq(false));
        verify(state2, times(1)).onPlace(any(), eq(pos), eq(state1), eq(false));
        // We don't check block entity is gone, since this requires more complex mocking of the Level,
        // and it is handled by BlockState.onRemove, which we check is called
    }

    @Test public void testGetSetBlockStateAndFluidState() {
        var random = new Random(-102);
        for (int i = 0; i < 100; i++) {
            simpleGetSetBlockState(random);
            fluidState(random);
            methodCallsAndBlockEntities(random);
        }
    }
}
