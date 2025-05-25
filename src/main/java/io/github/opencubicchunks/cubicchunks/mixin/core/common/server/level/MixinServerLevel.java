package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;


import java.util.List;
import java.util.concurrent.Executor;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubicServerLevel;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements CubicServerLevel, MarkableAsCubic {
    protected boolean cc_isCubic;

    public MixinServerLevel() {
        super(null, null, null, null, null, false, false, 0, 0);
    }

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override
    public boolean cc_isCubic() {
        return cc_isCubic;
    }

    @Inject(method = "<init>", at = @At("io.github.opencubicchunks.cubicchunks.ConstructorSuper"))
    private void cc_onInit(MinecraftServer server, Executor dispatcher, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey dimension,
                           LevelStem levelStem, ChunkProgressListener progressListener, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime,
                           RandomSequences randomSequences, CallbackInfo ci) {
        // TODO conditionally mark as cubic based on dimension, config, level data, etc
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("startTickingChunk(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    public native void cc_startTickingClo(LevelClo chunk);

    // TODO: phase 3 - isNaturalSpawningAllowed

    // TODO: phase 3 - invalidateCapabilites, neoforge api

    // TODO: phase 2 - tickCube - new function

    // TODO: phase 4 - setCubeForced - new function

    // TODO: saveDebugReport - mixins, debug only, low priority, if we really really really really need it

    // TODO: phase 2 - isPositionEntityTicking - mixin

}