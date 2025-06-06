package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.datafixers.DataFixer;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.MixinChunkSource;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
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

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), field = @FieldSig(type = @Ref(List.class), name = "spawningChunks"))
    private final List<LevelCube> cc_spawningCubes = new ObjectArrayList<>();

    @Shadow @Final public ServerLevel level;

    @Shadow @Nullable protected abstract ChunkHolder getVisibleChunkIfPresent(long pChunkPos);

    @Shadow @Final Thread mainThread;

    @Shadow @Final private ServerChunkCache.MainThreadExecutor mainThreadProcessor;

    @Shadow @Final private DistanceManager distanceManager;

    @Shadow @Final public ChunkMap chunkMap;

    @Shadow private long lastInhabitedUpdate;

    @Shadow @Nullable private NaturalSpawner.SpawnState lastSpawnState;

    @Shadow protected abstract void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_);

    @Inject(method = "<init>", at = @At("CTOR_HEAD"))
    private void cc_onInit(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager, Executor dispatcher,
                        ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progressListener,
                        ChunkStatusUpdateListener chunkStatusListener, Supplier overworldDataStorage, CallbackInfo ci) {
        if (((CanBeCubic) level).cc_isCubic())  {
            this.cc_setCubic();
        }
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(@MethodSig("storeInCache(JLnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V"))
    private native void cc_storeInCache(long pChunkPos, CubeAccess pChunk, ChunkStatus pChunkStatus);

    @TransformFromMethod(@MethodSig("getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Override @Nullable public native CubeAccess cc_getCube(int chunkX, @AddUnusedParam int chunkY, int chunkZ, ChunkStatus requiredStatus, boolean load);

    // mixin-into-dasm to replace call to getChunk with getCube
    @Dynamic @Inject(method = "cc_dasm$cc_getCube", cancellable = true, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private void cc_getCube_supplyAsync(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad, CallbackInfoReturnable<CubeAccess> cir) {
        cir.setReturnValue(CompletableFuture.supplyAsync(() -> this.cc_getCube(pChunkX, pChunkY, pChunkZ, pRequiredStatus, pLoad), this.mainThreadProcessor).join());
    }

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of getCube
    @Dynamic @Redirect(method = "cc_dasm$cc_getCube", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/api/CubePos;dummy_chunkAsLong(II)J"))
    private long cc_getCube_posAsLong(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CloPos.cubeAsLong(pX, pY, pZ);
    }

    // The second through fifth params are the params to the call being redirected; the next three params are the x/y/z coordinates in the params of getCube
    @Dynamic @Redirect(method = "cc_dasm$cc_getCube", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getChunkFutureMainThread(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture cc_getCube_getChunkFutureMainThread(ServerChunkCache instance, int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load, int chunkXRepeated, int chunkY, int chunkZRepeated) {
        return this.cc_getCubeFutureMainThread(chunkX, chunkY, chunkZ, requiredStatus, load);
    }

    @TransformFromMethod(@MethodSig("getChunkNow(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Override @Nullable public native LevelCube cc_getCubeNow(int pChunkX, @AddUnusedParam int chunkY, int pChunkZ);

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of getCubeNow
    @Dynamic @Redirect(method = "cc_dasm$cc_getCubeNow", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/api/CubePos;dummy_chunkAsLong(II)J"))
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
    public CompletableFuture<ChunkResult<CubeAccess>> cc_getCubeFuture(
        int pX, int chunkY, int pZ, ChunkStatus pChunkStatus, boolean pLoad
    ) {
        boolean flag = Thread.currentThread() == this.mainThread;
        CompletableFuture<ChunkResult<CubeAccess>> completablefuture;
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

    @TransformFromMethod(@MethodSig("getChunkFutureMainThread(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Ljava/util/concurrent/CompletableFuture;"))
    private native CompletableFuture<ChunkResult<CubeAccess>> cc_getCubeFutureMainThread(
        int pX, @AddUnusedParam int chunkY, int pZ, ChunkStatus pChunkStatus, boolean pLoad
    );

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of cc_getCubeFutureMainThread
    @Dynamic @Redirect(method = "cc_dasm$cc_getCubeFutureMainThread", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/api/CubePos;dummy_fromChunkCoords(II)Lio/github/opencubicchunks/cc_core/api/CubePos;"))
    private CubePos cc_getCubeFutureMainThread_chunkPosConstruct(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CubePos.of(pX, pY, pZ);
    }

    @TransformFromMethod(@MethodSig("hasChunk(II)Z"))
    public native boolean cc_hasCube(int pX, @AddUnusedParam int y, int pZ);

    // The first two params are the x and z coordinates inside the call being redirected; the next three params are the x/y/z coordinates in the params of cc_hasCube
    @Dynamic @Redirect(method = "cc_dasm$cc_hasCube", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cc_core/api/CubePos;dummy_fromChunkCoords(II)Lio/github/opencubicchunks/cc_core/api/CubePos;"))
    private CubePos cc_hasCube_posAsLong(int pX, int pZ, int pXRepeated, int pY, int pZRepeated) {
        return CubePos.of(pX, pY, pZ);
    }

    // TODO (P2) - lighting; currently unused. can probably be done with dasm and @AddUnusedParam
    @Nullable public LightChunk cc_getCubeForLighting(int pChunkX, int chunkY, int pChunkZ) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cc_onTick(BooleanSupplier hasTimeLeft, boolean tickChunks, CallbackInfo ci) {
        if (this.cc_isCubic) {
            ci.cancel();
            cc_tick(hasTimeLeft, tickChunks);
        }
    }

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("tick(Ljava/util/function/BooleanSupplier;Z)V"))
    public native void cc_tick(BooleanSupplier hasTimeLeft, boolean tickChunks);

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(useRedirectSets = ChunkToCloSet.class, value = @MethodSig("broadcastChangedChunks(Lnet/minecraft/util/profiling/ProfilerFiller;)V"))
    private native void cc_broadcastChangedClos(ProfilerFiller profiler);

    @Inject(method = "broadcastChangedChunks", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaBroadcastChangedChunks(ProfilerFiller profiler, CallbackInfo ci) {
        if (cc_isCubic) {
            ci.cancel();
            cc_broadcastChangedClos(profiler);
        }
    }

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(useRedirectSets = ChunkToCloSet.class, value = @MethodSig("tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V"))
    private native void cc_tickClos(ProfilerFiller profiler, long timeInhabited);

    @Inject(method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaTickChunks(ProfilerFiller profiler, long timeInhabited, CallbackInfo ci) {
        if (cc_isCubic) {
            ci.cancel();
            cc_tickClos(profiler, timeInhabited);
        }
    }

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("tickSpawningChunk(Lnet/minecraft/world/level/chunk/LevelChunk;JLjava/util/List;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;)V"))
    private void cc_tickSpawningClo(LevelClo levelClo, long timeInhabited, List<MobCategory> spawnCategories, NaturalSpawner.SpawnState spawnState) {
        // TODO (P2)
    }

    // TODO just inject and do this in vanilla method?
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

    @Inject(method = "onLightUpdate", at = @At("HEAD"), cancellable = true)
    private void cc_onVanillaOnLightUpdate(LightLayer type, SectionPos pos, CallbackInfo ci) {
        if (this.cc_isCubic) {
            ci.cancel();
            cc_onLightUpdate(type, pos);
        }
    }

    @Override
    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("onLightUpdate(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/SectionPos;)V"))
    public void cc_onLightUpdate(LightLayer pType, SectionPos pPos) {
        // TODO (P2) lighting
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(useRedirectSets = ChunkToCloSet.class, value = @MethodSig("addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_addTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(useRedirectSets = ChunkToCloSet.class, value = @MethodSig("addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_addTicketWithRadius(TicketType ticket, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(useRedirectSets = ChunkToCloSet.class, value = @MethodSig("removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_removeTicketWithRadius(TicketType ticket, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(useRedirectSets = ChunkToCloSet.class, value = @MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)Z"))
    public native boolean cc_updateCloForced(CloPos pPos, boolean pAdd);

    // Cube-specific methods that delegate to the corresponding Clo methods
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    public void cc_addTicket(Ticket ticket, CubePos cubePos) {
        cc_addTicket(ticket, CloPos.cube(cubePos));
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    public void cc_addTicketWithRadius(TicketType ticket, CubePos cubePos, int radius) {
        cc_addTicketWithRadius(ticket, CloPos.cube(cubePos), radius);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    public void cc_removeTicketWithRadius(TicketType ticket, CubePos cubePos, int radius) {
        cc_removeTicketWithRadius(ticket, CloPos.cube(cubePos), radius);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ServerChunkCache.class), method = @MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)Z\""))
    @Override public boolean cc_updateCubeForced(CubePos cubePos, boolean forced) {
        return cc_updateCloForced(CloPos.cube(cubePos), forced);
    }

    // TODO should probably be implemented properly, but is low priority (debug)
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getChunkDebugData(Lnet/minecraft/world/level/ChunkPos;)Ljava/lang/String;"))
    public native String cc_getChunkDebugData(CloPos pChunkPos);
}
