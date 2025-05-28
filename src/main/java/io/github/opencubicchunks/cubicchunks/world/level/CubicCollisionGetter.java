package io.github.opencubicchunks.cubicchunks.world.level;

import javax.annotation.Nullable;

import net.minecraft.world.level.BlockGetter;

public interface CubicCollisionGetter {
    @Nullable BlockGetter cc_getCubeForCollisions(int cubeX, int cubeY, int cubeZ);
}
