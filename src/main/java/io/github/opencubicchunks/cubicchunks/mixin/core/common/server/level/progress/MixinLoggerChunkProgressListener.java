package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.mixin.GeneralSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CubicChunkProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Dasm(GeneralSet.class)
@Mixin(LoggerChunkProgressListener.class)
public abstract class MixinLoggerChunkProgressListener implements CubicChunkProgressListener {
    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"))
    @Override public native void updateSpawnPos(CloPos pCenter);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(@MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"))
    @Override public native void onStatusChange(CloPos pChunkPosition, @Nullable ChunkStatus pNewStatus);

    @Override @Shadow public abstract void start();

    @Override @Shadow public abstract void stop();
}
