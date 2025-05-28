package io.github.opencubicchunks.cubicchunks.world.level;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface CubicLevelReader extends CubicCollisionGetter {
    @Nullable CubeAccess cc_getCube(int cubeX, int cubeY, int cubeZ, ChunkStatus chunkStatus, boolean forceLoad);

    boolean cc_hasCube(int cubeX, int cubeY, int cubeZ);

    @Nullable default CubeAccess cc_getCube(BlockPos blockPos) {
        return this.cc_getCube(Coords.blockToCube(blockPos.getX()), Coords.blockToCube(blockPos.getY()), Coords.blockToCube(blockPos.getZ()));
    }

    @Nullable default CubeAccess cc_getCube(int cubeX, int cubeY, int cubeZ) {
        return this.cc_getCube(cubeX, cubeY, cubeZ, ChunkStatus.FULL, true);
    }

    @Nullable default CubeAccess cc_getCube(int cubeX, int cubeY, int cubeZ, ChunkStatus requiredStatus) {
        return this.cc_getCube(cubeX, cubeY, cubeZ, requiredStatus, true);
    }

    @Override @Nullable default BlockGetter cc_getCubeForCollisions(int cubeX, int cubeY, int cubeZ) {
        return this.cc_getCube(cubeX, cubeY, cubeZ, ChunkStatus.EMPTY, false);
    }

    default boolean cc_hasCubeAt(int blockX, int blockY, int blockZ) {
        return this.cc_hasCube(Coords.blockToCube(blockX), Coords.blockToCube(blockY), Coords.blockToCube(blockZ));
    }

    default boolean cc_hasCubeAt(BlockPos pos) {
        return this.cc_hasCubeAt(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean cc_hasCubesAt(BlockPos from, BlockPos to) {
        return this.cc_hasCubesAt(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }

    default boolean cc_hasCubesAt(int minBlockX, int minBlockY, int minBlockZ, int maxBlockX, int maxBlockY, int maxBlockZ) {
        int minX = Coords.blockToCube(minBlockX);
        int maxX = Coords.blockToCube(maxBlockX);
        int minY = Coords.blockToCube(minBlockY);
        int maxY = Coords.blockToCube(maxBlockY);
        int minZ = Coords.blockToCube(minBlockZ);
        int maxZ = Coords.blockToCube(maxBlockZ);

        for(int x = minX; x <= maxX; ++x) {
            for(int z = minZ; z <= maxZ; ++z) {
                for(int y = minY; y <= maxY; ++y) {
                    if (!this.cc_hasCube(x, y, z)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
