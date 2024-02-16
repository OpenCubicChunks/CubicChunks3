package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.mixin.GeneralSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CubicChunkProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Dasm(GeneralSet.class)
@Mixin(StoringChunkProgressListener.class)
public abstract class MixinStoringChunkProgressListener implements CubicChunkProgressListener {
    @Shadow @Final private LoggerChunkProgressListener delegate;
    @Shadow @Final private Long2ObjectOpenHashMap<ChunkStatus> statuses;
    @Shadow private ChunkPos spawnPos;
    @Shadow private boolean started;

    // TODO these two could be dasm?
    @AddMethodToSets(owner = @Ref(StoringChunkProgressListener.class), method = @MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"), sets = GeneralSet.class)
    @Override
    public void updateSpawnPos(CloPos pCenter) {
        if (this.started) {
            ((CubicChunkProgressListener) this.delegate).updateSpawnPos(pCenter);
            // FIXME we should have a CloPos spawnPos
            this.spawnPos = pCenter.correspondingChunkPos();
        }
    }

    @AddMethodToSets(owner = @Ref(StoringChunkProgressListener.class), method = @MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"), sets = GeneralSet.class)
    @Override
    public void onStatusChange(CloPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
        if (this.started) {
            ((CubicChunkProgressListener) this.delegate).onStatusChange(pChunkPosition, pNewStatus);
            if (pNewStatus == null) {
                this.statuses.remove(pChunkPosition.toLong());
            } else {
                this.statuses.put(pChunkPosition.toLong(), pNewStatus);
            }
        }
    }

    @Override @Shadow public abstract void start();

    @Override @Shadow public abstract void stop();
}
