package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
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

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkPyramid.class), field = @FieldSig(type = @Ref(ChunkPyramid.class), name = "GENERATION_PYRAMID"))
    public static CubePyramid CC_GENERATION_PYRAMID_CUBES;
    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkPyramid.class), field = @FieldSig(type = @Ref(ChunkPyramid.class), name = "LOADING_PYRAMID"))
    public static CubePyramid CC_LOADING_PYRAMID_CUBES;

    @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, owner = @Ref(ChunkPyramid.class), value = @MethodSig("<clinit>()V"))
    static void initCubePyramids() {
        throw new IllegalStateException("DASM failed to apply");
    }

    static {
        initCubePyramids();
    }

    @Dasm(ChunkToCubeSet.class)
    @TransformFromClass(sets = ChunkToCubeSet.class, value = @Ref(ChunkPyramid.Builder.class))
    public static class Builder {}
}
