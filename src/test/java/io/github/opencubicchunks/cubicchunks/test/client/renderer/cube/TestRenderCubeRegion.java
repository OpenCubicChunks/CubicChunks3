package io.github.opencubicchunks.cubicchunks.test.client.renderer.cube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCube;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCubeRegion;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestRenderCubeRegion extends BaseTest {
    // These two tests are mostly just a copy of the tests in TestLevelCube
    private void singleCubeGetBlockState(Random random) {
        CubePos cubePos = CubePos.of(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        Level levelMock = mock(Answers.RETURNS_DEEP_STUBS);
        var cube = new LevelCube(levelMock, cubePos);
        Map<BlockPos, BlockState> states = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            var pos = cubePos
                .asBlockPos(random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS));
            var state = random.nextBoolean() ? Blocks.STONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
            states.put(pos, state);
            cube.setBlockState(pos, state);
        }
        var arr = new RenderCube[27];
        arr[RenderCubeRegion.index(-1, -1, -1, 0, 0, 0)] = new RenderCube(cube);
        var renderCubeRegion = new RenderCubeRegion(levelMock, cubePos.getX()-1, cubePos.getY()-1, cubePos.getZ()-1, arr, null);

        for (var pos : states.keySet()) {
            assertEquals(states.get(pos), renderCubeRegion.getBlockState(pos));
        }
    }

    private void singleCubeGetFluidState(Random random) {
        CubePos cubePos = CubePos.of(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        Level levelMock = mock(Answers.RETURNS_DEEP_STUBS);
        var cube = new LevelCube(levelMock, cubePos);
        Set<BlockPos> positions = new HashSet<>();
        var state = Blocks.ANDESITE_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        for (int i = 0; i < 100; i++) {
            var pos = cubePos
                .asBlockPos(random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS));
            positions.add(pos);
            cube.setBlockState(pos, state);
        }

        var arr = new RenderCube[27];
        arr[RenderCubeRegion.index(-1, -1, -1, 0, 0, 0)] = new RenderCube(cube);
        var renderCubeRegion = new RenderCubeRegion(levelMock, cubePos.getX()-1, cubePos.getY()-1, cubePos.getZ()-1, arr, null);

        for (var pos : positions) {
            assertEquals(state, renderCubeRegion.getBlockState(pos));
            assertEquals(Fluids.WATER.getSource(false), renderCubeRegion.getFluidState(pos));
        }
    }

    @Disabled // TODO disabled until we can apply client-side mixins in tests properly
    @Test public void testSingleCubeGetBlockAndFluidState() {
        var random = new Random(-511);
        for (int i = 0; i < 100; i++) {
            singleCubeGetBlockState(random);
            singleCubeGetFluidState(random);
        }
    }
}
