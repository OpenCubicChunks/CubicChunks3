package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import com.google.common.collect.ImmutableList;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.exception.DasmFailedToApply;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

/**
 * {@link ChunkPyramid} represents a chain of {@link ChunkStep}s for either generating or loading a chunk. This class is the equivalent for cubes.
 */
@Dasm(GlobalSet.class)
public record CubePyramid(ImmutableList<CubeStep> steps) {
    public CubeStep getStepTo(ChunkStatus status) {
        return this.steps.get(status.getIndex());
    }

    @AddFieldToSets(containers = ChunkToCubeSet.ChunkPyramid_to_CubePyramid_redirects.class, field = "GENERATION_PYRAMID:Lnet/minecraft/world/level/chunk/status/ChunkPyramid;")
    public static CubePyramid CC_GENERATION_PYRAMID_CUBES;
    @AddFieldToSets(containers = ChunkToCubeSet.ChunkPyramid_to_CubePyramid_redirects.class, field = "LOADING_PYRAMID:Lnet/minecraft/world/level/chunk/status/ChunkPyramid;")
    public static CubePyramid CC_LOADING_PYRAMID_CUBES;

    @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, owner = @Ref(ChunkPyramid.class), value = "<clinit>")
    static void initCubePyramids() {
        throw new DasmFailedToApply();
    }

    static {
        initCubePyramids();
    }

    @Dasm(ChunkToCubeSet.class)
    @TransformFromClass(sets = ChunkToCubeSet.class, value = @Ref(ChunkPyramid.Builder.class))
    public static class Builder {}
}
