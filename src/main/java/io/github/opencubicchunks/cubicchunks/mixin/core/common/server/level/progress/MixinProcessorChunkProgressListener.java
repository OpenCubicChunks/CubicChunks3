package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(ChunkToCloSet.class)
@Mixin(ProcessorChunkProgressListener.class)
public abstract class MixinProcessorChunkProgressListener implements CloProgressListener {
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"))
    @Override public native void cc_updateSpawnPos(CloPos center);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"))
    @Override public native void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
}
