package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.SectionOcclusionGraph$GraphEventsAccess;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(ChunkToCubeSet.class)
@Mixin(SectionOcclusionGraph.class)
public abstract class MixinSectionOcclusionGraph {
    // TODO onChunkLoaded
    // TODO addNeighbors
    // TODO initializeQueueForFullUpdate
    // TODO runUpdates ChunkPos.asLong
    // TODO isInViewDistance
    // TODO maybe getRelativeFrom? unsure

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(@MethodSig("onChunkLoaded(Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_onCubeLoaded();

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(SectionOcclusionGraph.class), method = @MethodSig("addNeighbors(Lnet/minecraft/client/renderer/SectionOcclusionGraph$GraphEvents;Lnet/minecraft/world/level/ChunkPos;)V"))
    private void cc_addNeighbors(SectionOcclusionGraph.GraphEvents graphEvents, CloPos cubePos) {
        var access = ((SectionOcclusionGraph$GraphEventsAccess) (Object) graphEvents);
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX() - 1, cubePos.getY(), cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY() - 1, cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY(), cubePos.getZ() - 1));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX() + 1, cubePos.getY(), cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY() + 1, cubePos.getZ()));
        access.cc_chunksWhichReceivedNeighbors().add(CubePos.asLong(cubePos.getX(), cubePos.getY(), cubePos.getZ() + 1));
    }
}
