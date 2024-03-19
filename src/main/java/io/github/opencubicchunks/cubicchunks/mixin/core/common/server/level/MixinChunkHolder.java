package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GeneralSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkHolder;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ImposterProtoClo;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The vanilla {@link ChunkHolder} class wraps completable futures for different statuses (load levels) of a single chunk and handles logic for loading/unloading that chunk, as well as broadcasting updates to clients.
 * This mixin adds cubic chunks equivalents for methods where necessary, to allow ChunkHolder to dynamically wrap either a chunk or a cube (i.e. a CLO).
 */
@Dasm(GeneralSet.class)
@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder implements CubicChunkHolder {
    private boolean cc_isCubic;

    @AddFieldToSets(sets = GeneralSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(name = "pos", type = @Ref(ChunkPos.class)))
    private CloPos cc_cloPos;

    @AddFieldToSets(sets = GeneralSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(name = "onLevelChange", type = @Ref(ChunkHolder.LevelChangeListener.class)))
    private final CubicChunkHolder.LevelChangeListener cc_onLevelChange;
    @AddFieldToSets(sets = GeneralSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(name = "playerProvider", type = @Ref(ChunkHolder.PlayerProvider.class)))
    private final CubicChunkHolder.PlayerProvider cc_playerProvider;

    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;"))
    @Override public CloPos cc_getPos() {
        return cc_cloPos;
    }

    @Shadow private boolean hasChangedSections;
    @Shadow @Final private final ShortSet[] changedBlocksPerSection;

    @Shadow protected abstract void broadcastBlockEntityIfNeeded(List<ServerPlayer> pPlayers, Level pLevel, BlockPos pPos, BlockState pState);

    @Shadow protected abstract void broadcast(List<ServerPlayer> pPlayers, Packet<?> pPacket);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("<init>(Lnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/lighting/LevelLightEngine;"
            + "Lnet/minecraft/server/level/ChunkHolder$LevelChangeListener;Lnet/minecraft/server/level/ChunkHolder$PlayerProvider;)V"))
    public MixinChunkHolder() {
        throw new IllegalStateException("dasm failed to apply");
    }

    @Dynamic @Inject(at = @At("RETURN"), method = "cc_dasm$__init__(Lio/github/opencubicchunks/cubicchunks/world/level/chunklike/CloPos;ILnet/minecraft/world/level/LevelHeightAccessor;"
        + "Lnet/minecraft/world/level/lighting/LevelLightEngine;Lio/github/opencubicchunks/cubicchunks/server/level/CubicChunkHolder$LevelChangeListener;"
        + "Lio/github/opencubicchunks/cubicchunks/server/level/CubicChunkHolder$PlayerProvider;)V")
    private void onCcInit(CallbackInfo ci) {
        // TODO redirect changedBlocksPerSection construction for chunks
        cc_isCubic = true;
    }

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("getTickingChunk()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public native LevelClo cc_getTickingChunk();

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("getChunkToSend()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public native LevelClo cc_getChunkToSend();

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("getFullChunk()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    @Nullable public native LevelClo cc_getFullChunk();

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("getLastAvailable()Lnet/minecraft/world/level/chunk/ChunkAccess;"))
    @Nullable public native CloAccess cc_getLastAvailable();

    // dasm + mixin
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("blockChanged(Lnet/minecraft/core/BlockPos;)V"))
    public native void cc_blockChanged(BlockPos pos);

    /**
     * Only handle block changes for cubes, as this should not be tracked on columns
     */
    @Dynamic @Inject(method = "cc_dasm$cc_blockChanged",
        at = @At(value = "INVOKE_ASSIGN", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/level/ChunkHolder;cc_getTickingChunk()Lio/github/opencubicchunks/cubicchunks/world/level/chunklike/LevelClo;"
        ),
        cancellable = true)
    private void cc_blockChanged_checkCubic(BlockPos pos, CallbackInfo ci, @Local LevelClo clo) {
        if (clo instanceof ChunkAccess) ci.cancel();
    }

    /**
     * Redirect to use cube section indexing instead of chunk section indexing
     */
    // This can't be done using purely dasm because the original getSectionIndex call takes a y coordinate, whereas we need the full blockpos.
    @Dynamic @Redirect(method = "cc_dasm$cc_blockChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelHeightAccessor;getSectionIndex(I)I"))
    private int cc_blockChanged_sectionIndex(LevelHeightAccessor instance, int y, BlockPos pos) {
        return Coords.blockToIndex(pos);
    }

    // We want a different signature (see below); can't automatically redirect this one
    @Inject(method = "sectionLightChanged", at = @At("HEAD"))
    private void cc_onSectionLightChanged(LightLayer pType, int pSectionY, CallbackInfo ci) {
        // We should be calling the cubic signature instead
        assert !cc_isCubic;
    }

    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("sectionLightChanged(Lnet/minecraft/world/level/LightLayer;I)V"))
    public void cc_sectionLightChanged(LightLayer pType, SectionPos pos) {
        // TODO (P2) lighting
    }

    @AddMethodToSets(sets = GeneralSet.class, owner = @Ref(ChunkHolder.class), method = @MethodSig("broadcastChanges(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    public void cc_broadcastChanges(LevelClo clo) {
        // TODO (P2) also handle lighting - see vanilla method
        // TODO seems like this should only run for cubes; is that correct?
        if (this.hasChangedSections && clo instanceof LevelCube cube) {
            Level level = cube.getLevel();

            List<ServerPlayer> list1 = this.cc_playerProvider.getPlayers(this.cc_cloPos, false);

            for(int j = 0; j < this.changedBlocksPerSection.length; ++j) {
                ShortSet shortset = this.changedBlocksPerSection[j];
                if (shortset != null) {
                    this.changedBlocksPerSection[j] = null;
                    if (!list1.isEmpty()) {
                        SectionPos sectionpos = Coords.sectionPosByIndex(cube.cc_getCloPos().cubePos(), j);
                        if (shortset.size() == 1) {
                            BlockPos blockpos = sectionpos.relativeToBlockPos(shortset.iterator().nextShort());
                            BlockState blockstate = level.getBlockState(blockpos);
                            this.broadcast(list1, new ClientboundBlockUpdatePacket(blockpos, blockstate));
                            this.broadcastBlockEntityIfNeeded(list1, level, blockpos, blockstate);
                        } else {
                            LevelChunkSection levelchunksection = cube.getSection(j);
                            ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket = new ClientboundSectionBlocksUpdatePacket(
                                sectionpos, shortset, levelchunksection
                            );
                            this.broadcast(list1, clientboundsectionblocksupdatepacket);
                            clientboundsectionblocksupdatepacket.runUpdates(
                                (p_288761_, p_288762_) -> this.broadcastBlockEntityIfNeeded(list1, level, p_288761_, p_288762_)
                            );
                        }
                    }
                }
            }

            this.hasChangedSections = false;
        }
    }

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("getOrScheduleFuture(Lnet/minecraft/world/level/chunk/ChunkStatus;Lnet/minecraft/server/level/ChunkMap;)Ljava/util/concurrent/CompletableFuture;"))
    @Override public native CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_getOrScheduleFuture(ChunkStatus status, ChunkMap map);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("addSaveDependency(Ljava/lang/String;Ljava/util/concurrent/CompletableFuture;)V"))
    public native void cc_addSaveDependency(String source, CompletableFuture<?> future);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("updateChunkToSave(Ljava/util/concurrent/CompletableFuture;Ljava/lang/String;)V"))
    private native void cc_updateChunkToSave(CompletableFuture<? extends Either<? extends CloAccess, ChunkHolder.ChunkLoadingFailure>> future, String source);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("scheduleFullChunkPromotion(Lnet/minecraft/server/level/ChunkMap;Ljava/util/concurrent/CompletableFuture;Ljava/util/concurrent/Executor;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    private native void cc_scheduleFullChunkPromotion(
        ChunkMap chunkMap, CompletableFuture<Either<LevelClo, ChunkHolder.ChunkLoadingFailure>> future, Executor executor, FullChunkStatus fullChunkStatus
    );

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("demoteFullChunk(Lnet/minecraft/server/level/ChunkMap;Lnet/minecraft/server/level/FullChunkStatus;)V"))
    private native void cc_demoteFullChunk(ChunkMap chunkMap, FullChunkStatus fullChunkStatus);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("updateFutures(Lnet/minecraft/server/level/ChunkMap;Ljava/util/concurrent/Executor;)V"))
    protected native void cc_updateFutures(ChunkMap chunkMap, Executor executor);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        value = @MethodSig("replaceProtoChunk(Lnet/minecraft/world/level/chunk/ImposterProtoChunk;)V"))
    public native void cc_replaceProtoChunk(ImposterProtoClo imposter);

}
