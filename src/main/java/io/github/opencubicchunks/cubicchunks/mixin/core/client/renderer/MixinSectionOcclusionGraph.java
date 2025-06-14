package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer;

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
import net.minecraft.core.SectionPos;
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

    @Shadow protected abstract boolean isInViewDistance(long centerSectionPos, long sectionPos);

    @Inject(method = "waitAndReset", at = @At("RETURN"))
    private void cc_onWaitAndReset(@Nullable ViewArea viewArea, CallbackInfo ci) {
        cc_isCubic = viewArea != null && ((CanBeCubic) viewArea.getLevelHeightAccessor()).cc_isCubic();
    }

    @AddTransformToSets(ChunkToCubeSet.SectionOcclusionGraph_redirects.class) @TransformFromMethod(@MethodSig("onChunkReadyToRender(Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_onCubeReadyToRender(CubePos cubePos);

    @AddMethodToSets(containers = ChunkToCubeSet.SectionOcclusionGraph_redirects.class, method = @MethodSig("addNeighbors(Lnet/minecraft/client/renderer/SectionOcclusionGraph$GraphEvents;Lnet/minecraft/world/level/ChunkPos;)V"))
    private void cc_addNeighbors(SectionOcclusionGraph.GraphEvents graphEvents, CubePos cubePos) {
        var access = ((SectionOcclusionGraph$GraphEventsAccess) (Object) graphEvents);
        int cubeX = cubePos.getX();
        int cubeY = cubePos.getY();
        int cubeZ = cubePos.getZ();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubeX+dx, cubeY+dy, cubeZ+dz));
                }
            }
        }
    }

    @WrapOperation(method = "initializeQueueForFullUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;getRenderSection(J)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;"))
    private @Nullable SectionRenderDispatcher.RenderSection cc_onInitializeQueueForFullUpdate_getRenderSectionAt(ViewArea instance, long sectionPos, Operation<SectionRenderDispatcher.RenderSection> original) {
        var result = original.call(instance, sectionPos);
        if (result == null && cc_isCubic) {
            throw new IllegalStateException("getRenderSectionAt should never return null in a cubic world");
        }
        return result;
    }

    @WrapOperation(method = "runUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;sectionToChunk(J)J"))
    private long cc_onRunUpdates_sectionToChunk(long sectionPosLong, Operation<Long> original) {
        if (!cc_isCubic) return original.call(sectionPosLong);
        return CubePos.asLong(SectionPos.x(sectionPosLong), SectionPos.y(sectionPosLong), SectionPos.z(sectionPosLong));
    }

    @Inject(method = "isInViewDistance", at = @At("HEAD"), cancellable = true)
    private void cc_onIsInViewDistance(long originSectionPosLong, long sectionPosLong, CallbackInfoReturnable<Boolean> cir) {
        if (!cc_isCubic) return;
        int originCubeX = Coords.blockToCube(SectionPos.x(originSectionPosLong));
        int originCubeY = Coords.blockToCube(SectionPos.y(originSectionPosLong));
        int originCubeZ = Coords.blockToCube(SectionPos.z(originSectionPosLong));
        int posCubeX = Coords.blockToCube(SectionPos.x(sectionPosLong));
        int posCubeY = Coords.blockToCube(SectionPos.y(sectionPosLong));
        int posCubeZ = Coords.blockToCube(SectionPos.z(sectionPosLong));
        cir.setReturnValue(CloTrackingView.cc_isInViewDistance(originCubeX, originCubeY, originCubeZ, Coords.sectionToCubeRenderDistance(this.viewArea.getViewDistance()), posCubeX, posCubeY, posCubeZ));
    }

    @Inject(method = "getRelativeFrom", at = @At("HEAD"), cancellable = true)
    private void cc_onGetRelativeFrom(long sectionPosLong, SectionRenderDispatcher.RenderSection section, Direction direction, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (!cc_isCubic) return;
        // Same as vanilla logic but we don't manually check Y coordinates since that's handled by isInViewDistance now
        long sectionPosLong2 = section.getNeighborSectionNode(direction);
        if (!this.isInViewDistance(sectionPosLong, sectionPosLong2)) {
            cir.setReturnValue(null);
        } else {
            cir.setReturnValue(((ViewAreaAccess) this.viewArea).cc_invokeGetRenderSection(sectionPosLong2));
        }
    }
}
