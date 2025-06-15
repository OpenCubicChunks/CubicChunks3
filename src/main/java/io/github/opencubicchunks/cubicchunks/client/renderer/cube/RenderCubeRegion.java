package io.github.opencubicchunks.cubicchunks.client.renderer.cube;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.utils.Coords;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;

/**
 * The vanilla {@link RenderChunkRegion} stores a 3x3 of {@link RenderChunk}s and is used to get data for rendering a single {@link LevelChunkSection}
 * in the center of the 3x3.
 * Similarly, {@code RenderCubeRegion} stores a 3x3x3 of {@link RenderCube}s and is used to get data for rendering a single {@link LevelChunkSection}
 * in the center of the 3x3x3.
 */
public class RenderCubeRegion extends RenderChunkRegion {
    private final int minCubeX;
    private final int minCubeY;
    private final int minCubeZ;
    protected final RenderCube[] cubes;

    public RenderCubeRegion(
            Level level, int minCubeX, int minCubeY, int minCubeZ, RenderCube[] cubes,
            @Nullable it.unimi.dsi.fastutil.longs.Long2ObjectFunction<net.neoforged.neoforge.model.data.ModelData> modelDataSnapshot
    ) {
        super(level, 0, 0, null);
        // TODO set modelDataManager on parent - requires an accessor mixin since we can't AT the NF constructor or field
        this.minCubeX = minCubeX;
        this.minCubeY = minCubeY;
        this.minCubeZ = minCubeZ;
        this.cubes = cubes;
    }

    @Override public BlockState getBlockState(BlockPos pos) {
        return this.getCube(Coords.blockToCube(pos.getX()), Coords.blockToCube(pos.getY()), Coords.blockToCube(pos.getZ())).getBlockState(pos);
    }

    @Override public FluidState getFluidState(BlockPos pos) {
        return this.getCube(Coords.blockToCube(pos.getX()), Coords.blockToCube(pos.getY()), Coords.blockToCube(pos.getZ())).getBlockState(pos)
                .getFluidState();
    }

    @Nullable @Override public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getCube(Coords.blockToCube(pos.getX()), Coords.blockToCube(pos.getY()), Coords.blockToCube(pos.getZ())).getBlockEntity(pos);
    }

    private RenderCube getCube(int cubeX, int cubeY, int cubeZ) {
        return this.cubes[index(this.minCubeX, this.minCubeY, this.minCubeZ, cubeX, cubeY, cubeZ)];
    }

    public static int index(int minCubeX, int minCubeY, int minCubeZ, int cubeX, int cubeY, int cubeZ) {
        return cubeX - minCubeX + (cubeZ - minCubeZ) * 3 + (cubeY - minCubeY) * 9;
    }
}
