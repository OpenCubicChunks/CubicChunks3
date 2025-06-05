package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer.chunk;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.CubicRenderRegionCache;
import io.github.opencubicchunks.cubicchunks.mixin.access.client.SectionRenderDispatcherAccess;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class MixinSectionRenderDispatcher$RenderSection {
    @Shadow @Final SectionRenderDispatcher this$0;
    @Shadow private volatile long sectionNode;

    @Shadow protected abstract boolean doesChunkExistAt(long sectionPosLong);

    @Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
    private void cc_onDoesChunkExistAt(long sectionPosLong, CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic()) {
            return;
        }
        cir.setReturnValue(((CubicLevel) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_getCube(Coords.sectionToCube(SectionPos.x(sectionPosLong)), Coords.sectionToCube(SectionPos.y(sectionPosLong)), Coords.sectionToCube(SectionPos.z(sectionPosLong)), ChunkStatus.FULL, false) != null);
    }

    @Inject(method = "hasAllNeighbors", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;doesChunkExistAt(J)Z"))
    private void cc_onHasAllNeighbors(CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic())
            return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    // TODO this checks neighboring sections rather than neighboring cubes; not sure if we should be checking neighboring cubes instead?
                    if (!this.doesChunkExistAt(SectionPos.offset(this.sectionNode, dx, dy, dz))) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
        cir.setReturnValue(true);
    }

    @WrapOperation(method = "createCompileTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderRegionCache;createRegion(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/SectionPos;Z)Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;"))
    @Nullable private RenderChunkRegion cc_onCreateCompileTask_createRegion(RenderRegionCache instance, Level level, SectionPos sectionPos, boolean bool,
                                                                            Operation<RenderChunkRegion> original) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic())
            return original.call(instance, level, sectionPos, bool);
        return ((CubicRenderRegionCache) instance).cc_createRegion(level, sectionPos, bool);
    }
}
