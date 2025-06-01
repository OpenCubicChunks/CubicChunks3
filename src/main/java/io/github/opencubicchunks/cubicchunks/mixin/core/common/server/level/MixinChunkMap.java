package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.storage.MixinChunkStorage;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.SectionPosToCubeSet;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundSetCubeCacheCenterPacket;
import io.github.opencubicchunks.cubicchunks.server.level.CloCollectorFuture;
import io.github.opencubicchunks.cubicchunks.server.level.CloHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkMap;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.entity.CloStatusUpdateListener;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
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
public abstract class MixinChunkMap extends MixinChunkStorage implements CubicChunkMap {
    // TODO maybe don't shadow logger; use our own?
    @Shadow @Final private static Logger LOGGER;

    private static final List<ChunkStatus> cc_CHUNK_STATUSES = ChunkStatus.getStatusList();

    @Shadow public abstract ReportedException debugFuturesAndCreateReportedException(IllegalStateException exception, String details);

    @Shadow protected abstract ChunkHolder getUpdatingChunkIfPresent(long aLong);

    @Shadow private static boolean isChunkDataValid(CompoundTag tag) {
        throw new IllegalStateException("mixin failed to apply");
    }

    @Shadow @Final private BlockableEventLoop<Runnable> mainThreadExecutor;
    @Shadow @Final ServerLevel level;
    @Shadow @Final private ChunkMap.DistanceManager distanceManager;
    @AddFieldToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class), field = @FieldSig(type = @Ref(ChunkProgressListener.class), name = "progressListener"))
    private CloProgressListener cc_progressListener;
    @AddFieldToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class), field = @FieldSig(type = @Ref(ChunkStatusUpdateListener.class), name = "chunkStatusListener"))
    private CloStatusUpdateListener cc_cloStatusListener;

    // TODO once we can target non-return locations in constructors, do this when the vanilla field is set
    @Inject(method = "<init>", at = @At("RETURN"))
    private void cc_onInit(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager,
                           Executor dispatcher, BlockableEventLoop mainThreadExecutor, LightChunkGetter lightChunk, ChunkGenerator generator, ChunkProgressListener progressListener,
                           ChunkStatusUpdateListener chunkStatusListener, Supplier overworldDataStorage, int viewDistance, boolean sync, CallbackInfo ci) {
        if (((CanBeCubic) level).cc_isCubic()) {
            cc_progressListener = ((CloProgressListener) progressListener);
            // TODO P2 (entities): actually pass in a cloStatusListener - since ChunkStatusUpdateListener is passed as a parameter, not sure what the best approach is without making our own constructor
            cc_cloStatusListener = (cloPos, fullChunkStatus) -> {};
            ((MarkableAsCubic) distanceManager).cc_setCubic();
        }
    }

    /**
     * Returns the squared distance to the center of the cube.
     */
    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("euclideanDistanceSquared(Lnet/minecraft/world/level/ChunkPos;"
        + "Lnet/minecraft/world/entity/Entity;)D"))
    private static double cc_euclideanDistanceSquared(CloPos cloPos, Entity entity) {
        if (cloPos.isChunk()) {
            throw new UnsupportedOperationException("Should not call euclideanDistanceSquared with a chunk position");
        }
        double cubeCenterX = Coords.cubeToCenterBlock(cloPos.getX());
        double cubeCenterY = Coords.cubeToCenterBlock(cloPos.getX());
        double cubeCenterZ = Coords.cubeToCenterBlock(cloPos.getX());
        double dx = cubeCenterX - entity.getX();
        double dy = cubeCenterY - entity.getY();
        double dz = cubeCenterZ - entity.getZ();
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
    private static ChunkStatus cc_getChildStatus(ChunkStatus status) {
        int index = status.getIndex() + 1;
        return index >= cc_CHUNK_STATUSES.size() ? ChunkStatus.FULL : cc_CHUNK_STATUSES.get(index);
    }

    // TODO this could be substantially improved probably hopefully
    /**
     * Cubes require different adjacency logic compared to Chunks
     */
    @Dynamic @Inject(method = "cc_dasm$cc_getChunkRangeFuture", at = @At("HEAD"), cancellable = true)
    private void cc_onGetChunkRangeFuture(ChunkHolder cloHolder, int radius, IntFunction<ChunkStatus> statusByRadius,
                                          CallbackInfoReturnable<CompletableFuture<ChunkResult<List<CloAccess>>>> cir) {
        CloPos pos = ((CloHolder) cloHolder).cc_getCloPos();
        if (!pos.isCube()) return;
        // The vanilla method has an early exit for radius=0 here; this is not valid for cubes because even if radius=0 we still depend on chunks that neighbor the cube
        List<ChunkHolder> cloHolders = new ArrayList<>();
        List<ChunkStatus> expectedStatuses = new ArrayList<>();
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
                            var pos1 = new ChunkPos(Coords.cubeToSection(pos.getX()+dx, sectionX), Coords.cubeToSection(pos.getZ()+dz, sectionZ));
                            cir.setReturnValue(CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                                @Override
                                public String toString() {
                                    return "Unloaded " + pos1;
                                }
                            })));
                            return;
                        }
                        ChunkStatus expectedStatus = statusByRadius.apply(chunkDistance);
                        // getChunkRangeFuture statusByRadius returns the status that is depended on, not the actual destination status. for non-central chunks that's fine,
                        // but for the chunks intersecting the center cube, the central cube reaching the destination status depends on the intersecting chunks reaching the destination status, not its parent.
                        if (chunkDistance == 0) expectedStatus = cc_getChildStatus(expectedStatus);
                        cloHolders.add(holder);
                        expectedStatuses.add(expectedStatus);
                    }
                }
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        middleCubeIndex = cloHolders.size();
                    }
                    ChunkHolder holder = this.getUpdatingChunkIfPresent(CloPos.cubeAsLong(pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz));
                    if (holder == null) {
                        var pos1 = CloPos.cube(pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz);
                        cir.setReturnValue(CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                            @Override
                            public String toString() {
                                return "Unloaded " + pos1;
                            }
                        })));
                        return;
                    }
                    ChunkStatus expectedStatus = statusByRadius.apply(Math.max(chunkDistance, Math.abs(dy)));
                    cloHolders.add(holder);
                    expectedStatuses.add(expectedStatus);
                }
            }
        }

        // Vanilla gets futures for each individual ChunkHolder and uses Util.sequence to combine them;
        // we instead use CloCollectorFuture, and add a listener to each CloHolder that notifies the collector when that CloHolder has reached the desired stage.
        // This saves several gigabytes of CompletableFuture objects.
        var cloCollectorFuture = new CloCollectorFuture(cloHolders.size());
        // Lambda created outside the loop to avoid allocating it multiple times
        BiConsumer<ChunkResult<CloAccess>, Throwable> cloCollectorCallback = (either, error) -> cloCollectorFuture.add(either, error, false);
        for (int i = 0; i < cloHolders.size(); i++) {
            var holder = cloHolders.get(i);
            var expectedStatus = expectedStatuses.get(i);
            if (i == middleCubeIndex) {
                ((CloHolder) holder).cc_addCloStatusListener(expectedStatus, (either, error) -> cloCollectorFuture.add(either, error, true), (ChunkMap) (Object) this);
            } else {
                ((CloHolder) holder).cc_addCloStatusListener(expectedStatus, cloCollectorCallback, (ChunkMap) (Object) this);
            }
        }

        CompletableFuture<ChunkResult<List<CloAccess>>> combinedFuture = cloCollectorFuture.thenApply(p_183730_ -> {
                List<CloAccess> list2 = Lists.newArrayList();
                int k1 = 0;

                for(final ChunkResult<CloAccess> either : p_183730_) {
                    if (either == null) {
                        throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                    }

                    Optional<CloAccess> optional = either.left();
                    if (optional.isEmpty()) {
                        int index = k1;
                        return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                            @Override
                            public String toString() {
                                // TODO we should actually show the position here, not just the index - see vanilla method
                                return "Unloaded " + index + " " + either.right().get();
                            }
                        });
                    }

                    list2.add(optional.get());
                    ++k1;
                }

                return Either.left(list2);
            }
        );

        // TODO verify whether this addSaveDependency logic is correct for cubes, especially for radius=0
        for (ChunkHolder holder : cloHolders) {
            ((CloHolder) holder).cc_addSaveDependency("getChunkRangeFuture " + pos + " " + radius, combinedFuture);
        }

        cir.setReturnValue(combinedFuture);
    }

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updateChunkScheduling(JILnet/minecraft/server/level/ChunkHolder;I)Lnet/minecraft/server/level/ChunkHolder;"))
    @Nullable public native ChunkHolder cc_updateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);

    // TODO this is a bit jank; maybe things that call this method should be altered instead?
    @Inject(method = "updateChunkScheduling", at = @At("HEAD"), cancellable = true)
    private void cc_onUpdateChunkScheduling(long chunkPos, int newLevel, ChunkHolder holder, int oldLevel, CallbackInfoReturnable<ChunkHolder> cir) {
        if (((CanBeCubic) level).cc_isCubic()) {
            cir.setReturnValue(cc_updateChunkScheduling(chunkPos, newLevel, holder, oldLevel));
        }
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

    // P4: scheduleUnload lambda we'll want to mirror the forge API for cubes
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("scheduleUnload(JLnet/minecraft/server/level/ChunkHolder;)V"))
    private native void cc_scheduleUnload(long chunkPos, ChunkHolder chunkHolder);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("schedule(Lnet/minecraft/server/level/ChunkHolder;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<CloAccess>> cc_schedule(ChunkHolder holder, ChunkStatus status);

    // dasm + mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("scheduleChunkLoad(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<ChunkResult<CloAccess>> cc_scheduleChunkLoad(CloPos cloPos);

    /**
     * Loading cubes at EMPTY requires additional logic to ensure that the corresponding chunks are loaded first
     * (Unlike other statuses, this dependency is not handled in {@link ChunkMap#getChunkRangeFuture})
     */
    @Dynamic @Inject(method = "cc_dasm$cc_scheduleChunkLoad", at = @At("HEAD"), cancellable = true)
    private void cc_onScheduleChunkLoad(CloPos pos,
                                          CallbackInfoReturnable<CompletableFuture<ChunkResult<CloAccess>>> cir) {
        if (!pos.isCube()) return;
        // TODO this is a bit of a disaster, why did mojang ever design their code around futures of Eithers
        // Logic for loading cube-adjacent chunks first, similar to getChunkRangeFuture
        List<CompletableFuture<ChunkResult<CloAccess>>> chunkLoadFutures = new ArrayList<>();

        for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
            for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                final CloPos chunkPos = CloPos.chunk(Coords.cubeToSection(pos.getX(), sectionX), Coords.cubeToSection(pos.getZ(), sectionZ));
                long chunkPosLong = chunkPos.toLong();
                ChunkHolder chunkholder = this.getUpdatingChunkIfPresent(chunkPosLong);
                if (chunkholder == null) { // This shouldn't occur as DistanceManager should add chunks to the ChunkMap before their corresponding cubes
                    cir.setReturnValue(CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                        @Override
                        public String toString() {
                            return "Unloaded " + chunkPos;
                        }
                    })));
                    return;
                }

                CompletableFuture<ChunkResult<CloAccess>> completablefuture = ((CloHolder) chunkholder).cc_getOrScheduleFuture(
                    ChunkStatus.EMPTY, (ChunkMap) (Object) this
                );
                chunkLoadFutures.add(completablefuture);
            }
        }

        CompletableFuture<List<ChunkResult<CloAccess>>> chunkResultsFuture = Util.sequence(chunkLoadFutures);
        CompletableFuture<ChunkResult<List<CloAccess>>> allChunksLoadedFuture = chunkResultsFuture.thenApply(p_183730_ -> {
            List<CloAccess> list2 = Lists.newArrayList();
            int index = 0;

            for(final ChunkResult<CloAccess> either : p_183730_) {
                if (either == null) {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }

                Optional<CloAccess> optional = either.left();
                if (optional.isEmpty()) {
                    final int indexFinal = index; // thanks java
                    return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                        @Override
                        public String toString() {
                            return "Unloaded chunk for cube " + pos + "offset: " + new ChunkPos(indexFinal % CubicConstants.DIAMETER_IN_SECTIONS, indexFinal / CubicConstants.DIAMETER_IN_SECTIONS) + " " + either.right().get();
                        }
                    });
                }

                list2.add(optional.get());
                ++index;
            }

            return Either.left(list2);
        });
        // Wait for adjacent chunks to load, and then load the cube
        // TODO allChunksLoadedFuture and cc_readChunk could (and probably should) run in parallel, it just makes this future logic a bit more complex
        cir.setReturnValue(allChunksLoadedFuture
            .thenCompose((result) -> result.map(
                left -> this.cc_readChunk(pos).thenApply(p_214925_ -> p_214925_.filter(p_214928_ -> {
                    boolean flag = isChunkDataValid(p_214928_);
                    if (!flag) {
                        LOGGER.error("Chunk file at {} is missing level data, skipping", pos);
                    }
                    return flag;
                })).<ChunkResult<CloAccess>>thenApplyAsync(p_313584_ -> {
                    this.level.getProfiler().incrementCounter("chunkLoad");
                    // TODO (P2) loading save data
//                    if (p_313584_.isPresent()) {
//                        ChunkAccess chunkaccess = ChunkSerializer.read(this.level, this.poiManager, pos, p_313584_.get());
//                        this.markPosition(pos, chunkaccess.getPersistedStatus().getChunkType());
//                        return Either.left(chunkaccess);
//                    } else {
                        return Either.left(this.cc_createEmptyChunk(pos));
//                    }
                }, this.mainThreadExecutor).exceptionallyAsync(p_214888_ -> this.cc_handleChunkLoadFailure(p_214888_, pos), this.mainThreadExecutor),
                right -> CompletableFuture.completedFuture(Either.right(right)))));

    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("handleChunkLoadFailure(Ljava/lang/Throwable;Lnet/minecraft/world/level/ChunkPos;)Lcom/mojang/datafixers/util/Either;"))
    private native ChunkResult<CloAccess> cc_handleChunkLoadFailure(Throwable exception, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(
            value = @MethodSig("createEmptyChunk(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    private native CloAccess cc_createEmptyChunk(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("markPositionReplaceable(Lnet/minecraft/world/level/ChunkPos;)V"))
    private native void cc_markPositionReplaceable(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("markPosition(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/status/ChunkStatus$ChunkType;)B"))
    private native byte cc_markPosition(CloPos cloPos, ChunkType chunkType);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("scheduleChunkGeneration(Lnet/minecraft/server/level/ChunkHolder;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<ChunkResult<CloAccess>> cc_scheduleChunkGeneration(ChunkHolder chunkHolder, ChunkStatus chunkStatus);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("releaseLightTicket(Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_releaseLightTicket(CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getDependencyStatus(Lnet/minecraft/world/level/chunk/status/ChunkStatus;I)Lnet/minecraft/world/level/chunk/status/ChunkStatus;"))
    private native ChunkStatus cc_getDependencyStatus(ChunkStatus chunkStatus, int p_140264_);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("protoChunkToFullChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<ChunkResult<CloAccess>> cc_protoChunkToFullChunk(ChunkHolder holder);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("prepareEntityTickingChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<LevelClo>> cc_prepareEntityTickingChunk(ChunkHolder holder);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("prepareTickingChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<LevelClo>> cc_prepareTickingChunk(ChunkHolder holder);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onChunkReadyToSend(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private native void cc_onChunkReadyToSend(LevelClo cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("prepareAccessibleChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<ChunkResult<LevelClo>> cc_prepareAccessibleChunk(ChunkHolder holder);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("saveChunkIfNeeded(Lnet/minecraft/server/level/ChunkHolder;)Z"))
    private native boolean cc_saveChunkIfNeeded(ChunkHolder holder);

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

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("anyPlayerCloseEnoughForSpawning(Lnet/minecraft/world/level/ChunkPos;)Z"))
    public native boolean cc_anyPlayerCloseEnoughForSpawning(CloPos cloPos);

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
        PacketDistributor.PLAYER.with(player).send(new CCClientboundSetCubeCacheCenterPacket(((CloTrackingView.Positioned) cloTrackingView).center().cubePos()));
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
    private native void processUnloads(BooleanSupplier hasMoreTime);

    // TODO resendBiomesForChunks - only used for FillBiomeCommand

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onFullChunkStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    public native void cc_onFullChunkStatusChange(CloPos cloPos, FullChunkStatus fullChunkStatus);

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
