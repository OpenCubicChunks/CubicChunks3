package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

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

// FIXME this class desperately needs unit tests.
@Dasm(GlobalSet.class)
@Mixin(ChunkGenerationTask.class)
public abstract class MixinChunkGenerationTask implements CloGenerationTask {
    @Shadow @Final private ChunkPos pos;
    @Shadow @Final private GeneratingChunkMap chunkMap;
    @Shadow @Final public ChunkStatus targetStatus;
    @Shadow private volatile boolean markedForCancellation;
    @Shadow @Final private StaticCache2D<GenerationChunkHolder> cache;
    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), field = @FieldSig(type = @Ref(ChunkPos.class), name = "pos"))
    private CubePos cc_cubePos;
    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), field = @FieldSig(type = @Ref(StaticCache2D.class), name = "cache"))
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

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), method = @MethodSig("create(Lnet/minecraft/server/level/GeneratingChunkMap;Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    @Public private static ChunkGenerationTask cc_createCubeGenerationTask(GeneratingChunkMap chunkMap, ChunkStatus targetStatus, CubePos pos) {
        int cubeRadius = CubePyramid.CC_GENERATION_PYRAMID_CUBES.getStepTo(targetStatus).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
        int cubeDiameter = cubeRadius * 2 + 1;
        int chunkDiameter = cubeDiameter * CubicConstants.DIAMETER_IN_SECTIONS;
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

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkGenerationTask.class), method = @MethodSig("create(Lnet/minecraft/server/level/GeneratingChunkMap;Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    @Public private static ChunkGenerationTask cc_createCubeGenerationTask(GeneratingChunkMap chunkMap, ChunkStatus targetStatus, CloPos pos) {
        if (pos.isCube()) {
            return cc_createCubeGenerationTask(chunkMap, targetStatus, pos.cubePos());
        } else {
            return create(chunkMap, targetStatus, pos.chunkPos());
        }
    }

    @Inject(method = "releaseClaim", at = @At("HEAD"), cancellable = true)
    private void cc_onReleaseClaim(CallbackInfo ci) {
        if (cc_cubePos == null) {
            return;
        }
        ci.cancel();
        GenerationChunkHolder generationchunkholder = this.cc_cubeCache.get(this.cc_cubePos.getX(), this.cc_cubePos.getY(), this.cc_cubePos.getZ());
        ((GenerationChunkHolderAccess) generationchunkholder).cc_invokeRemoveTask(((ChunkGenerationTask) (Object) this));
        this.cache.forEach(this.chunkMap::releaseGeneration);
    }

    @Inject(method = "canLoadWithoutGeneration", at = @At("HEAD"), cancellable = true)
    private void cc_onCanLoadWithoutGeneration(CallbackInfoReturnable<Boolean> cir) {
        if (cc_cubePos == null) {
            return;
        }
        if (this.targetStatus == ChunkStatus.EMPTY) {
            cir.setReturnValue(true);
        } else {
            ChunkStatus currentStatus = this.cc_cubeCache.get(this.cc_cubePos.getX(), this.cc_cubePos.getY(), this.cc_cubePos.getZ()).getPersistedStatus();
            if (currentStatus != null && !currentStatus.isBefore(this.targetStatus)) {
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

    // FIXME load order - chunks probably just need to be a status ahead of cubes
    @Inject(method = "scheduleLayer", at = @At("HEAD"), cancellable = true)
    private void cc_onScheduleLayer(ChunkStatus status, boolean needsGeneration, CallbackInfo ci) {
        if (cc_cubePos == null) return;
        ci.cancel();
        try (Zone zone = Profiler.get().zone("scheduleLayer")) {
            zone.addText(status::getName);
            int cubeRadius = this.cc_getCubeRadiusForLayer(status, needsGeneration);

            for (int cubeX = this.cc_cubePos.getX() - cubeRadius; cubeX <= this.cc_cubePos.getX() + cubeRadius; cubeX++) {
                for (int cubeZ = this.cc_cubePos.getZ() - cubeRadius; cubeZ <= this.cc_cubePos.getZ() + cubeRadius; cubeZ++) {
                    for (int cubeY = this.cc_cubePos.getY() - cubeRadius; cubeY <= this.cc_cubePos.getY() + cubeRadius; cubeY++) {
                        GenerationChunkHolder generationchunkholder = this.cc_cubeCache.get(cubeX, cubeY, cubeZ);
                        if (this.markedForCancellation || !this.scheduleChunkInLayer(status, needsGeneration, generationchunkholder)) {
                            return;
                        }
                    }
                    for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                        for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                            int chunkX = Coords.cubeToSection(cubeX, dx);
                            int chunkZ = Coords.cubeToSection(cubeZ, dz);
                            GenerationChunkHolder generationchunkholder = this.cache.get(chunkX, chunkZ);
                            if (this.markedForCancellation || !this.scheduleChunkInLayer(status, needsGeneration, generationchunkholder)) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), value = @MethodSig("getRadiusForLayer(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Z)I"))
    private native int cc_getCubeRadiusForLayer(ChunkStatus status, boolean needsGeneration);

    @Shadow protected abstract boolean scheduleChunkInLayer(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk);

    @Shadow public static ChunkGenerationTask create(GeneratingChunkMap chunkMap, ChunkStatus targetStatus, ChunkPos pos) {
        return null;
    }

    @Inject(method = "scheduleChunkInLayer", at = @At("HEAD"), cancellable = true)
    private void cc_onScheduleChunkInLayer(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk, CallbackInfoReturnable<Boolean> cir) {
        var cloPos = ((GenerationCloHolder) chunk).cc_getCloPos();
        if (cloPos != null && cloPos.isCube()) cir.setReturnValue(cc_scheduleCubeInLayer(status, needsGeneration, chunk));
    }

    @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, owner = @Ref(ChunkGenerationTask.class), value = @MethodSig("scheduleChunkInLayer(Lnet/minecraft/world/level/chunk/status/ChunkStatus;ZLnet/minecraft/server/level/GenerationChunkHolder;)Z"))
    private native boolean cc_scheduleCubeInLayer(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk);
}
