package io.github.opencubicchunks.cubicchunks.world.level;

import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;

public interface CubicLevel extends CubicLevelAccessor {
    LevelCube cc_getCubeAt(BlockPos blockPos);
}
