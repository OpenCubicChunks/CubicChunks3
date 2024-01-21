package io.github.opencubicchunks.cubicchunks.world.level;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface CubicLevelReader {

    @Nullable
    CubeAccess cc_getCube(int x, int y, int z, ChunkStatus chunkStatus, boolean forceLoad);

    @Deprecated
    boolean cc_hasCube(int x, int y, int z);

    default CubeAccess cc_getCube(BlockPos blockPos) {
        return this.cc_getCube(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    default CubeAccess cc_getCube(int x, int y, int z) {
        return this.cc_getCube(x, y, z, ChunkStatus.FULL, true);
    }

    default CubeAccess cc_getCube(int x, int y, int z, ChunkStatus chunkStatus) {
        return this.cc_getCube(x, y, z, chunkStatus, true);
    }

    // TODO: This might break if we try to calculate a falling block outside of loaded cubes, since BlockCollisions uses chunks to do some of its math
//    @Nullable
//    @Override
//    default BlockGetter getCubeForCollisions(int x, int y, int z) {
//        return this.getCube(x, y, z, ChunkStatus.EMPTY, false);
//    }

    @Deprecated
    default boolean cc_hasCubeAt(int x, int y, int z) {
        return this.cc_hasCube(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(y), SectionPos.blockToSectionCoord(z));
    }

    @Deprecated
    default boolean cc_hasCubeAt(BlockPos pos) {
        return this.cc_hasCubeAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Deprecated
    default boolean cc_hasCubesAt(BlockPos from, BlockPos to) {
        return this.cc_hasCubesAt(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }

    @Deprecated
    default boolean cc_hasCubesAt(int fromX, int toX, int fromY, int toY, int fromZ, int toZ) {
        int sectionFromX = SectionPos.blockToSectionCoord(fromX);
        int sectionToX = SectionPos.blockToSectionCoord(toX);
        int sectionFromY = SectionPos.blockToSectionCoord(fromY);
        int sectionToY = SectionPos.blockToSectionCoord(toY);
        int sectionFromZ = SectionPos.blockToSectionCoord(fromZ);
        int sectionToZ = SectionPos.blockToSectionCoord(toZ);

        for(int i1 = sectionFromX; i1 <= sectionToX; ++i1) {
            for(int j1 = sectionFromY; j1 <= sectionToY; ++j1) {
                for(int k1 = sectionFromZ; k1 <= sectionToZ; ++k1) {
                    if (!this.cc_hasCube(i1, j1, k1)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
