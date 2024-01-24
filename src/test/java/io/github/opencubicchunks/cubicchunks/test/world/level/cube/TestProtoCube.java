package io.github.opencubicchunks.cubicchunks.test.world.level.cube;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestProtoCube {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    private ProtoCube makeProtoCube(CloPos cubePos) {
        LevelHeightAccessor heightAccessor = mock(Answers.RETURNS_DEEP_STUBS);
        when(heightAccessor.getMinBuildHeight()).thenReturn(-(1 << 24));
        when(heightAccessor.getMaxBuildHeight()).thenReturn(1 << 24);
        when(heightAccessor.getHeight()).thenReturn(1 << 25);
        when(heightAccessor.isOutsideBuildHeight(any())).thenReturn(false);
        return new ProtoCube(cubePos, mock(Answers.RETURNS_DEEP_STUBS), heightAccessor, mock(Answers.RETURNS_DEEP_STUBS), mock(Answers.RETURNS_DEEP_STUBS));
    }

    // TODO markPosForPostprocessing - need to figure out what it actually does in order to test it

    private void simpleGetSetBlockState(Random random) {
        CloPos cubePos = CloPos.cube(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        var cube = makeProtoCube(cubePos);
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
        var cube = makeProtoCube(cubePos);
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

    @Test public void testGetSetBlockStateAndFluidState() {
        var random = new Random(-102);
        for (int i = 0; i < 100; i++) {
            simpleGetSetBlockState(random);
            fluidState(random);
        }
    }
}