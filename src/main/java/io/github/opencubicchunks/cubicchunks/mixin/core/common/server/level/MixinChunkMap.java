package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.storage.MixinChunkStorage;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.SectionPosToCubeSet;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundSetCubeCacheCenterPacket;
import io.github.opencubicchunks.cubicchunks.server.level.CloGenerationTask;
import io.github.opencubicchunks.cubicchunks.server.level.CloHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkMap;
import io.github.opencubicchunks.cubicchunks.server.level.GeneratingCubeMap;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStep;
import io.github.opencubicchunks.cubicchunks.world.level.entity.CloStatusUpdateListener;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The vanilla {@link ChunkMap} class stores all loaded chunks for a world and handles loading and unloading them, including dependencies on neighboring chunks.
 * This mixin adds cubic chunks equivalents for methods where necessary, to allow ChunkMap to work with CLOs (i.e. both chunks and cubes).
 */
@Dasm(ChunkToCloSet.class)
@Mixin(ChunkMap.class)
public abstract class MixinChunkMap extends MixinChunkStorage implements GeneratingCubeMap, CubicChunkMap {
    @Shadow public abstract ReportedException debugFuturesAndCreateReportedException(IllegalStateException exception, String details);

    @Shadow protected abstract ChunkHolder getUpdatingChunkIfPresent(long aLong);

    @Shadow @Final ServerLevel level;
    @Shadow @Final private ChunkMap.DistanceManager distanceManager;

    private static CompletableFuture<ChunkResult<CloAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(
        ChunkResult.error("Unloaded chunk")
    );

    @Shadow @Final private static CompletableFuture<ChunkResult<List<CloAccess>>> UNLOADED_CHUNK_LIST_FUTURE;
    @Shadow @Final private static ChunkResult<List<CloAccess>> UNLOADED_CHUNK_LIST_RESULT;

    @Shadow private static double euclideanDistanceSquared(ChunkPos chunkPos, Vec3 pos) {
        throw new IllegalStateException();
    }

    @AddFieldToSets(sets = GlobalSet.class, owner = @Ref(ChunkMap.class), field = @FieldSig(type = @Ref(ChunkProgressListener.class), name = "progressListener"))
    private CloProgressListener cc_progressListener;
    @AddFieldToSets(sets = GlobalSet.class, owner = @Ref(ChunkMap.class), field = @FieldSig(type = @Ref(ChunkStatusUpdateListener.class), name = "chunkStatusListener"))
    private CloStatusUpdateListener cc_cloStatusListener;

    // TODO once we can target non-return locations in constructors, do this when the vanilla field is set
    @Inject(method = "<init>", at = @At("RETURN"))
    private void cc_onInit(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager, Executor dispatcher,
                           BlockableEventLoop mainThreadExecutor, LightChunkGetter lightChunk, ChunkGenerator generator, ChunkProgressListener progressListener,
                           ChunkStatusUpdateListener chunkStatusListener, Supplier overworldDataStorage, TicketStorage ticketStorage, int serverViewDistance, boolean sync, CallbackInfo ci) {
        if (((CanBeCubic) level).cc_isCubic()) {
            cc_progressListener = ((CloProgressListener) progressListener);
            // TODO P2 (entities): actually pass in a cloStatusListener - since ChunkStatusUpdateListener is passed as a parameter, not sure what the best approach is without making our own constructor
            cc_cloStatusListener = (cloPos, fullChunkStatus) -> {};
            ((MarkableAsCubic) distanceManager).cc_setCubic();
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("setChunkUnsaved(Lnet/minecraft/world/level/ChunkPos;)V"))
    private native void cc_setCloUnsaved(CloPos cloPos);

    /**
     * Returns the squared distance to the center of the cube.
     */
    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("euclideanDistanceSquared(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/phys/Vec3;)D"))
    private static double cc_euclideanDistanceSquared(CloPos cloPos, Vec3 vec3) {
        if (cloPos.isChunk()) {
            // FIXME we shouldn't be getting euclidean distance for chunks, as this doesn't make sense in context
            return euclideanDistanceSquared(cloPos.chunkPos(), vec3);
//            throw new UnsupportedOperationException("Should not call euclideanDistanceSquared with a chunk position");
        }
        double cubeCenterX = Coords.cubeToCenterBlock(cloPos.getX());
        double cubeCenterY = Coords.cubeToCenterBlock(cloPos.getX());
        double cubeCenterZ = Coords.cubeToCenterBlock(cloPos.getX());
        double dx = cubeCenterX - vec3.x();
        double dy = cubeCenterY - vec3.y();
        double dz = cubeCenterZ - vec3.z();
        return dx * dx + dy * dy + dz * dz;
    }

    // TODO make vanilla isChunkTracked/isChunkOnTrackedBorder fail in cubic world

    // These methods are not copied due to taking 3 ints instead of 2
    @Override
    public boolean cc_isChunkTracked(ServerPlayer player, int x, int y, int z) {
        return ((CloTrackingView) player.getChunkTrackingView()).cc_contains(x, y, z)
            // TODO this requires PlayerChunkSender to accept Clo longs
            && !player.connection.chunkSender.isPending(CloPos.cubeAsLong(x, y, z));
    }

    private boolean cc_isChunkOnTrackedBorder(ServerPlayer player, int x, int y, int z) {
        if (this.cc_isChunkTracked(player, x, y, z)) {
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    for (int dy = -1; dy <= 1; ++dy) {
                        if ((dx != 0 || dz != 0 || dy != 0) && !this.cc_isChunkTracked(player, x + dx, y + dy, z + dz)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // TODO getChunkDebugData - low prio

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getChunkRangeFuture(Lnet/minecraft/server/level/ChunkHolder;ILjava/util/function/IntFunction;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<ChunkResult<List<CloAccess>>> cc_getChunkRangeFuture(ChunkHolder cloHolder, int radius,
                                                                                          IntFunction<ChunkStatus> statusByRadius);

    // TODO this could be substantially improved probably hopefully
    /**
     * Cubes require different adjacency logic compared to Chunks
     */
    @Dynamic @Inject(method = "cc_dasm$cc_getChunkRangeFuture", at = @At("HEAD"), cancellable = true)
    private void cc_onGetChunkRangeFuture(ChunkHolder cloHolder, int radius, IntFunction<ChunkStatus> statusByRadius,
                                          CallbackInfoReturnable<CompletableFuture<ChunkResult<List<CloAccess>>>> cir) {
        // Note that statusByRadius sometimes isn't actually correct for cubes beyond the first few steps, but getChunkRangeFuture is only called with parameters for which it's correct within the radius
        CloPos pos = ((CloHolder) cloHolder).cc_getCloPos();
        if (!pos.isCube()) return;
        // The vanilla method has an early exit for radius=0 here; this is not valid for cubes because even if radius=0 we still depend on chunks that neighbor the cube
        int cubeDiameter = radius * 2 + 1;
        int chunkDiameter = cubeDiameter * CubicConstants.DIAMETER_IN_SECTIONS;
        int futureCount = cubeDiameter * cubeDiameter * cubeDiameter + chunkDiameter * chunkDiameter;
        List<CompletableFuture<ChunkResult<CloAccess>>> futures = new ArrayList<>(futureCount);
        int middleCubeIndex = -1;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                // We want the chunks intersecting this column of cubes to be loaded at the maximum level of any of those cubes;
                // this occurs when dy=0, so we only consider x/z distance
                int chunkDistance = Math.max(Math.abs(dz), Math.abs(dx));
                for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
                    for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                        ChunkHolder holder = this.getUpdatingChunkIfPresent(CloPos.chunkAsLong(Coords.cubeToSection(pos.getX()+dx, sectionX), Coords.cubeToSection(pos.getZ()+dz, sectionZ)));
                        if (holder == null) {
                            cir.setReturnValue(UNLOADED_CHUNK_LIST_FUTURE);
                            return;
                        }
                        ChunkStatus expectedStatus = statusByRadius.apply(chunkDistance);
                        futures.add((CompletableFuture<ChunkResult<CloAccess>>) (Object) holder.scheduleChunkGenerationTask(expectedStatus, (ChunkMap) (Object) this));
                    }
                }
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        middleCubeIndex = futures.size();
                    }
                    ChunkHolder holder = this.getUpdatingChunkIfPresent(CloPos.cubeAsLong(pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz));
                    if (holder == null) {
                        cir.setReturnValue(UNLOADED_CHUNK_LIST_FUTURE);
                        return;
                    }
                    ChunkStatus expectedStatus = statusByRadius.apply(Math.max(chunkDistance, Math.abs(dy)));
                    futures.add((CompletableFuture<ChunkResult<CloAccess>>) (Object) holder.scheduleChunkGenerationTask(expectedStatus, (ChunkMap) (Object) this));
                }
            }
        }

        // Vanilla expects that the center chunk is in the middle of the list; this is not the case for cubes, so we manually swap the center cube to the middle
        // - this is a """temporary""" approach, that we may or may not actually fix later.
        Collections.swap(futures, middleCubeIndex, futures.size() / 2);

        cir.setReturnValue(Util.sequence(futures).thenApply(resultList -> {
            List<CloAccess> outputList = new ArrayList<>(resultList.size());

            for(final ChunkResult<CloAccess> chunkResult : resultList) {
                if (chunkResult == null) {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }

                CloAccess cloAccess = chunkResult.orElse(null);
                if (cloAccess == null) {
                    return UNLOADED_CHUNK_LIST_RESULT;
                }

                outputList.add(cloAccess);
            }

            return ChunkResult.of(outputList);
        }));
    }

    // dasm + mixin
    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, value = @MethodSig("updateChunkScheduling(JILnet/minecraft/server/level/ChunkHolder;I)Lnet/minecraft/server/level/ChunkHolder;"))
    @Nullable public native ChunkHolder cc_updateCubeScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);

    @Inject(method = "updateChunkScheduling", at = @At("HEAD"), cancellable = true)
    private void cc_onUpdateChunkScheduling(long chunkPos, int newLevel, ChunkHolder holder, int oldLevel, CallbackInfoReturnable<ChunkHolder> cir) {
        if (((CanBeCubic) level).cc_isCubic() && CloPos.isCube(chunkPos)) {
            cir.setReturnValue(cc_updateCubeScheduling(chunkPos, newLevel, holder, oldLevel));
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
    private native void cc_onLevelChange(CloPos cloPos, IntSupplier intsupplier, int i, IntConsumer intconsumer);

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
    public void cc_onCubeLevelChange(CubePos cubePos, IntSupplier queueLevelGetter, int ticketLevel, IntConsumer queueLevelSetter) {
        cc_onLevelChange(CloPos.cube(cubePos), queueLevelGetter, ticketLevel, queueLevelSetter);
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("saveAllChunks(Z)V"))
    public native void cc_saveAllChunks(boolean flush);

    @Inject(method = "saveAllChunks", at = @At("HEAD"), cancellable = true)
    private void cc_onSaveAllChunks(boolean flush, CallbackInfo ci) {
        if (((CanBeCubic) level).cc_isCubic()) {
            cc_saveAllChunks(flush);
            ci.cancel();
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("saveChunksEagerly(Ljava/util/function/BooleanSupplier;)V"))
    private native void cc_saveClosEagerly(BooleanSupplier hasMoreTime);

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("scheduleUnload(JLnet/minecraft/server/level/ChunkHolder;)V"))
    private void cc_scheduleUnload(long chunkPos, ChunkHolder chunkHolder) {
        // TODO (P2) save/load
    }

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("scheduleChunkLoad(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<CloAccess> cc_scheduleChunkLoad(CloPos cloPos);

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("scheduleChunkLoad(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<CloAccess> cc_scheduleChunkLoad(CubePos cubePos) {
        return cc_scheduleChunkLoad(CloPos.cube(cubePos));
    }

    @Dynamic @Redirect(method = "cc_dasm$cc_scheduleChunkLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;prefetch(Lio/github/opencubicchunks/cc_core/world/level/CloPos;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<?> cc_onScheduleChunkLoad_poiManagerPreFetch(PoiManager instance, CloPos cloPos) {
        // TODO (P2) save/load - PoiManager
        return CompletableFuture.completedFuture(null);
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("handleChunkLoadFailure(Ljava/lang/Throwable;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    private native ChunkResult<CloAccess> cc_handleChunkLoadFailure(Throwable exception, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(
            value = @MethodSig("createEmptyChunk(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    private native CloAccess cc_createEmptyChunk(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("markPositionReplaceable(Lnet/minecraft/world/level/ChunkPos;)V"))
    private native void cc_markPositionReplaceable(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("markPosition(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/status/ChunkType;)B"))
    private native byte cc_markPosition(CloPos cloPos, ChunkType chunkType);

    // dasm + mixin
    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(useRedirectSets = ChunkToCubeSet.class, value = @MethodSig("applyStep(Lnet/minecraft/server/level/GenerationChunkHolder;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<CubeAccess> cc_applyCubeStep(
        GenerationChunkHolder generationchunkholder, CubeStep chunkstep, StaticCache3D<GenerationChunkHolder> cache
    );

    @Dynamic @Redirect(method = "cc_dasm$cc_applyCubeStep", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks/util/StaticCache3D;get(II)Ljava/lang/Object;"))
    private Object cc_onApplyCubeStep_staticCacheGet(StaticCache3D instance, int x, int z, @Local(ordinal = 0) CubePos cubePos) {
        return instance.get(cubePos.getX(), cubePos.getY(), cubePos.getZ());
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("scheduleGenerationTask(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    public native ChunkGenerationTask cc_scheduleGenerationTask(ChunkStatus chunkstatus, CloPos cloPos);

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("scheduleGenerationTask(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    public ChunkGenerationTask cc_scheduleGenerationTask(ChunkStatus chunkstatus, CubePos cubePos) {
        return cc_scheduleGenerationTask(chunkstatus, CloPos.cube(cubePos));
    }

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("runGenerationTask(Lnet/minecraft/server/level/ChunkGenerationTask;)V"))
    private native void cc_runGenerationTask(ChunkGenerationTask chunkgenerationtask);

    // Delegate to the cube method for cubes
    @Inject(method = "runGenerationTask", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaRunGenerationTask(ChunkGenerationTask task, CallbackInfo ci) {
        if (((CloGenerationTask) task).cc_getCloPos().isCube()) {
            ci.cancel();
            cc_runGenerationTask(task);
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("prepareEntityTickingChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<LevelClo>> cc_prepareEntityTickingChunk(ChunkHolder holder);

    @Inject(method = "prepareEntityTickingChunk", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaPrepareEntityTickingChunk(ChunkHolder chunk, CallbackInfoReturnable<CompletableFuture<ChunkResult<LevelClo>>> cir) {
        if (((CloHolder) chunk).cc_getCloPos().isCube()) {
            cir.setReturnValue(cc_prepareEntityTickingChunk(chunk));
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("prepareTickingChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<LevelClo>> cc_prepareTickingChunk(ChunkHolder holder);

    @Inject(method = "prepareTickingChunk", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaPrepareTickingChunk(ChunkHolder chunk, CallbackInfoReturnable<CompletableFuture<ChunkResult<LevelClo>>> cir) {
        if (((CloHolder) chunk).cc_getCloPos().isCube()) {
            cir.setReturnValue(cc_prepareTickingChunk(chunk));
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onChunkReadyToSend(Lnet/minecraft/server/level/ChunkHolder;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private native void cc_onChunkReadyToSend(ChunkHolder chunkholder, LevelClo cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("prepareAccessibleChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<LevelClo>> cc_prepareAccessibleChunk(ChunkHolder holder);

    @Inject(method = "prepareAccessibleChunk", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaPrepareAccessibleChunk(ChunkHolder chunk, CallbackInfoReturnable<CompletableFuture<ChunkResult<LevelClo>>> cir) {
        if (((CloHolder) chunk).cc_getCloPos().isCube()) {
            cir.setReturnValue(cc_prepareAccessibleChunk(chunk));
        }
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("saveChunkIfNeeded(Lnet/minecraft/server/level/ChunkHolder;J)Z"))
    private native boolean cc_saveChunkIfNeeded(ChunkHolder holder, long gameTime);

    // TODO (P2): for now we just don't save (requires more things to be CC-ified to not crash)
    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"))
    private boolean cc_save(CloAccess cloAccess) {
        return false;
    }

//    // dasm + mixin
//    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"))
//    private native boolean cc_save(CloAccess cloAccess);
//
//    /**
//     * Redirect error logging to log with CloPos
//     */
//    @Dynamic @Inject(method = "cc_dasm$cc_save", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/world/level/CloPos;getX()I"), cancellable = true)
//    private void cc_onSave_errorLog(CloAccess cloAccess, CallbackInfoReturnable<Boolean> cir, @Local Exception exception) {
//        LOGGER.error("Failed to save chunk or cube {}", cloAccess.cc_getCloPos().toString(), exception);
//        cir.setReturnValue(false);
//    }

    // This calls ChunkSerializer.getChunkTypeFromTag, which could be an issue?
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("isExistingChunkFull(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private native boolean cc_isExistingChunkFull(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V"))
    private native void cc_markChunkPendingToSend(ServerPlayer player, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private static native void cc_markChunkPendingToSend(ServerPlayer player, LevelClo clo);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("dropChunk(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V"))
    private static native void cc_dropChunk(ServerPlayer player, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getChunkToSend(J)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    public native LevelClo cc_getChunkToSend(long cloPos);

    // dumpChunks (low prio)

    // printFuture - only ever called in dumpChunks

    // TODO (P2) readChunk: this.upgradeChunkTag might need a dasm redirect?

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("readChunk(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Optional<CompoundTag>> cc_readChunk(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("collectSpawningChunks(Ljava/util/List;)V"))
    native void cc_collectSpawningClos(List<LevelClo> list);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("forEachBlockTickingChunk(Ljava/util/function/Consumer;)V"))
    native void cc_forEachBlockTickingClo(Consumer<LevelClo> consumer);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("anyPlayerCloseEnoughForSpawning(Lnet/minecraft/world/level/ChunkPos;)Z"))
    public native boolean cc_anyPlayerCloseEnoughForSpawning(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("anyPlayerCloseEnoughForSpawningInternal(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private native boolean cc_anyPlayerCloseEnoughForSpawningInternal(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getPlayersCloseForSpawning(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/List;"))
    public native List<ServerPlayer> cc_getPlayersCloseForSpawning(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("playerIsCloseEnoughForSpawning(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private native boolean cc_playerIsCloseEnoughForSpawning(ServerPlayer player, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updatePlayerStatus(Lnet/minecraft/server/level/ServerPlayer;Z)V"))
    public native void cc_updatePlayerStatus(ServerPlayer player, boolean track);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("move(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public native void cc_move(ServerPlayer player);

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updateChunkTracking(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private native void cc_updateChunkTracking(ServerPlayer player);

    @Dynamic @WrapOperation(method = "cc_dasm$cc_updateChunkTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;getPlayerViewDistance(Lnet/minecraft/server/level/ServerPlayer;)I"))
    private int cc_onUpdateChunkTracking_getViewDistance(ChunkMap instance, ServerPlayer player, Operation<Integer> original) {
        return Coords.sectionToCubeRenderDistance(original.call(instance, player));
    }

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("applyChunkTrackingView(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ChunkTrackingView;)V"))
    private native void cc_applyChunkTrackingView(ServerPlayer player, CloTrackingView chunkTrackingView);

    @Dynamic @Redirect(method = "cc_dasm$cc_applyChunkTrackingView", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void cc_onApplyChunkTrackingView_setChunkCacheCenterPacket(ServerGamePacketListenerImpl instance, Packet packet, ServerPlayer player, CloTrackingView cloTrackingView) {
        PacketDistributor.sendToPlayer(player, new CCClientboundSetCubeCacheCenterPacket(((CloTrackingView.Positioned) cloTrackingView).center().cubePos()));
    }

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getPlayers(Lnet/minecraft/world/level/ChunkPos;Z)Ljava/util/List;"))
    public native List<ServerPlayer> cc_getPlayers(CloPos pos, boolean boundaryOnly);

    @Dynamic @Redirect(method = "cc_dasm$cc_getPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;isChunkOnTrackedBorder(Lnet/minecraft/server/level/ServerPlayer;II)Z"))
    private boolean cc_getPlayers_isChunkOnTrackedBorder(ChunkMap instance, ServerPlayer player, int x, int z, @Local CloPos pos) {
        return this.cc_isChunkOnTrackedBorder(player, pos.getX(), pos.getY(), pos.getZ());
    }

    @Dynamic @Redirect(method = "cc_dasm$cc_getPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;isChunkTracked(Lnet/minecraft/server/level/ServerPlayer;II)Z"))
    private boolean cc_getPlayers_isChunkTracked(ChunkMap instance, ServerPlayer player, int x, int z, @Local CloPos pos) {
        return this.cc_isChunkTracked(player, pos.getX(), pos.getY(), pos.getZ());
    }

    // Replace `SectionPos.chunk()` with `SectionPos.cc_cube()` unconditionally here
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(value = @MethodSig("tick(Ljava/util/function/BooleanSupplier;)V"), useRedirectSets = { ChunkToCloSet.class, SectionPosToCubeSet.class })
    protected native void cc_tick(BooleanSupplier hasMoreTime);

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(value = @MethodSig("tick()V"), useRedirectSets = { ChunkToCloSet.class, SectionPosToCubeSet.class })
    public native void cc_tick();

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(value = @MethodSig("processUnloads(Ljava/util/function/BooleanSupplier;)V"))
    private native void cc_processUnloads(BooleanSupplier hasMoreTime);

    // TODO resendBiomesForChunks - only used for FillBiomeCommand

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onFullChunkStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    public native void cc_onFullChunkStatusChange(CloPos cloPos, FullChunkStatus fullChunkStatus);

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("onFullChunkStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    public void cc_onFullChunkStatusChange(CubePos cubePos, FullChunkStatus fullChunkStatus) {
        cc_onFullChunkStatusChange(CloPos.cube(cubePos), fullChunkStatus);
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("waitForLightBeforeSending(Lnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_waitForLightBeforeSending(CloPos cloPos, int p_301130_);

    // TODO these three are temporary - needs dasm subclass method redirect inheritance for non-overriden methods
    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class),
        method = @MethodSig("isOldChunkAround(Lnet/minecraft/world/level/ChunkPos;I)Z"))
    public boolean cc_isOldChunkAround(CloPos pos, int radius) {
        return super.cc_isOldChunkAround(pos, radius);
    }

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class),
        method = @MethodSig("read(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Optional<CompoundTag>> cc_read(CloPos cloPos) {
        return super.cc_read(cloPos);
    }

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class),
        method = @MethodSig("write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"))
    public void cc_write(CloPos cloPos, CompoundTag chunkData) {
        super.cc_write(cloPos, chunkData);
    }
}
