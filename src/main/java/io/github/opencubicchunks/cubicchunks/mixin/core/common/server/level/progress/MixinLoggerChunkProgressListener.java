package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(value = ChunkToCloSet.class, target = @Ref(LoggerChunkProgressListener.class))
@Mixin(LoggerChunkProgressListener.class)
public abstract class MixinLoggerChunkProgressListener implements CloProgressListener {
    @AddTransformToSets(ChunkToCloSet.LoggerChunkProgressListener_redirects.class)
    @TransformFromMethod("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V")
    @Override public native void cc_updateSpawnPos(CloPos center);

    @AddTransformToSets(ChunkToCloSet.LoggerChunkProgressListener_redirects.class)
    @TransformFromMethod("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V")
    @Override public native void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
}
