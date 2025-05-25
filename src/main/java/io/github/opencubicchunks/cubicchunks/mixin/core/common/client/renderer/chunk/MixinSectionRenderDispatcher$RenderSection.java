package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer.chunk;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class MixinSectionRenderDispatcher$RenderSection {
    @Shadow @Final SectionRenderDispatcher this$0;
    @Shadow @Final private BlockPos.MutableBlockPos[] relativeOrigins;
    @Shadow protected abstract boolean doesChunkExistAt(BlockPos pos);

    @Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
    public void cc_onDoesChunkExistAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) this$0).cc_isCubic()) return;
        cir.setReturnValue(((CubicLevel) this$0).cc_getCube(Coords.blockToCube(pos.getX()), Coords.blockToCube(pos.getY()), Coords.blockToCube(pos.getZ()), ChunkStatus.FULL, false) != null);
    }

    @Inject(method = "hasAllNeighbors", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;doesChunkExistAt(Lnet/minecraft/core/BlockPos;)Z"))
    public void cc_onHasAllNeighbors(CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) this$0).cc_isCubic()) return;
        if (this.doesChunkExistAt(this.relativeOrigins[Direction.UP.ordinal()])
            && this.doesChunkExistAt(this.relativeOrigins[Direction.DOWN.ordinal()])) return;
        // Fail in cubic worlds where there aren't chunks above and below
        cir.setReturnValue(false);
    }
}
