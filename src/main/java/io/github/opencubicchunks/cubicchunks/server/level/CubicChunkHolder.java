package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface CubicChunkHolder {
    CloPos cc_getPos();

    @Nullable LevelClo cc_getTickingChunk();

    void cc_broadcastChanges(LevelClo clo);

    CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_getOrScheduleFuture(ChunkStatus status, ChunkMap map);

    void cc_addSaveDependency(String source, CompletableFuture<?> future);

    @FunctionalInterface
    interface LevelChangeListener {
        void cc_onLevelChange(CloPos cloPos, IntSupplier p_140120_, int p_140121_, IntConsumer p_140122_);
    }

    interface PlayerProvider {
        /**
         * Returns the players tracking the given chunk.
         */
        List<ServerPlayer> getPlayers(CloPos pos, boolean boundaryOnly);
    }
}
