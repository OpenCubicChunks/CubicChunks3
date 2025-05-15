package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

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
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(ChunkToCloSet.class)
@Mixin(StoringChunkProgressListener.class)
public abstract class MixinStoringChunkProgressListener implements CloProgressListener {
    @AddFieldToSets(sets = GlobalSet.class, owner = @Ref(StoringChunkProgressListener.class), field = @FieldSig(type = @Ref(ChunkPos.class), name = "spawnPos"))
    private CloPos cc_spawnPos;

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"))
    @Override public native void cc_updateSpawnPos(CloPos center);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"))
    @Override public native void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
}
