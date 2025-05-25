package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Dasm(ChunkToCloSet.class)
@Mixin(ProcessorChunkProgressListener.class)
public abstract class MixinProcessorChunkProgressListener implements CloProgressListener {
    // We need a field referencing the delegate as a CloProgressListener, otherwise we end up trying to access a field of the wrong type
    @AddFieldToSets(sets = ChunkToCloSet.class, owner = @Ref(ProcessorChunkProgressListener.class), field = @FieldSig(type = @Ref(ChunkProgressListener.class), name = "delegate")) private CloProgressListener cc_delegate;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cc_onInit(ChunkProgressListener delegate, Executor dispatcher, CallbackInfo ci) {
        cc_delegate = ((CloProgressListener) delegate);
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"))
    @Override public native void cc_updateSpawnPos(CloPos center);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"))
    @Override public native void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
}
