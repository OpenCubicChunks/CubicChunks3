package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;


import java.util.List;
import java.util.concurrent.Executor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.MixinLevel;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubicServerLevel;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Dasm(ChunkToCloSet.class)
@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends MixinLevel implements CubicServerLevel {
    @Shadow @Final private ServerChunkCache chunkSource;

    @Inject(method = "<init>", at = @At("CTOR_HEAD"))
    private void cc_onInit(MinecraftServer server, Executor dispatcher, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey dimension,
                           LevelStem levelStem, ChunkProgressListener progressListener, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime,
                           RandomSequences randomSequences, CallbackInfo ci) {
        // TODO conditionally mark as cubic based on dimension, config, level data, etc
    }

    @Override public ServerCubeCache cc_getCubeSource() {
        return ((ServerCubeCache) this.chunkSource);
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("startTickingChunk(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    public native void cc_startTickingClo(LevelClo chunk);

    @WrapOperation(method = "setDefaultSpawnPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    private void cc_onSetDefaultSpawnPos_removeTicketWithRadius(ServerChunkCache instance, TicketType ticket, ChunkPos chunkPos, int radius, Operation<Void> original, BlockPos pos) {
        if (cc_isCubic) {
            ((ServerCubeCache) instance).cc_removeTicketWithRadius(ticket, CloPos.cube(pos), radius);
        } else {
            original.call(instance, ticket, chunkPos, radius);
        }
    }

    @WrapOperation(method = "setDefaultSpawnPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    private void cc_onSetDefaultSpawnPos_addicketWithRadius(ServerChunkCache instance, TicketType ticket, ChunkPos chunkPos, int radius, Operation<Void> original, BlockPos pos) {
        if (cc_isCubic) {
            ((ServerCubeCache) instance).cc_addTicketWithRadius(ticket, CloPos.cube(pos), radius);
        } else {
            original.call(instance, ticket, chunkPos, radius);
        }
    }

    // TODO: comments below don't account for 1.20.4->1.21.5 changes; will need to check for other methods that need CC changes

    // TODO: phase 3 - isNaturalSpawningAllowed

    // TODO: phase 3 - invalidateCapabilites, neoforge api

    // TODO: phase 2 - tickCube - new function

    // TODO: phase 4 - setCubeForced - new function

    // TODO: saveDebugReport - mixins, debug only, low priority, if we really really really really need it

    // TODO: phase 2 - isPositionEntityTicking - mixin

}