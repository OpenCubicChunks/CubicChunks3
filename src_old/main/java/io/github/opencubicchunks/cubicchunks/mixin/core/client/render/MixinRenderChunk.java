package io.github.opencubicchunks.cubicchunks.mixin.core.client.render;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.mixin.access.client.ChunkRenderDispatcherAccess;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public abstract class MixinRenderChunk {
    @SuppressWarnings("target") @Shadow @Final ChunkRenderDispatcher this$0;

    @Shadow @Final private BlockPos.MutableBlockPos[] relativeOrigins;

    @Shadow protected abstract double getDistToPlayerSqr();

    @Shadow protected abstract boolean doesChunkExistAt(BlockPos blockPos);

    /**
     * Add checks for the cube at that pos
     */
    @Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
    private void doesChunkAndCubeExistAt(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if (!((CubicLevelHeightAccessor) level).isCubic()) {
                return;
            }
        }

        cir.setReturnValue(
            ((ChunkRenderDispatcherAccess) this$0).getLevel().getChunk(
                Coords.blockToSection(blockPos.getX()),
                Coords.blockToSection(blockPos.getZ()),
                ChunkStatus.FULL,
                false) != null &&
                ((CubicLevelAccessor) ((ChunkRenderDispatcherAccess) this$0).getLevel()).getCube(
                    Coords.blockToCube(blockPos.getX()),
                    Coords.blockToCube(blockPos.getY()),
                    Coords.blockToCube(blockPos.getZ()),
                    ChunkStatus.FULL,
                    false) != null
        );
    }

    /**
     * Add checks for above and below neighbors
     */
    @Inject(method = "hasAllNeighbors", at = @At("HEAD"), cancellable = true)
    private void onHasAllNeighbors(CallbackInfoReturnable<Boolean> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if (!((CubicLevelHeightAccessor) level).isCubic()) {
                return;
            }
        }

        if (!(this.getDistToPlayerSqr() > 576.0D)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(
                this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.UP.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.DOWN.ordinal()])
            );
        }
    }
}
