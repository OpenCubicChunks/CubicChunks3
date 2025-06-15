package io.github.opencubicchunks.cubicchunks.world.level;

import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;

public interface CubicLevel extends CubicLevelAccessor {
    LevelCube cc_getCubeAt(BlockPos blockPos);

    LevelCube cc_getCube(int x, int y, int z);
}
