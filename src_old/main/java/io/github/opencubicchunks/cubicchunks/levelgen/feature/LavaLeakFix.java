package io.github.opencubicchunks.cubicchunks.levelgen.feature;

import java.util.Random;

import com.mojang.serialization.Codec;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.levelgen.CubeWorldGenRegion;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.CubeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

//TODO: There has to be a better way to do this in our Cubic Aquifer.
public class LavaLeakFix extends Feature<NoneFeatureConfiguration> {

    public static final Direction[] DIRECTIONS = { Direction.DOWN, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH };

    public LavaLeakFix(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!(context.level() instanceof CubeWorldGenRegion level)) {
            return false;
        }
        if (context.level().getLevel().dimension() != Level.NETHER) {
            return false;
        }
        CubeAccess cube = level.getCube(level.getMainCubeX(), level.getMainCubeY(), level.getMainCubeZ());
        CubePos cubePos = cube.getCubePos();
        Random random = context.random();

        ChunkGenerator generator = context.chunkGenerator();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int localY = 0; localY < CubicConstants.DIAMETER_IN_BLOCKS; localY++) {
            for (int localX = 0; localX < CubicConstants.DIAMETER_IN_BLOCKS; localX++) {
                for (int localZ = 0; localZ < CubicConstants.DIAMETER_IN_BLOCKS; localZ++) {
                    mutable.set(localX, localY, localZ);
                    if (cube.getBlockState(new BlockPos(localX, localY, localZ)).getBlock() != Blocks.LAVA) {
                        continue;
                    }

                    //TODO: Try detect the topState and underState somehow? Used to be done by fetching the surface builder
                    BlockState block = Blocks.NETHERRACK.defaultBlockState();

                    checkDirectionsAndPreventLeaking(level, block, block, cubePos, random, generator, mutable, localX, localY, localZ);
                }
            }
        }
        return false;
    }

    private void checkDirectionsAndPreventLeaking(CubeWorldGenRegion level, BlockState topState, BlockState underState, CubePos cubePos, Random random, ChunkGenerator generator,
                                                  BlockPos.MutableBlockPos mutable, int localX, int localY, int localZ) {

        for (Direction direction : DIRECTIONS) {
            BlockState blockState = level.getBlockState(mutable.set(Coords.localToBlock(cubePos.getX(), localX),
                Coords.localToBlock(cubePos.getY(), localY), Coords.localToBlock(cubePos.getZ(), localZ)).move(direction));
            if (blockState.isAir()) {
                if (direction == Direction.DOWN) {
                    //TODO: This may not always be netherrack
                    level.setBlock(mutable, Blocks.NETHERRACK.defaultBlockState(), 2);
                } else {
                    if (random.nextInt(5) == 0) {
                        continue;
                    }

                    BlockState blockStateDown = level.getBlockState(mutable.move(Direction.DOWN));
                    mutable.move(Direction.UP);
                    if (blockStateDown.getBlock() != topState.getBlock()) {
                        level.setBlock(mutable, level.getBlockState(mutable.offset(0, 1, 0)).isAir() ? topState : underState, 2);
                    } else {
                        level.setBlock(mutable, underState, 2);
                    }
                }
            }
        }
    }
}
