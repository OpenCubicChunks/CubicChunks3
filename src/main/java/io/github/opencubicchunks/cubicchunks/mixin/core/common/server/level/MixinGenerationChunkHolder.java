package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubeLevel;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkMap;
import io.github.opencubicchunks.cubicchunks.server.level.GenerationCloHolder;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStep;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Dasm(ChunkToCubeSet.class)
@Mixin(GenerationChunkHolder.class)
public abstract class MixinGenerationChunkHolder implements GenerationCloHolder {
    @Shadow @Final protected ChunkPos pos;
    @Shadow @Final private AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> futures;
    @Shadow @Final private static ChunkResult<ChunkAccess> NOT_DONE_YET;

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(GenerationChunkHolder.class), field = @FieldSig(name = "pos", type = @Ref(ChunkPos.class)))
    protected CubePos cc_cubePos;

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(GenerationChunkHolder.class), method = @MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;"))
    @Override public CloPos cc_getCloPos() {
        if (cc_cubePos != null) {
            return CloPos.cube(cc_cubePos);
        }
        return CloPos.chunk(pos);
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("<init>(Lnet/minecraft/world/level/ChunkPos;)V"))
    public MixinGenerationChunkHolder() {
        throw new IllegalStateException("dasm failed to apply");
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("applyStep(Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/server/level/GeneratingChunkMap;Lnet/minecraft/util/StaticCache2D;)Ljava/util/concurrent/CompletableFuture;"))
    native CompletableFuture<ChunkResult<CubeAccess>> cc_applyCubeStep(CubeStep step, GeneratingChunkMap chunkMap, StaticCache3D<GenerationChunkHolder> cache);

    @WrapOperation(method = "updateHighestAllowedStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkLevel;generationStatus(I)Lnet/minecraft/world/level/chunk/status/ChunkStatus;"))
    @Nullable protected ChunkStatus cc_onUpdateHighestAllowedStatus_generationStatus(int level, Operation<ChunkStatus> original) {
        if (cc_cubePos != null) {
            return CubeLevel.cubeGenerationStatus(level);
        }
        return original.call(level);
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("replaceProtoChunk(Lnet/minecraft/world/level/chunk/ImposterProtoChunk;)V"))
    public native void cc_replaceProtoCube(ImposterProtoCube cube);

    @WrapOperation(method = "rescheduleChunkTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;scheduleGenerationTask(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/server/level/ChunkGenerationTask;"))
    private ChunkGenerationTask cc_onRescheduleChunkTask_scheduleGenerationTask(ChunkMap instance, ChunkStatus status, ChunkPos chunkPos, Operation<ChunkGenerationTask> original) {
        if (cc_cubePos != null) {
            return ((CubicChunkMap) instance).cc_scheduleGenerationTask(status, cc_cubePos);
        }
        return original.call(instance, status, chunkPos);
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("completeFuture(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/world/level/chunk/ChunkAccess;)V"))
    private native void cc_completeFuture(ChunkStatus targetStatus, CubeAccess cubeAccess);

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("getChunkIfPresentUnchecked(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public native CubeAccess cc_getCubeIfPresentUnchecked(ChunkStatus status);

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("getChunkIfPresent(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public native CubeAccess cc_getCubeIfPresent(ChunkStatus status);

    @Shadow @Nullable public abstract ChunkAccess getLatestChunk();

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("getLatestChunk()Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public native CubeAccess cc_getLatestCube();

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(GenerationChunkHolder.class), method = @MethodSig("getLatestChunk()Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public CloAccess cc_getLatestClo() {
        if (cc_cubePos != null) {
            return cc_getLatestCube();
        }
        return (CloAccess) getLatestChunk();
    }

    @SuppressWarnings("unchecked") @Inject(method = "getPersistedStatus", at = @At("HEAD"), cancellable = true)
    public void cc_onGetPersistedStatus(CallbackInfoReturnable<ChunkStatus> cir) {
        if (cc_cubePos != null) {
            CompletableFuture<ChunkResult<CubeAccess>> completablefuture = (CompletableFuture<ChunkResult<CubeAccess>>) (Object) this.futures.get(ChunkStatus.EMPTY.getIndex());
            CubeAccess cubeAccess = completablefuture == null ? null : completablefuture.getNow((ChunkResult<CubeAccess>) (Object) NOT_DONE_YET).orElse(null);
            cir.setReturnValue(cubeAccess == null ? null : cubeAccess.getPersistedStatus());
        }
    }

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(GenerationChunkHolder.class), value = @MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;"))
    public native CubePos cc_getCubePos();

    // TODO getLatestStatus - only used for vanilla debug code
}
