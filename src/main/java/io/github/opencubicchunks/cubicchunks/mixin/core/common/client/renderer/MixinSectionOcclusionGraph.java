package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.access.client.ViewAreaAccess;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.SectionOcclusionGraph$GraphEventsAccess;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Dasm(ChunkToCubeSet.class)
@Mixin(SectionOcclusionGraph.class)
public abstract class MixinSectionOcclusionGraph {
    private boolean cc_isCubic = false;
    @Shadow @Nullable private ViewArea viewArea;

    @Shadow protected abstract boolean isInViewDistance(BlockPos pos, BlockPos origin);

    @Inject(method = "waitAndReset", at = @At("RETURN"))
    private void cc_onWaitAndReset(@Nullable ViewArea viewArea, CallbackInfo ci) {
        cc_isCubic = viewArea != null && ((CanBeCubic) viewArea.getLevelHeightAccessor()).cc_isCubic();
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(@MethodSig("onChunkLoaded(Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_onCubeLoaded(CubePos cubePos);

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(SectionOcclusionGraph.class), method = @MethodSig("addNeighbors(Lnet/minecraft/client/renderer/SectionOcclusionGraph$GraphEvents;Lnet/minecraft/world/level/ChunkPos;)V"))
    private void cc_addNeighbors(SectionOcclusionGraph.GraphEvents graphEvents, CubePos cubePos) {
        var access = ((SectionOcclusionGraph$GraphEventsAccess) (Object) graphEvents);
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX() - 1, cubePos.getY(), cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY() - 1, cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY(), cubePos.getZ() - 1));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX() + 1, cubePos.getY(), cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY() + 1, cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY(), cubePos.getZ() + 1));
    }

    @WrapOperation(method = "initializeQueueForFullUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;getRenderSectionAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;"))
    private @Nullable SectionRenderDispatcher.RenderSection cc_onInitializeQueueForFullUpdate_getRenderSectionAt(ViewArea instance, BlockPos pos, Operation<SectionRenderDispatcher.RenderSection> original) {
        var result = original.call(instance, pos);
        if (result == null && cc_isCubic) {
            throw new IllegalStateException("getRenderSectionAt should never return null in a cubic world");
        }
        return result;
    }

    @WrapOperation(method = "runUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;asLong(Lnet/minecraft/core/BlockPos;)J"))
    private long cc_onRunUpdates_chunkPosAsLong(BlockPos pos, Operation<Long> original) {
        if (!cc_isCubic) return original.call(pos);
        return CubePos.asLong(pos);
    }

    @Inject(method = "isInViewDistance", at = @At("HEAD"), cancellable = true)
    private void cc_onIsInViewDistance(BlockPos pos, BlockPos origin, CallbackInfoReturnable<Boolean> cir) {
        if (!cc_isCubic) return;
        int posCubeX = Coords.blockToCube(pos.getX());
        int posCubeY = Coords.blockToCube(pos.getY());
        int posCubeZ = Coords.blockToCube(pos.getZ());
        int originCubeX = Coords.blockToCube(origin.getX());
        int originCubeY = Coords.blockToCube(origin.getY());
        int originCubeZ = Coords.blockToCube(origin.getZ());
        cir.setReturnValue(CloTrackingView.cc_isInViewDistance(posCubeX, posCubeY, posCubeZ, Coords.sectionToCubeRenderDistance(this.viewArea.getViewDistance()), originCubeX, originCubeY, originCubeZ));
    }

    @Inject(method = "getRelativeFrom", at = @At("HEAD"), cancellable = true)
    private void cc_onGetRelativeFrom(BlockPos pos, SectionRenderDispatcher.RenderSection section, Direction direction, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (!cc_isCubic) return;
        // Same as vanilla logic but we don't manually check Y coordinates since that's handled by isInViewDistance now
        BlockPos relativeOrigin = section.getRelativeOrigin(direction);
        if (!this.isInViewDistance(pos, relativeOrigin)) {
            cir.setReturnValue(null);
        } else {
            cir.setReturnValue(((ViewAreaAccess) this.viewArea).cc_invokeGetRenderSectionAt(relativeOrigin));
        }
    }
}
