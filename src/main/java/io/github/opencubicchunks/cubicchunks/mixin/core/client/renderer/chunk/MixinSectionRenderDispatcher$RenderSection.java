package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer.chunk;

import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.access.client.SectionRenderDispatcherAccess;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code RenderSection} represents a single {@link LevelChunkSection} to be rendered.
 * We modify it to check cubes instead of chunks when validating the presence of neighboring sections in cubic levels.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class MixinSectionRenderDispatcher$RenderSection {
    @Shadow @Final SectionRenderDispatcher this$0;
    @Shadow volatile long sectionNode;

    @Shadow protected abstract boolean doesChunkExistAt(long sectionPosLong);

    @Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
    private void cc_onDoesChunkExistAt(long sectionPosLong, CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic()) {
            return;
        }
        // TODO (P2) lighting: also check the cubic equivalent of LevelLightEngine.lightOnInColumn here (see vanilla doesChunkExistAt method) -
        // sections currently sometimes fail to render due to this missing check
        cir.setReturnValue(((CubicLevel) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_getCube(
                Coords.sectionToCube(SectionPos.x(sectionPosLong)), Coords.sectionToCube(SectionPos.y(sectionPosLong)),
                Coords.sectionToCube(SectionPos.z(sectionPosLong)), ChunkStatus.FULL, false) != null);
    }

    /**
     * Check vertically offset neighbors as well as purely horizontal neighbors in cubic worlds
     */
    @Inject(method = "hasAllNeighbors", cancellable = true, at = @At("HEAD"))
    private void cc_onHasAllNeighbors(CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) ((SectionRenderDispatcherAccess) this$0).cc_getLevel()).cc_isCubic()) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    if (!this.doesChunkExistAt(SectionPos.offset(this.sectionNode, dx, dy, dz))) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
        cir.setReturnValue(true);
    }
}
