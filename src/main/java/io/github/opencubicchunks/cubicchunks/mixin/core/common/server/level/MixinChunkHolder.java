package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

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
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.exception.DasmFailedToApply;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.server.level.CloHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CubeHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkMap;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The vanilla {@link ChunkHolder} class extends {@link GenerationChunkHolder} to have methods for getting a fully loaded chunk, and handle saving dependencies and broadcasting updates to clients.
 * This mixin adds cubic chunks equivalents for methods where necessary, to allow GenerationChunkHolder to dynamically wrap either a chunk or a cube (i.e. a CLO).
 */
@Dasm(ChunkToCubeSet.class)
@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder extends MixinGenerationChunkHolder implements CloHolder, CubeHolder {
    @Shadow private boolean hasChangedSections;
    @Shadow @Final private ShortSet[] changedBlocksPerSection;

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(type = @Ref(ChunkHolder.LevelChangeListener.class), name = "onLevelChange"))
    private final LevelChangeListener cc_onLevelChange;

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(type = @Ref(ChunkHolder.PlayerProvider.class), name = "playerProvider"))
    private final PlayerProvider cc_playerProvider;

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkHolder.class), value = @MethodSig("<init>(Lnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/lighting/LevelLightEngine;Lnet/minecraft/server/level/ChunkHolder$LevelChangeListener;Lnet/minecraft/server/level/ChunkHolder$PlayerProvider;)V"))
    public MixinChunkHolder() {
        throw new DasmFailedToApply();
    }

    @Shadow @Nullable public abstract LevelChunk getTickingChunk();

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkHolder.class), value = @MethodSig("getTickingChunk()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public native LevelCube cc_getTickingCube();

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("getTickingChunk()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public LevelClo cc_getTickingClo() {
        if (cc_cubePos != null) {
            return cc_getTickingCube();
        }
        return (LevelClo) getTickingChunk();
    }

    @Shadow @Nullable public abstract LevelChunk getChunkToSend();

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkHolder.class), value = @MethodSig("getChunkToSend()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public native LevelCube cc_getCubeToSend();

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(ChunkHolder.class), value = @MethodSig("getChunkToSend()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public LevelClo cc_getCloToSend() {
        if (cc_cubePos != null) {
            return cc_getCubeToSend();
        }
        return (LevelClo) getChunkToSend();
    }

    @Inject(method = "blockChanged", at = @At("HEAD"), cancellable = true)
    public void cc_onBlockChanged(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cc_cubePos != null) {
            cir.setReturnValue(cc_blockChanged(pos));
        }
    }

    // region [cc_blockChanged dasm + mixin]
    @TransformFromMethod(@MethodSig("blockChanged(Lnet/minecraft/core/BlockPos;)Z"))
    private native boolean cc_blockChanged(BlockPos pos);

    @Dynamic @Redirect(method = "cc_dasm$cc_blockChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelHeightAccessor;getSectionIndex(I)I"))
    private int cc_onBlockChanged_sectionIndex(LevelHeightAccessor instance, int y, BlockPos pos) {
        return Coords.sectionToIndex(Coords.blockToSection(pos.getX()), Coords.blockToSection(pos.getY()), Coords.blockToSection(pos.getZ()));
    }
    // endregion

    @Inject(method = "sectionLightChanged", at = @At("HEAD"), cancellable = true)
    public void cc_onSectionLightChanged(LightLayer lightLayer, int sectionY, CallbackInfoReturnable<Boolean> cir) {
        if (cc_cubePos != null) {
            cir.setReturnValue(false); // TODO (P2) lighting
        }
    }

    @Shadow public abstract void broadcastChanges(LevelChunk chunk);

    // region [cc_broadcastCubeChanges dasm + mixin]
    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkHolder.class), value = @MethodSig("broadcastChanges(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    public native void cc_broadcastCubeChanges(LevelCube cube);
    // TODO (P2) lighting - ClientboundLightUpdatePacket branch is currently never reached; once we have lighting it will have to be a CC packet, and this.broadcast will need to redirect to a CC method

    @Dynamic @Redirect(method = "cc_dasm$cc_broadcastCubeChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelHeightAccessor;getSectionYFromSectionIndex(I)I"))
    private int cc_onBroadcastCubeChanges_indexToSectionY(LevelHeightAccessor instance, int sectionIndex) {
        // The vanilla method uses SectionPos.of(ChunkPos, sectionY), but we want SectionPos.of(CubePos, sectionIndex).
        // The easiest way to accomplish this is to turn `getSectionYFromSectionIndex` into a no-op so that we get sectionIndex instead of sectionY.
        // (We could do local captures, but it'd be more brittle)
        return sectionIndex;
    }

    @Dynamic @Redirect(method = "cc_dasm$cc_broadcastCubeChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;of(Lio/github/opencubicchunks/cc_core/api/CubePos;I)Lnet/minecraft/core/SectionPos;"))
    private SectionPos cc_onBroadcastCubeChanges_sectionPos(CubePos cubePos, int sectionIndex) {
        return Coords.sectionPosByIndex(cubePos, sectionIndex);
    }
    // endregion

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("broadcastChanges(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    public void cc_broadcastCloChanges(LevelClo clo) {
        if (cc_cubePos != null) {
            cc_broadcastCubeChanges((LevelCube) clo);
        } else {
            broadcastChanges(((LevelChunk) clo));
        }
    }

    @WrapOperation(method = { "lambda$scheduleFullChunkPromotion$4", "demoteFullChunk" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;onFullChunkStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    private void cc_onCallChunkMapOnFullChunkStatusChange(ChunkMap instance, ChunkPos chunkPos, FullChunkStatus fullChunkStatus, Operation<Void> original) {
        if (cc_cubePos != null) {
            ((CubicChunkMap) instance).cc_onFullChunkStatusChange(cc_cubePos, fullChunkStatus);
        } else {
            original.call(instance, chunkPos, fullChunkStatus);
        }
    }

    @WrapOperation(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder$LevelChangeListener;onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
    protected void cc_onUpdateFutures_onCallOnLevelChange(ChunkHolder.LevelChangeListener instance, ChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer,
                                                          Operation<Void> original) {
        if (cc_cubePos != null) {
            cc_onLevelChange.onLevelChange(cc_cubePos, intSupplier, i, intConsumer);
        } else {
            original.call(instance, chunkPos, intSupplier, i, intConsumer);
        }
    }

    // TODO dasm inheritance
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("getLatestChunk()Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public CubeAccess cc_getLatestCube() {
        return super.cc_getLatestCube();
    }

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("getLatestChunk()Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public CloAccess cc_getLatestClo() {
        return super.cc_getLatestClo();
    }

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;"))
    @Override public CloPos cc_getCloPos() {
        return super.cc_getCloPos();
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("getChunkIfPresent(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public CubeAccess cc_getCubeIfPresent(ChunkStatus status) {
        return super.cc_getCubeIfPresent(status);
    }
}
