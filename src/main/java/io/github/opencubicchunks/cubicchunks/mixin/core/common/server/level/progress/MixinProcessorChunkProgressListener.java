package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.mixin.GeneralSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CubicChunkProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Dasm(GeneralSet.class)
@Mixin(ProcessorChunkProgressListener.class)
public abstract class MixinProcessorChunkProgressListener implements CubicChunkProgressListener {
    @Shadow @Final private ProcessorMailbox<Runnable> mailbox;
    @Shadow @Final private ChunkProgressListener delegate;

    // TODO these two could be dasm?
    @AddMethodToSets(owner = @Ref(ProcessorChunkProgressListener.class), method = @MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"), sets = GeneralSet.class)
    @Override
    public void updateSpawnPos(CloPos pCenter) {
        this.mailbox.tell(() -> ((CubicChunkProgressListener) this.delegate).updateSpawnPos(pCenter));
    }

    @AddMethodToSets(owner = @Ref(ProcessorChunkProgressListener.class), method = @MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"), sets = GeneralSet.class)
    @Override
    public void onStatusChange(CloPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
        this.mailbox.tell(() -> ((CubicChunkProgressListener) this.delegate).onStatusChange(pChunkPosition, pNewStatus));
    }

    @Override @Shadow public abstract void start();

    @Override @Shadow public abstract void stop();
}
