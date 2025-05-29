package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer.chunk;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.CubicRenderRegionCache;
import io.github.opencubicchunks.cubicchunks.mixin.access.client.SectionRenderDispatcherAccess;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import net.minecraft.Util;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class MixinSectionRenderDispatcher$RenderSection {
    @Shadow @Final SectionRenderDispatcher this$0;
    @Shadow @Final BlockPos.MutableBlockPos origin;
    @Shadow @Final private BlockPos.MutableBlockPos[] relativeOrigins;
    // For neighbor checking, we need to make sure that we offset into neighboring cubes rather than different sections in the same cube
    // (Vanilla avoids this problem since chunks are one section wide)
    private final BlockPos.MutableBlockPos[] cc_relativeOriginsForNeighborChecks = Util.make(new BlockPos.MutableBlockPos[6], p_294717_ -> {
        for(int i = 0; i < p_294717_.length; ++i) {
            p_294717_[i] = new BlockPos.MutableBlockPos();
        }
    });
    @Shadow protected abstract boolean doesChunkExistAt(BlockPos pos);

    @Inject(method = "setOrigin", at = @At("RETURN"))
    private void cc_onSetOrigin(int x, int y, int z, CallbackInfo ci) {
        for(Direction direction : Direction.values()) {
            this.cc_relativeOriginsForNeighborChecks[direction.ordinal()].set(this.origin).move(direction, CubicConstants.DIAMETER_IN_BLOCKS);
        }
    }

    @Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
    private void cc_onDoesChunkExistAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic())
            return;
        cir.setReturnValue(((CubicLevel) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_getCube(Coords.blockToCube(pos.getX()), Coords.blockToCube(pos.getY()), Coords.blockToCube(pos.getZ()), ChunkStatus.FULL, false) != null);
    }

    @Inject(method = "hasAllNeighbors", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;doesChunkExistAt(Lnet/minecraft/core/BlockPos;)Z"))
    private void cc_onHasAllNeighbors(CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic())
            return;
        cir.setReturnValue(
            this.doesChunkExistAt(this.cc_relativeOriginsForNeighborChecks[Direction.UP.ordinal()])
            && this.doesChunkExistAt(this.cc_relativeOriginsForNeighborChecks[Direction.DOWN.ordinal()])
                && this.doesChunkExistAt(this.cc_relativeOriginsForNeighborChecks[Direction.WEST.ordinal()])
                && this.doesChunkExistAt(this.cc_relativeOriginsForNeighborChecks[Direction.NORTH.ordinal()])
                && this.doesChunkExistAt(this.cc_relativeOriginsForNeighborChecks[Direction.EAST.ordinal()])
                && this.doesChunkExistAt(this.cc_relativeOriginsForNeighborChecks[Direction.SOUTH.ordinal()])
        );
    }

    @WrapOperation(method = "createCompileTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderRegionCache;createRegion(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;"))
    @Nullable private RenderChunkRegion cc_onCreateCompileTask_createRegion(RenderRegionCache instance, Level level, BlockPos pos1, BlockPos pos2, int i, boolean bool,
                                                                            Operation<RenderChunkRegion> original) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic())
            return original.call(instance, level, pos1, pos2, i, bool);
        return ((CubicRenderRegionCache) instance).cc_createRegion(level, pos1, pos2, i, bool);
    }
}
