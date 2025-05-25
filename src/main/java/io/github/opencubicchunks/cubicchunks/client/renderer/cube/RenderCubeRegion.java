package io.github.opencubicchunks.cubicchunks.client.renderer.cube;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.utils.Coords;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class RenderCubeRegion extends RenderChunkRegion {
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    protected final RenderCube[][][] cubes;

    public RenderCubeRegion(Level level, int centerX, int centerY, int centerZ, RenderCube[][][] cubes, @Nullable net.neoforged.neoforge.client.model.data.ModelDataManager.Snapshot modelDataManager) {
        super(level, 0, 0, null);
        // TODO set modelDataManager on parent - requires an accessor mixin since we can't AT the NF constructor or field
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.cubes = cubes;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int x = Coords.blockToCube(pos.getX()) - this.centerX;
        int y = Coords.blockToCube(pos.getY()) - this.centerY;
        int z = Coords.blockToCube(pos.getZ()) - this.centerZ;
        return this.cubes[x][y][z].getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        int x = Coords.blockToCube(pos.getX()) - this.centerX;
        int y = Coords.blockToCube(pos.getY()) - this.centerY;
        int z = Coords.blockToCube(pos.getZ()) - this.centerZ;
        return this.cubes[x][y][z].getBlockState(pos).getFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        int x = Coords.blockToCube(pos.getX()) - this.centerX;
        int y = Coords.blockToCube(pos.getY()) - this.centerY;
        int z = Coords.blockToCube(pos.getZ()) - this.centerZ;
        return this.cubes[x][y][z].getBlockEntity(pos);
    }
}
