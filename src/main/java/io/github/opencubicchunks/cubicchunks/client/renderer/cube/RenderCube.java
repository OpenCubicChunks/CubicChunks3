package io.github.opencubicchunks.cubicchunks.client.renderer.cube;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Whole class redirect
@TransformFromClass(value = @Ref(RenderChunk.class), sets = ChunkToCubeSet.class)
public class RenderCube {
    // Methods copied by DASM
    public RenderCube(LevelCube wrapped) {
        throw new IllegalStateException("DASM failed to apply");
    }
    public native BlockEntity getBlockEntity(BlockPos pos);
    // This method is modified with mixin
    public native BlockState getBlockState(BlockPos pos);
}
