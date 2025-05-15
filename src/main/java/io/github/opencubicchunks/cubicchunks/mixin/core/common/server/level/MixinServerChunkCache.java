package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.ChunkMapAccess;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.MixinChunkSource;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.server.level.CloHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CubicDistanceManager;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
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
 * The vanilla {@link ServerChunkCache} class stores all loaded chunks on the server and has methods for getting chunks, ticking all chunks, handling block+light updates, managing chunkloading tickets, etc.
 * This mixin adds versions of these methods for cubes, meaning that this class now stores and manages both cubes and chunks.
 */
@Dasm(ChunkToCubeSet.class)
@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache extends MixinChunkSource implements ServerCubeCache {
    // Cube equivalents for cached chunks
    @Shadow @Final private static int CACHE_SIZE;

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), field = @FieldSig(type = @Ref(long[].class), name = "lastChunkPos"))
    private final long[] cc_lastCubePos = new long[CACHE_SIZE];

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), field = @FieldSig(type = @Ref(ChunkStatus[].class), name = "lastChunkStatus"))
    private final ChunkStatus[] cc_lastCubeStatus = new ChunkStatus[CACHE_SIZE];

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), field = @FieldSig(type = @Ref(CloAccess[].class), name = "lastChunk"))
    private final CubeAccess[] cc_lastCube = new CubeAccess[CACHE_SIZE];

    @Shadow @Final public ServerLevel level;

    @Shadow @Nullable protected abstract ChunkHolder getVisibleChunkIfPresent(long pChunkPos);

    @Shadow @Final Thread mainThread;

    @Shadow @Final private ServerChunkCache.MainThreadExecutor mainThreadProcessor;

    @Shadow @Final private DistanceManager distanceManager;

    @Shadow @Final public ChunkMap chunkMap;

    @Shadow private long lastInhabitedUpdate;

    @Shadow @Nullable private NaturalSpawner.SpawnState lastSpawnState;

    @Shadow protected abstract void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_);

    // TODO inject at head once we can inject at non-return locations
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void cc_onInit(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager, Executor dispatcher,
                        ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progressListener,
                        ChunkStatusUpdateListener chunkStatusListener, Supplier overworldDataStorage, CallbackInfo ci) {
        if (((CanBeCubic) level).cc_isCubic())  {
            this.cc_setCubic();
        }
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(@MethodSig("storeInCache(JLnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"))
    private native void cc_storeInCache(long pChunkPos, CubeAccess pChunk, ChunkStatus pChunkStatus);

    @TransformFromMethod(@MethodSig("getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Override @Nullable public native CubeAccess cc_getCube(int chunkX, @AddUnusedParam int chunkY, int chunkZ, ChunkStatus requiredStatus, boolean load);

    // mixin-into-dasm to replace call to getChunk with getCube
    @Dynamic @Inject(method = "cc_dasm$cc_getCube", cancellable = true, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private void cc_getCube_supplyAsync(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad, CallbackInfoReturnable<CubeAccess> cir) {
        cir.setReturnValue(CompletableFuture.supplyAsync(() -> this.cc_getCube(pChunkX, pChunkY, pChunkZ, pRequiredStatus, pLoad), this.mainThreadProcessor).join());
    }

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of getCube
    @Dynamic @Redirect(method = "cc_dasm$cc_getCube", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/world/level/CloPos;chunkAsLong(II)J"))
    private long cc_getCube_posAsLong(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CloPos.cubeAsLong(pX, pY, pZ);
    }

    // The second through fifth params are the params to the call being redirected; the next three params are the x/y/z coordinates in the params of getCube
    @Dynamic @Redirect(method = "cc_dasm$cc_getCube", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getChunkFutureMainThread(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture cc_getCube_getChunkFutureMainThread(ServerChunkCache instance, int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load, int chunkXRepeated, int chunkY, int chunkZRepeated) {
        return this.cc_getCubeFutureMainThread(chunkX, chunkY, chunkZ, requiredStatus, load);
    }

    @TransformFromMethod(@MethodSig("getChunkNow(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Override @Nullable public native LevelCube cc_getCubeNow(int pChunkX, @AddUnusedParam int chunkY, int pChunkZ);

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of getCubeNow
    @Dynamic @Redirect(method = "cc_dasm$cc_getCubeNow", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/world/level/CloPos;chunkAsLong(II)J"))
    private long cc_getCubeNow_posAsLong(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CloPos.cubeAsLong(pX, pY, pZ);
    }

    // Note that we don't add this to any redirect sets; we just need it for the below mixin
    // (Whenever clearing caches, we want to clear all caches, not just cubes or chunks specifically)
    @TransformFromMethod(@MethodSig("clearCache()V"))
    private native void cc_clearCache();

    /**
     * When clearing caches, clear the cube cache as well as the chunk cache
     */
    @Inject(method = "clearCache", at = @At("HEAD"))
    private void cc_onClearCache(CallbackInfo ci) {
        cc_clearCache();
    }

    // This method requires enough manual redirects that we just replace it entirely
    @Override
    public CompletableFuture<Either<CubeAccess, ChunkHolder.ChunkLoadingFailure>> cc_getCubeFuture(
        int pX, int chunkY, int pZ, ChunkStatus pChunkStatus, boolean pLoad
    ) {
        boolean flag = Thread.currentThread() == this.mainThread;
        CompletableFuture<Either<CubeAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture;
        if (flag) {
            completablefuture = this.cc_getCubeFutureMainThread(pX, chunkY, pZ, pChunkStatus, pLoad);
            this.mainThreadProcessor.managedBlock(completablefuture::isDone);
        } else {
            completablefuture = CompletableFuture.supplyAsync(
                    () -> this.cc_getCubeFutureMainThread(pX, chunkY, pZ, pChunkStatus, pLoad), this.mainThreadProcessor
                )
                .thenCompose(p_8413_ -> p_8413_);
        }

        return completablefuture;
    }

    @WrapOperation(method = "getChunkFutureMainThread", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;getOrScheduleFuture(Lnet/minecraft/world/level/chunk/ChunkStatus;Lnet/minecraft/server/level/ChunkMap;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> cc_onGetChunkFutureMainThread(ChunkHolder chunkHolder, ChunkStatus status, ChunkMap chunkMap,
                                                                                                                  Operation<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> original) {
        if (!cc_isCubic) return original.call(chunkHolder, status, chunkMap);
        return (CompletableFuture) ((CloHolder) chunkHolder).cc_getOrScheduleFuture(status, chunkMap);
    }

    @TransformFromMethod(@MethodSig("getChunkFutureMainThread(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<Either<CubeAccess, ChunkHolder.ChunkLoadingFailure>> cc_getCubeFutureMainThread(
        int pX, @AddUnusedParam int chunkY, int pZ, ChunkStatus pChunkStatus, boolean pLoad
    );

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of cc_getCubeFutureMainThread
    @Dynamic @Redirect(method = "cc_dasm$cc_getCubeFutureMainThread", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/world/level/CloPos;chunk(II)Lio/github/opencubicchunks/cc_core/world/level/CloPos;"))
    private CloPos cc_getCubeFutureMainThread_chunkPosConstruct(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CloPos.cube(pX, pY, pZ);
    }

    @TransformFromMethod(@MethodSig("hasChunk(II)Z"))
    public native boolean cc_hasCube(int pX, @AddUnusedParam int y, int pZ);

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of cc_hasCube
    @Dynamic @Redirect(method = "cc_dasm$cc_hasCube", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/world/level/CloPos;chunk(II)Lio/github/opencubicchunks/cc_core/world/level/CloPos;"))
    private CloPos cc_hasCube_posAsLong(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CloPos.cube(pX, pY, pZ);
    }

    // TODO (P2) - lighting; currently unused. can probably be done with dasm and @AddUnusedParam
    @Nullable public LightChunk cc_getCubeForLighting(int pChunkX, int chunkY, int pChunkZ) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @WrapOperation(method = "runDistanceManagerUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/DistanceManager;runAllUpdates(Lnet/minecraft/server/level/ChunkMap;)Z"))
    private boolean cc_onRunDistanceManagerUpdates(DistanceManager instance, ChunkMap chunkMap, Operation<Boolean> original) {
        if (!((CanBeCubic) level).cc_isCubic()) {
            return original.call(instance, chunkMap);
        }
        return ((CubicDistanceManager) instance).cc_runAllUpdates(chunkMap);
    }

    /**
     * In cubic levels, redirect to the cubic tickChunks method
     */
    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;tickChunks()V"))
    private boolean cc_onTickChunks(ServerChunkCache instance) {
        if (((CanBeCubic) level).cc_isCubic()) {
            cc_tickChunks();
            return false;
        }
        return true;
    }

    // This could maybe be DASM, but the mixins into the copied method would likely end up being quite complex
    private void cc_tickChunks() {
        long i = this.level.getGameTime();
        long j = i - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = i;
        if (!this.level.isDebug()) {
            ProfilerFiller profilerfiller = this.level.getProfiler();
            profilerfiller.push("pollingChunks");
            profilerfiller.push("filteringLoadedChunks");
            List<CloAndHolder> list = Lists.newArrayListWithCapacity(this.chunkMap.size());

            for(ChunkHolder chunkholder : ((ChunkMapAccess) this.chunkMap).cc_invokeGetChunks()) {
                LevelClo levelchunk = ((CloHolder) chunkholder).cc_getTickingChunk();
                if (levelchunk != null) {
                    list.add(new CloAndHolder(levelchunk, chunkholder));
                }
            }

            if (this.level.getServer().tickRateManager().runsNormally()) {
                profilerfiller.popPush("naturalSpawnCount");
                int l = this.distanceManager.getNaturalSpawnChunkCount();
                NaturalSpawner.SpawnState naturalspawner$spawnstate = NaturalSpawner.createState(
                    l, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap)
                );
                this.lastSpawnState = naturalspawner$spawnstate;
                profilerfiller.popPush("spawnAndTick");
                boolean flag1 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
                Util.shuffle(list, this.level.random);
                int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
                boolean flag = this.level.getLevelData().getGameTime() % 400L == 0L;

                for(CloAndHolder serverchunkcache$chunkandholder : list) {
                    LevelClo levelchunk1 = serverchunkcache$chunkandholder.chunk();
                    CloPos chunkpos = levelchunk1.cc_getCloPos();
                    // TODO isNaturalSpawningAllowed
//                    if ((this.level.isNaturalSpawningAllowed(chunkpos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos)) || this.distanceManager.shouldForceTicks(chunkpos.toLong())) {
//                        levelchunk1.incrementInhabitedTime(j);
//                        if (flag1 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkpos)) {
//                            NaturalSpawner.spawnForChunk(this.level, levelchunk1, naturalspawner$spawnstate, this.spawnFriendlies, this.spawnEnemies, flag);
//                        }
//
//                        if (this.level.shouldTickBlocksAt(chunkpos.toLong())) {
//                            this.level.tickChunk(levelchunk1, k);
//                        }
//                    }
                }

                profilerfiller.popPush("customSpawners");
                // TODO spawning
//                if (flag1) {
//                    this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
//                }
            }

            profilerfiller.popPush("broadcast");
            list.forEach(p_184022_ -> ((CloHolder) p_184022_.holder()).cc_broadcastChanges(p_184022_.chunk()));
            profilerfiller.pop();
            profilerfiller.pop();
        }
    }

    // needs manual impl because needs to use cube rather than chunk
    @Override
    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("blockChanged(Lnet/minecraft/core/BlockPos;)V"))
    public void cc_blockChanged(BlockPos pos) {
        int x = Coords.blockToCube(pos.getX());
        int y = Coords.blockToCube(pos.getY());
        int z = Coords.blockToCube(pos.getZ());
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(CloPos.cubeAsLong(x, y, z));
        if (chunkholder != null) {
            chunkholder.blockChanged(pos);
        }
    }

    @Override
    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("onLightUpdate(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/SectionPos;)V"))
    public void cc_onLightUpdate(LightLayer pType, SectionPos pPos) {
        // TODO (P2) lighting
    }

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public native <T> void cc_addRegionTicket(TicketType<T> pType, CloPos pPos, int pDistance, T pValue);
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public native <T> void cc_addRegionTicket(TicketType<T> p_8388_, CloPos p_8389_, int p_8390_, T p_8391_, boolean forceTicks);

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public native <T> void cc_removeRegionTicket(TicketType<T> pType, CloPos pPos, int pDistance, T pValue);
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public native <T> void cc_removeRegionTicket(TicketType<T> p_8439_, CloPos p_8440_, int p_8441_, T p_8442_, boolean forceTicks);

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)V"))
    public native void cc_updateChunkForced(CloPos pPos, boolean pAdd);

    // TODO should probably be implemented properly, but is low priority (debug)
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("getChunkDebugData(Lnet/minecraft/world/level/ChunkPos;)Ljava/lang/String;"))
    public native String cc_getChunkDebugData(CloPos pChunkPos);
}
