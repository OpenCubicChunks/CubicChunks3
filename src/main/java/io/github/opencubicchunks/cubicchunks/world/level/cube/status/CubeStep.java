package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
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
 * {@link ChunkStep} represents a single step in either chunk loading or chunk generation.
 * This class represents a single step in cube loading or generation. It is identical to {@code ChunkStep}, but stores a {@link CubeStatusTask}
 * instead of a {@link ChunkStatusTask}.
 * 
 * @param targetStatus            The status that this step corresponds to.
 * @param directDependencies      The dependencies of this individual step, by radius.
 *                                Always includes the parent status at radius zero (except for EMPTY, which has no parent), and may have additional
 *                                dependencies.
 * @param accumulatedDependencies All dependencies needed to reach this step from unloaded, by radius.
 *                                Effectively a combination of the directDependencies of this step and all previous steps.
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
        @AddFieldToSets(containers = ChunkToCubeSet.ChunkStep$Builder_to_CubeStep$Builder_redirects.class, field = @FieldSig(type = @Ref(CubeStep.class), name = "parent"))
        @Nullable private final CubeStep parent;
        private ChunkStatus[] directDependenciesByRadius;
        private int blockStateWriteRadius = -1;
        @AddFieldToSets(containers = ChunkToCubeSet.ChunkStep$Builder_to_CubeStep$Builder_redirects.class, field = @FieldSig(type = @Ref(ChunkStatusTask.class), name = "task"))
        private CubeStatusTask task = (worldGenContext, step, cache, cube) -> CompletableFuture.completedFuture(cube);

        // TODO these should be dasm-copied but for some reason it didn't work when I tried
        protected Builder(ChunkStatus status) {
            if (status.getParent() != status) {
                throw new IllegalArgumentException("Not starting with the first status: " + status);
            } else {
                this.status = status;
                this.parent = null;
                this.directDependenciesByRadius = new ChunkStatus[0];
            }
        }

        protected Builder(ChunkStatus status, CubeStep parent) {
            if (parent.targetStatus.getIndex() != status.getIndex() - 1) {
                throw new IllegalArgumentException("Out of order status: " + status);
            } else {
                this.status = status;
                this.parent = parent;
                this.directDependenciesByRadius = new ChunkStatus[] { parent.targetStatus };
            }
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

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = @MethodSig("setTask(Lnet/minecraft/world/level/chunk/status/ChunkStatusTask;)Lnet/minecraft/world/level/chunk/status/ChunkStep$Builder;"))
        public native CubeStep.Builder setTask(CubeStatusTask task);

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = @MethodSig("build()Lnet/minecraft/world/level/chunk/status/ChunkStep;"))
        public native CubeStep build();

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = @MethodSig("buildAccumulatedDependencies()[Lnet/minecraft/world/level/chunk/status/ChunkStatus;"))
        private native ChunkStatus[] buildAccumulatedDependencies();

        @TransformFromMethod(owner = @Ref(ChunkStep.Builder.class), value = @MethodSig("getRadiusOfParent(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)I"))
        private native int getRadiusOfParent(ChunkStatus parentStatus);
    }
}
