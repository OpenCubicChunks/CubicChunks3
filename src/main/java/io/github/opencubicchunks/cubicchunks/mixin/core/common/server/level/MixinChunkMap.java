package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import static java.util.Collections.swap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
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
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GeneralSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.SectionPosToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.storage.MixinChunkStorage;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkMap;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CubicChunkProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
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
@Dasm(GeneralSet.class)
@Mixin(ChunkMap.class)
public abstract class MixinChunkMap extends MixinChunkStorage implements CubicChunkMap {
    // TODO maybe don't shadow logger; use our own?
    @Shadow @Final private static Logger LOGGER;

    private static final List<ChunkStatus> cc_CHUNK_STATUSES = ChunkStatus.getStatusList();

    @Shadow public abstract ReportedException debugFuturesAndCreateReportedException(IllegalStateException pException, String pDetails);

    @Shadow protected abstract ChunkHolder getUpdatingChunkIfPresent(long aLong);

    @AddFieldToSets(sets = GeneralSet.class, owner = @Ref(ChunkMap.class), field = @FieldSig(type = @Ref(ChunkProgressListener.class), name = "progressListener"))
    private CubicChunkProgressListener cc_progressListener;

    // TODO once we can target non-return locations in constructors, do this when the vanilla field is set
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ServerLevel pLevel, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, DataFixer pFixerUpper, StructureTemplateManager pStructureManager,
                        Executor pDispatcher, BlockableEventLoop pMainThreadExecutor, LightChunkGetter pLightChunk, ChunkGenerator pGenerator, ChunkProgressListener pProgressListener,
                        ChunkStatusUpdateListener pChunkStatusListener, Supplier pOverworldDataStorage, int pViewDistance, boolean pSync, CallbackInfo ci) {
        cc_progressListener = ((CubicChunkProgressListener) pProgressListener);
    }

    /**
     * Returns the squared distance to the center of the cube.
     */
    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkMap.class), method = @MethodSig("euclideanDistanceSquared(Lnet/minecraft/world/level/ChunkPos;"
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
            && !player.connection.chunkSender.isPending(CloPos.asLong(x, y, z));
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
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("getChunkRangeFuture(Lnet/minecraft/server/level/ChunkHolder;ILjava/util/function/IntFunction;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Either<List<CloAccess>, ChunkHolder.ChunkLoadingFailure>> cc_getChunkRangeFuture(ChunkHolder cloHolder, int radius,
                                                                                                                      IntFunction<ChunkStatus> statusByRadius);
    private static ChunkStatus cc_getChildStatus(ChunkStatus status) {
        int index = status.getIndex();
        return index >= cc_CHUNK_STATUSES.size() ? ChunkStatus.FULL : cc_CHUNK_STATUSES.get(index + 1);
    }

    // TODO this could be substantially improved probably hopefully
    /**
     * Cubes require different adjacency logic compared to Chunks
     */
    @Dynamic @Inject(method = "cc_dasm$cc_getChunkRangeFuture", at = @At("HEAD"), cancellable = true)
    private void cc_onGetChunkRangeFuture(ChunkHolder cloHolder, int radius, IntFunction<ChunkStatus> statusByRadius,
                                          CallbackInfoReturnable<CompletableFuture<Either<List<CloAccess>, ChunkHolder.ChunkLoadingFailure>>> cir) {
        CloPos pos = ((CubicChunkHolder) cloHolder).cc_getPos();
        if (!pos.isCube()) return;
        // The vanilla method has an early exit for radius=0 here; this is not valid for cubes because even if radius=0 we still depend on chunks that neighbor the cube
        List<CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>>> dependencyFutures = new ArrayList<>();
        List<ChunkHolder> cloHolders = new ArrayList<>();
        int middleCubeIndex = -1;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                // We want the chunks intersecting this column of cubes to be loaded at the maximum level of any of those cubes;
                // this occurs when dy=0, so we only consider x/z distance
                int chunkDistance = Math.max(Math.abs(dz), Math.abs(dx));
                for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
                    for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                        ChunkHolder holder = this.getUpdatingChunkIfPresent(CloPos.asLong(Coords.cubeToSection(pos.getX()+dx, sectionX), Coords.cubeToSection(pos.getZ()+dz, sectionZ)));
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
                        var future = ((CubicChunkHolder) holder).cc_getOrScheduleFuture(expectedStatus, (ChunkMap) (Object) this);
                        cloHolders.add(holder);
                        dependencyFutures.add(future);
                    }
                }
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        middleCubeIndex = cloHolders.size();
                    }
                    ChunkHolder holder = this.getUpdatingChunkIfPresent(CloPos.asLong(pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz));
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
                    var future = ((CubicChunkHolder) holder).cc_getOrScheduleFuture(expectedStatus, (ChunkMap) (Object) this);
                    cloHolders.add(holder);
                    dependencyFutures.add(future);
                }
            }
        }

        // Vanilla expects that the center chunk is in the middle of the list; this is not the case for cubes, so we manually swap the center cube to the middle
        // - this is a temporary approach, until we make our own cube+chunk list wrapper
        swap(cloHolders, middleCubeIndex, cloHolders.size() / 2);
        swap(dependencyFutures, middleCubeIndex, cloHolders.size() / 2);

        var sequencedFuture = Util.sequence(dependencyFutures);
        CompletableFuture<Either<List<CloAccess>, ChunkHolder.ChunkLoadingFailure>> combinedFuture = sequencedFuture.thenApply(p_183730_ -> {
                List<CloAccess> list2 = Lists.newArrayList();
                int k1 = 0;

                for(final Either<CloAccess, ChunkHolder.ChunkLoadingFailure> either : p_183730_) {
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
            ((CubicChunkHolder) holder).cc_addSaveDependency("getChunkRangeFuture " + pos + " " + radius, combinedFuture);
        }

        cir.setReturnValue(combinedFuture);
    }

    // dasm + mixin
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("updateChunkScheduling(JILnet/minecraft/server/level/ChunkHolder;I)Lnet/minecraft/server/level/ChunkHolder;"))
    @Nullable native ChunkHolder cc_updateChunkScheduling(long pChunkPos, int pNewLevel, @Nullable ChunkHolder pHolder, int pOldLevel);

    // TODO move to forge sourceset
    /**
     * Only call Forge hook for chunks, not cubes
     */
    @Dynamic @WrapWithCondition(method = "cc_dasm$cc_updateChunkScheduling", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;fireChunkTicketLevelUpdated"
        + "(Lnet/minecraft/server/level/ServerLevel;JIILnet/minecraft/server/level/ChunkHolder;)V"))
    private boolean cc_updateChunkScheduling_onForgeHook(ServerLevel level, long pos, int oldLevel, int newLevel, ChunkHolder holder) {
        return CloPos.isChunk(pos);
    }

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("saveAllChunks(Z)V"))
    protected native void cc_saveAllChunks(boolean flush);

    // P4: scheduleUnload lambda we'll want to mirror the forge API for cubes
    // FIXME remove call to forge hook in copied method
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("scheduleUnload(JLnet/minecraft/server/level/ChunkHolder;)V"))
    private native void cc_scheduleUnload(long pChunkPos, ChunkHolder pChunkHolder);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("schedule(Lnet/minecraft/server/level/ChunkHolder;Lnet/minecraft/world/level/chunk/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_schedule(ChunkHolder pHolder, ChunkStatus pStatus);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("scheduleChunkLoad(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_scheduleChunkLoad(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("handleChunkLoadFailure(Ljava/lang/Throwable;Lnet/minecraft/world/level/ChunkPos;)Lcom/mojang/datafixers/util/Either;"))
    private native Either<CloAccess, ChunkHolder.ChunkLoadingFailure> cc_handleChunkLoadFailure(Throwable exception, CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
            value = @MethodSig("createEmptyChunk(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    private native CloAccess cc_createEmptyChunk(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("markPositionReplaceable(Lnet/minecraft/world/level/ChunkPos;)V"))
    private native void cc_markPositionReplaceable(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("markPosition(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus$ChunkType;)B"))
    private native byte cc_markPosition(CloPos cloPos, ChunkStatus.ChunkType chunkType);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("scheduleChunkGeneration(Lnet/minecraft/server/level/ChunkHolder;Lnet/minecraft/world/level/chunk/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_scheduleChunkGeneration(ChunkHolder pChunkHolder, ChunkStatus pChunkStatus);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("releaseLightTicket(Lnet/minecraft/world/level/ChunkPos;)V"))
    protected native void cc_releaseLightTicket(ChunkPos pChunkPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("getDependencyStatus(Lnet/minecraft/world/level/chunk/ChunkStatus;I)Lnet/minecraft/world/level/chunk/ChunkStatus;"))
    private native ChunkStatus cc_getDependencyStatus(ChunkStatus pChunkStatus, int p_140264_);

    // FIXME remove call to forge hook in copied method
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("protoChunkToFullChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_protoChunkToFullChunk(ChunkHolder pHolder);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("prepareTickingChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<Either<LevelClo, ChunkHolder.ChunkLoadingFailure>> cc_prepareTickingChunk(ChunkHolder pHolder);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("onChunkReadyToSend(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private native void cc_onChunkReadyToSend(LevelClo cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("prepareAccessibleChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;"))
    public native CompletableFuture<Either<LevelClo, ChunkHolder.ChunkLoadingFailure>> cc_prepareAccessibleChunk(ChunkHolder pHolder);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("saveChunkIfNeeded(Lnet/minecraft/server/level/ChunkHolder;)Z"))
    private native boolean cc_saveChunkIfNeeded(ChunkHolder holder);

    // FIXME remove call to forge hook in copied method
    // dasm + mixin
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"))
    private native boolean cc_save(CloAccess cloAccess);

    /**
     * Redirect error logging to log with CloPos
     */
    @Dynamic @Inject(method = "cc_dasm$cc_save", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks/world/level/chunklike/CloPos;getX()I"), cancellable = true)
    private void cc_onSave_errorLog(CloAccess cloAccess, CallbackInfoReturnable<Boolean> cir, @Local Exception exception) {
        LOGGER.error("Failed to save chunk or cube {}", cloAccess.cc_getCloPos().toString(), exception);
        cir.setReturnValue(false);
    }

    // This calls ChunkSerializer.getChunkTypeFromTag, which could be an issue?
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("isExistingChunkFull(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private native boolean cc_isExistingChunkFull(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V"))
    private native void cc_markChunkPendingToSend(ServerPlayer player, CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private static native void cc_markChunkPendingToSend(ServerPlayer player, LevelClo clo);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("dropChunk(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V"))
    private static native void cc_dropChunk(ServerPlayer player, CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("getChunkToSend(J)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    public native LevelClo cc_getChunkToSend(long cloPos);

    // dumpChunks (low prio)

    // printFuture - only ever called in dumpChunks

    // TODO (P2) readChunk: this.upgradeChunkTag might need a dasm redirect?

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("readChunk(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Optional<CompoundTag>> cc_readChunk(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("anyPlayerCloseEnoughForSpawning(Lnet/minecraft/world/level/ChunkPos;)Z"))
    native boolean cc_anyPlayerCloseEnoughForSpawning(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("getPlayersCloseForSpawning(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/List;"))
    public native List<ServerPlayer> cc_getPlayersCloseForSpawning(CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("playerIsCloseEnoughForSpawning(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private native boolean cc_playerIsCloseEnoughForSpawning(ServerPlayer player, CloPos cloPos);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("updatePlayerStatus(Lnet/minecraft/server/level/ServerPlayer;Z)V"))
    native void cc_updatePlayerStatus(ServerPlayer pPlayer, boolean pTrack);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("move(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public native void cc_move(ServerPlayer pPlayer);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("updateChunkTracking(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private native void cc_updateChunkTracking(ServerPlayer pPlayer);

    // dasm + mixin - TODO redirect for ClientboundSetChunkCacheCenterPacket construction, 2 -> 3 ints
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("applyChunkTrackingView(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ChunkTrackingView;)V"))
    private native void cc_applyChunkTrackingView(ServerPlayer pPlayer, ChunkTrackingView pChunkTrackingView);

    // dasm + mixin
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("getPlayers(Lnet/minecraft/world/level/ChunkPos;Z)Ljava/util/List;"))
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
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(value = @MethodSig("tick()V"), useRedirectSets = { GeneralSet.class, SectionPosToCubeSet.class })
    protected native void cc_tick();

    // TODO resendBiomesForChunks - only used for FillBiomeCommand

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("onFullChunkStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    native void cc_onFullChunkStatusChange(CloPos cloPos, FullChunkStatus fullChunkStatus);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("waitForLightBeforeSending(Lnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_waitForLightBeforeSending(CloPos cloPos, int p_301130_);

    // TODO these three are temporary - needs dasm subclass method redirect inheritance for non-overriden methods
    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkMap.class),
        method = @MethodSig("isOldChunkAround(Lnet/minecraft/world/level/ChunkPos;I)Z"))
    public boolean cc_isOldChunkAround(CloPos pPos, int pRadius) {
        return super.cc_isOldChunkAround(pPos, pRadius);
    }

    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkMap.class),
        method = @MethodSig("read(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Optional<CompoundTag>> cc_read(CloPos cloPos) {
        return super.cc_read(cloPos);
    }

    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkMap.class),
        method = @MethodSig("write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"))
    public void cc_write(CloPos cloPos, CompoundTag chunkData) {
        super.cc_write(cloPos, chunkData);
    }
}
