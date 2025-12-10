package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.exception.DasmFailedToApply;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTask;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;

/**
 * {@link ChunkStep} represents a single step in either chunk loading or chunk generation. This class represents a single step in cube loading or
 * generation. It is identical to {@code ChunkStep}, but stores a {@link CubeStatusTask} instead of a {@link ChunkStatusTask}.
 *
 * @param targetStatus            The status that this step corresponds to.
 * @param directDependencies      The dependencies of this individual step, by radius. Always includes the parent status at radius zero (except for
 *                                EMPTY, which has no parent), and may have additional dependencies.
 * @param accumulatedDependencies All dependencies needed to reach this step from unloaded, by radius. Effectively a combination of the
 *                                directDependencies of this step and all previous steps.
 * @param blockStateWriteRadius   The radius of chunks that can receive blockstate writes. 0 if only the center chunk can; -1 if there are no
 *                                blockstate writes. Always -1 for chunk loading.
 * @param task                    The chunk loading or generation task for this step.
 */
@Dasm(ChunkToCubeSet.class)
@TransformFromClass(sets = ChunkToCubeSet.class, value = @Ref(ChunkStep.class))
public record CubeStep(
        ChunkStatus targetStatus, ChunkDependencies directDependencies, ChunkDependencies accumulatedDependencies, int blockStateWriteRadius,
        CubeStatusTask task
) {
    public int getAccumulatedRadiusOf(ChunkStatus status) {
        throw new DasmFailedToApply();
    }

    public CompletableFuture<CubeAccess> apply(WorldGenContext worldGenContext, StaticCache3D<GenerationChunkHolder> cache, CubeAccess chunk) {
        throw new DasmFailedToApply();
    }

    @Dasm(ChunkToCubeSet.class)
    public static class Builder {
        private final ChunkStatus status;
        @AddFieldToSets(containers = ChunkToCubeSet.ChunkStep$Builder_to_CubeStep$Builder_redirects.class, field = "parent:Lnet/minecraft/world/level/chunk/status/ChunkStep;")
        private final @Nullable CubeStep parent;
        private ChunkStatus[] directDependenciesByRadius;
        private int blockStateWriteRadius = -1;
        @AddFieldToSets(containers = ChunkToCubeSet.ChunkStep$Builder_to_CubeStep$Builder_redirects.class, field = "task:Lnet/minecraft/world/level/chunk/status/ChunkStatusTask;")
        private CubeStatusTask task = (worldGenContext, step, cache, cube) -> CompletableFuture.completedFuture(cube);

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = "<init>(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V")
        protected Builder(ChunkStatus status) {
            throw new DasmFailedToApply();
        }

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = "<init>(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/chunk/status/ChunkStep;)V")
        protected Builder(ChunkStatus status, CubeStep parent) {
            throw new DasmFailedToApply();
        }

        // TODO could be mixin + DASM
        public CubeStep.Builder addRequirement(ChunkStatus requiredStatus, int radius) {
            if (requiredStatus.isOrAfter(this.status)) {
                throw new IllegalArgumentException("Status " + requiredStatus + " can not be required by " + this.status);
            } else {
                ChunkStatus[] achunkstatus = this.directDependenciesByRadius;
                int i = Coords.sectionToCubeCeil(radius) + 1;
                if (i > achunkstatus.length) {
                    this.directDependenciesByRadius = new ChunkStatus[i];
                    Arrays.fill(this.directDependenciesByRadius, requiredStatus);
                }

                for (int j = 0; j < Math.min(i, achunkstatus.length); j++) {
                    this.directDependenciesByRadius[j] = ChunkStatus.max(achunkstatus[j], requiredStatus);
                }

                return this;
            }
        }

        // TODO could be mixin + DASM
        public CubeStep.Builder blockStateWriteRadius(int blockStateWriteRadiusInSections) {
            this.blockStateWriteRadius = Coords.sectionToCubeCeil(blockStateWriteRadiusInSections);
            return this;
        }

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = "setTask(Lnet/minecraft/world/level/chunk/status/ChunkStatusTask;)Lnet/minecraft/world/level/chunk/status/ChunkStep$Builder;")
        public native CubeStep.Builder setTask(CubeStatusTask task);

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = "build()Lnet/minecraft/world/level/chunk/status/ChunkStep;")
        public native CubeStep build();

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = "buildAccumulatedDependencies()[Lnet/minecraft/world/level/chunk/status/ChunkStatus;")
        private native ChunkStatus[] buildAccumulatedDependencies();

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = "getRadiusOfParent(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)I")
        private native int getRadiusOfParent(ChunkStatus parentStatus);
    }
}
