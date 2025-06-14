package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.annotation.Public;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.GenerationChunkHolderAccess;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.server.level.CloGenerationTask;
import io.github.opencubicchunks.cubicchunks.server.level.GeneratingCubeMap;
import io.github.opencubicchunks.cubicchunks.server.level.GenerationCloHolder;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubePyramid;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link ChunkGenerationTask} handles loading a given chunk at a given {@link ChunkStatus}, including generation if required, and loading neighboring chunks as required.
 * <p/>
 * We modify it to support cube loading as well, loading both neighboring cubes and chunks as required, and preserving chunk-cube load order invariants
 * (cube status never exceeds the status of any chunks intersecting it)
 */
@Dasm(GlobalSet.class)
@Mixin(ChunkGenerationTask.class)
public abstract class MixinChunkGenerationTask implements CloGenerationTask {
    @Shadow @Final private ChunkPos pos;
    @Shadow @Final private GeneratingChunkMap chunkMap;
    @Shadow @Nullable private ChunkStatus scheduledStatus;
    @Shadow @Final public ChunkStatus targetStatus;
    @Shadow private volatile boolean markedForCancellation;
    @Shadow @Final private StaticCache2D<GenerationChunkHolder> cache;
    @AddFieldToSets(containers = ChunkToCubeSet.ChunkGenerationTask_redirects.class, field = @FieldSig(type = @Ref(ChunkPos.class), name = "pos"))
    private CubePos cc_cubePos;
    // scheduledChunkStatus must be one status higher than the scheduled status for cubes until the target status is reached, to ensure load order invariants are preserved
    // we use the vanilla field for cube status, since that is the status of the actual cube that is being generated
    @Nullable public ChunkStatus cc_scheduledChunkStatus;
    @AddFieldToSets(containers = ChunkToCubeSet.ChunkGenerationTask_redirects.class, field = @FieldSig(type = @Ref(StaticCache2D.class), name = "cache"))
    private StaticCache3D<GenerationChunkHolder> cc_cubeCache;

    private GeneratingCubeMap cc_getGeneratingCubeMap() {
        return ((GeneratingCubeMap) chunkMap);
    }

    @Override public CloPos cc_getCloPos() {
        if (cc_cubePos != null) {
            return CloPos.cube(cc_cubePos);
        }
        return CloPos.chunk(pos);
    }

    /**
     * Factory method to create a {@code ChunkGenerationTask} for a cube.
     */
    @AddMethodToSets(containers = ChunkToCubeSet.ChunkGenerationTask_redirects.class, method = @MethodSig("create(Lnet/minecraft/server/level/GeneratingChunkMap;Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    @Public private static ChunkGenerationTask cc_createCubeGenerationTask(GeneratingChunkMap chunkMap, ChunkStatus targetStatus, CubePos pos) {
        int cubeRadius = CubePyramid.CC_GENERATION_PYRAMID_CUBES.getStepTo(targetStatus).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
        int cubeDiameter = cubeRadius * 2 + 1;
        int chunkDiameter = cubeDiameter * CubicConstants.DIAMETER_IN_SECTIONS;
        // We directly use the StaticCache2D constructor, as the `create` factory method only allows for odd dimensions, and `chunkDiameter` is even (for cube sizes greater than 16)
        StaticCache2D<GenerationChunkHolder> staticcache2d = new StaticCache2D<>(
            Coords.cubeToSection(pos.getX() - cubeRadius, 0), Coords.cubeToSection(pos.getZ() - cubeRadius, 0), chunkDiameter, chunkDiameter, (x, z) -> chunkMap.acquireGeneration(ChunkPos.asLong(x, z))
        );
        var chunkGenerationTask = new ChunkGenerationTask(chunkMap, targetStatus, null, staticcache2d);
        ((MixinChunkGenerationTask) (Object) chunkGenerationTask).cc_cubePos = pos;
        ((MixinChunkGenerationTask) (Object) chunkGenerationTask).cc_cubeCache = StaticCache3D.create(
            pos.getX(), pos.getY(), pos.getZ(), cubeRadius, (x, y, z) -> chunkMap.acquireGeneration(CubePos.asLong(x, y, z))
        );
        return chunkGenerationTask;
    }

    @AddMethodToSets(containers = ChunkToCloSet.ChunkGenerationTask_redirects.class, method = @MethodSig("create(Lnet/minecraft/server/level/GeneratingChunkMap;Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    @Public private static ChunkGenerationTask cc_createCubeGenerationTask(GeneratingChunkMap chunkMap, ChunkStatus targetStatus, CloPos pos) {
        if (pos.isCube()) {
            return cc_createCubeGenerationTask(chunkMap, targetStatus, pos.cubePos());
        } else {
            return create(chunkMap, targetStatus, pos.chunkPos());
        }
    }

    @Inject(method = "scheduleNextLayer", at = @At("HEAD"), cancellable = true)
    private void cc_onScheduleNextLayer(CallbackInfo ci) {
        if (cc_cubePos != null) {
            ci.cancel();
            cc_scheduleNextLayer();
        }
    }

    /**
     * Cube equivalent to {@code scheduleNextLayer}.
     * Loads chunks one status higher than cubes until chunks have reached the target status, as otherwise cubes could reach a status before their intersecting chunks do.
     */
    private void cc_scheduleNextLayer() {
        ChunkStatus nextChunkStatus;
        ChunkStatus nextCubeStatus = null;
        // First two branches of this `if` are equivalent to the vanilla method; we are loading chunks at EMPTY so cubes are not involved yet
        if (this.cc_scheduledChunkStatus == null) {
            nextChunkStatus = ChunkStatus.EMPTY;
        } else if (!this.needsGeneration && this.cc_scheduledChunkStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
            this.needsGeneration = true;
            nextChunkStatus = ChunkStatus.EMPTY;
        } else {
            // All chunks have reached `cc_scheduledChunkStatus`, so cubes can now be scheduled at that status
            nextCubeStatus = this.cc_scheduledChunkStatus;
            if (nextCubeStatus.isOrAfter(this.targetStatus)) {
                // If nextCubeStatus is at or after targetStatus, it means chunks have already reached targetStatus and we just need cubes
                nextChunkStatus = null;
            } else {
                // Simultaneously schedule chunks one status ahead of cubes
                nextChunkStatus = ChunkStatus.getStatusList().get(this.cc_scheduledChunkStatus.getIndex() + 1);
            }
        }

        this.cc_scheduleLayer(nextChunkStatus, nextCubeStatus, this.needsGeneration);
        this.scheduledStatus = nextCubeStatus;
        if (nextChunkStatus != null) { // If nextChunkStatus is null, the scheduled chunk status did not change
            this.cc_scheduledChunkStatus = nextChunkStatus;
        }
    }

    /**
     * When loading a cube, get the center cube instead of chunk, and call {@link GeneratingChunkMap#releaseGeneration} for cubes as well
     */
    @Inject(method = "releaseClaim", at = @At("HEAD"), cancellable = true)
    private void cc_onReleaseClaim(CallbackInfo ci) {
        if (cc_cubePos == null) {
            return;
        }
        ci.cancel();
        GenerationChunkHolder generationchunkholder = this.cc_cubeCache.get(this.cc_cubePos.getX(), this.cc_cubePos.getY(), this.cc_cubePos.getZ());
        ((GenerationChunkHolderAccess) generationchunkholder).cc_invokeRemoveTask(((ChunkGenerationTask) (Object) this));
        this.cache.forEach(this.chunkMap::releaseGeneration);
        this.cc_cubeCache.forEach(this.chunkMap::releaseGeneration);
    }

    /**
     * When loading a cube, check cube dependencies as well when determining if generation is required
     */
    @Inject(method = "canLoadWithoutGeneration", at = @At("HEAD"), cancellable = true)
    private void cc_onCanLoadWithoutGeneration(CallbackInfoReturnable<Boolean> cir) {
        if (cc_cubePos == null) {
            return;
        }
        if (this.targetStatus == ChunkStatus.EMPTY) {
            cir.setReturnValue(true);
        } else {
            ChunkStatus currentCubeStatus = this.cc_cubeCache.get(this.cc_cubePos.getX(), this.cc_cubePos.getY(), this.cc_cubePos.getZ()).getPersistedStatus();
            if (currentCubeStatus != null && !currentCubeStatus.isBefore(this.targetStatus)) {
                ChunkDependencies cubeDependencies = CubePyramid.CC_LOADING_PYRAMID_CUBES.getStepTo(this.targetStatus).accumulatedDependencies();
                int cubeRadius = cubeDependencies.getRadius();

                for (int cubeX = this.cc_cubePos.getX() - cubeRadius; cubeX <= this.cc_cubePos.getX() + cubeRadius; cubeX++) {
                    for (int cubeZ = this.cc_cubePos.getZ() - cubeRadius; cubeZ <= this.cc_cubePos.getZ() + cubeRadius; cubeZ++) {
                        for (int cubeY = this.cc_cubePos.getY() - cubeRadius; cubeY <= this.cc_cubePos.getY() + cubeRadius; cubeY++) {
                            int distance = this.cc_cubePos.getChessboardDistance(cubeX, cubeY, cubeZ);
                            ChunkStatus dependencyRequiredStatus = cubeDependencies.get(distance);
                            ChunkStatus dependencyCurrentStatus = this.cc_cubeCache.get(cubeX, cubeY, cubeZ).getPersistedStatus();
                            if (dependencyCurrentStatus == null || dependencyCurrentStatus.isBefore(dependencyRequiredStatus)) {
                                cir.setReturnValue(false);
                                return;
                            }
                        }
                        // Chunk required status is the highest status of all cubes, which occurs when cubeY is equal to this.cc_cubePos.y
                        int chunkDistanceInCubes = this.cc_cubePos.getChessboardDistance(cubeX, this.cc_cubePos.getY(), cubeZ);
                        ChunkStatus dependencyRequiredStatus = cubeDependencies.get(chunkDistanceInCubes);
                        for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                            for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                int chunkX = Coords.cubeToSection(cubeX, dx);
                                int chunkZ = Coords.cubeToSection(cubeZ, dz);
                                ChunkStatus dependencyCurrentStatus = this.cache.get(chunkX, chunkZ).getPersistedStatus();
                                if (dependencyCurrentStatus == null || dependencyCurrentStatus.isBefore(dependencyRequiredStatus)) {
                                    cir.setReturnValue(false);
                                    return;
                                }
                            }
                        }
                    }
                }

                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getCenter", at = @At("HEAD"), cancellable = true)
    private void cc_onGetCenter(CallbackInfoReturnable<GenerationChunkHolder> cir) {
        if (cc_cubePos != null) cir.setReturnValue(this.cc_cubeCache.get(this.cc_cubePos.getX(), this.cc_cubePos.getY(), this.cc_cubePos.getZ()));
    }

    /**
     * Cubic equivalent of {@code scheduleLayer}; schedules both cubes and chunks at given statuses (Chunk status will be higher to preserve load order invariants)
     */
    // TODO this could be two methods I guess? does it matter?
    private void cc_scheduleLayer(@Nullable ChunkStatus chunkStatus, @Nullable ChunkStatus cubeStatus, boolean needsGeneration) {
        try (Zone zone = Profiler.get().zone("scheduleLayer")) {
            zone.addText(() -> String.format("Chunk: %s, Cube: %s", chunkStatus == null ? "null" : chunkStatus.getName(), cubeStatus == null ? "null" : cubeStatus.getName()));
            if (cubeStatus != null) {
                int cubeRadius = this.cc_getCubeRadiusForLayer(cubeStatus, needsGeneration);
                for (int cubeX = this.cc_cubePos.getX() - cubeRadius; cubeX <= this.cc_cubePos.getX() + cubeRadius; cubeX++) {
                    for (int cubeZ = this.cc_cubePos.getZ() - cubeRadius; cubeZ <= this.cc_cubePos.getZ() + cubeRadius; cubeZ++) {
                        for (int cubeY = this.cc_cubePos.getY() - cubeRadius; cubeY <= this.cc_cubePos.getY() + cubeRadius; cubeY++) {
                            GenerationChunkHolder generationchunkholder = this.cc_cubeCache.get(cubeX, cubeY, cubeZ);
                            if (this.markedForCancellation || !this.scheduleChunkInLayer(cubeStatus, needsGeneration, generationchunkholder)) {
                                return;
                            }
                        }
                    }
                }
            }
            if (chunkStatus != null) {
                int cubeRadius = this.cc_getCubeRadiusForLayer(chunkStatus, needsGeneration);
                for (int cubeX = this.cc_cubePos.getX() - cubeRadius; cubeX <= this.cc_cubePos.getX() + cubeRadius; cubeX++) {
                    for (int cubeZ = this.cc_cubePos.getZ() - cubeRadius; cubeZ <= this.cc_cubePos.getZ() + cubeRadius; cubeZ++) {
                        for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                            for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                int chunkX = Coords.cubeToSection(cubeX, dx);
                                int chunkZ = Coords.cubeToSection(cubeZ, dz);
                                GenerationChunkHolder generationchunkholder = this.cache.get(chunkX, chunkZ);
                                if (this.markedForCancellation || !this.scheduleChunkInLayer(chunkStatus, needsGeneration, generationchunkholder)) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "scheduleLayer", at = @At("HEAD"))
    private void cc_onScheduleLayer(ChunkStatus status, boolean needsGeneration, CallbackInfo ci) {
        if (cc_cubePos == null) return;
        throw new IllegalStateException("shouldn't call vanilla scheduleLayer for cube generation task");
    }

    @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), value = @MethodSig("getRadiusForLayer(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Z)I"))
    private native int cc_getCubeRadiusForLayer(ChunkStatus status, boolean needsGeneration);

    @Shadow protected abstract boolean scheduleChunkInLayer(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk);

    @Shadow public static ChunkGenerationTask create(GeneratingChunkMap chunkMap, ChunkStatus targetStatus, ChunkPos pos) {
        return null;
    }

    @Shadow private boolean needsGeneration;

    @Shadow protected abstract boolean canLoadWithoutGeneration();

    @Inject(method = "scheduleChunkInLayer", at = @At("HEAD"), cancellable = true)
    private void cc_onScheduleChunkInLayer(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk, CallbackInfoReturnable<Boolean> cir) {
        if (((GenerationCloHolder) chunk).cc_getCubePos() != null) {
            cir.setReturnValue(cc_scheduleCubeInLayer(status, needsGeneration, chunk));
        }
    }

    @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), value = @MethodSig("scheduleChunkInLayer(Lnet/minecraft/world/level/chunk/status/ChunkStatus;ZLnet/minecraft/server/level/GenerationChunkHolder;)Z"))
    private native boolean cc_scheduleCubeInLayer(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk);
}
