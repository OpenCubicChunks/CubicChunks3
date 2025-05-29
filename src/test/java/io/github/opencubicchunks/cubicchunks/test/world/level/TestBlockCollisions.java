package io.github.opencubicchunks.cubicchunks.test.world.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;

public class TestBlockCollisions extends BaseTest {
    record CubeGetter(TriFunction<Integer, Integer, Integer, BlockGetter> getter) {
        public BlockGetter cc_getCubeForCollisions(int cubeX, int cubeY, int cubeZ) {
            return getter.apply(cubeX, cubeY, cubeZ);
        }
    }

    record DummyBlockGetter(BlockState state) {
        public BlockState getBlockState(BlockPos pos) {
            return state;
        }

        static BlockGetter mockBlockGetter(BlockState state) {
            var dummyBlockGetter = new DummyBlockGetter(state);
            BlockGetter blockGetter = mock();
            when(blockGetter.getBlockState(any())).then(AdditionalAnswers.delegatesTo(dummyBlockGetter));
            return blockGetter;
        }
    }

    @Test public void testSimpleCCBlockCollisions() {
        Level level = mock();
        when(((CanBeCubic) level).cc_isCubic()).thenReturn(true);
        var blockGetter = DummyBlockGetter.mockBlockGetter(Blocks.STONE.defaultBlockState());
        var cubePos = CubePos.of(7, -13, 4);
        var cubeGetter = new CubeGetter((cubeX, cubeY, cubeZ) -> {
            if (cubePos.getX() == cubeX && cubePos.getY() == cubeY && cubePos.getZ() == cubeZ)
                return blockGetter;
            throw new IllegalStateException();
        });
        when(((CubicLevel) level).cc_getCubeForCollisions(anyInt(), anyInt(), anyInt())).then(AdditionalAnswers.delegatesTo(cubeGetter));

        var random = new Random(-8);
        for (int i = 0; i < 500; i++) {
            // We don't generate positions on the edge of the cube here since BlockCollisions tries to reach into neighboring cubes in that case
            var blockPos = cubePos.asBlockPos(random.nextInt(1, CubicConstants.DIAMETER_IN_BLOCKS-1), random.nextInt(1, CubicConstants.DIAMETER_IN_BLOCKS-1), random.nextInt(1, CubicConstants.DIAMETER_IN_BLOCKS-1));
            int[] c = new int[] { 0 };
            var blockCollisions = new BlockCollisions<Void>(level, null, AABB.unitCubeFromLowerCorner(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())).inflate(-0.2), false, (pos, voxelShape) -> {
                assertEquals(blockPos, pos);
                c[0]++;
                return null;
            });
            blockCollisions.forEachRemaining((v) -> {});
            assertEquals(1, c[0], "block collision callback should be called exactly once");
        }
    }

    @Test public void testCCBlockCollisionsAcrossCubeBorders() {
        Level level = mock();
        when(((CanBeCubic) level).cc_isCubic()).thenReturn(true);
        var emptyBlockGetter = DummyBlockGetter.mockBlockGetter(Blocks.AIR.defaultBlockState());
        var solidBlockGetter = DummyBlockGetter.mockBlockGetter(Blocks.STONE.defaultBlockState());
        var cubeGetter = new CubeGetter((cubeX, cubeY, cubeZ) -> {
            // 3d checkerboard of empty and solid cubes
            return ((cubeX + cubeY + cubeZ) & 1) == 0 ? emptyBlockGetter : solidBlockGetter;
        });
        when(((CubicLevel) level).cc_getCubeForCollisions(anyInt(), anyInt(), anyInt())).then(AdditionalAnswers.delegatesTo(cubeGetter));

        var cubePos = CubePos.of(-3, -1, -8);
        var blockPos = cubePos.asBlockPos(CubicConstants.DIAMETER_IN_BLOCKS-1, CubicConstants.DIAMETER_IN_BLOCKS-1, CubicConstants.DIAMETER_IN_BLOCKS-1);
        // AABB that reaches across the corner between 8 cubes
        var aabb = AABB.unitCubeFromLowerCorner(new Vec3(blockPos.getX()+0.5, blockPos.getY()+0.5, blockPos.getZ()+0.5)).inflate(-0.2);
        int[] c = new int[] { 0 };
        var blockCollisions = new BlockCollisions<Void>(level, null, aabb, false, (pos, voxelShape) -> {
            var collidedCubePos = CubePos.from(pos);
            System.out.println(pos + " " + collidedCubePos);
            assertEquals(1, ((collidedCubePos.getX() + collidedCubePos.getY() + collidedCubePos.getZ()) & 1), "should only collide with positions in solid cubes");
            c[0]++;
            return null;
        });
        blockCollisions.forEachRemaining((v) -> {});
        assertEquals(4, c[0], "block collision callback should be called exactly four times (four of eight cubes are solid at corner of 3d checkerboard");
    }
}
