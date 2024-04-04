package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkMapTestAccess {
    @Accessor("visibleChunkMap") Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap();

    @Invoker @Nullable ChunkHolder invokeUpdateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);

    @Invoker CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> invokeGetChunkRangeFuture(ChunkHolder chunkHolder, int range, IntFunction<ChunkStatus> statusGetter);

    @Invoker ChunkStatus invokeGetDependencyStatus(ChunkStatus chunkStatus, int p_140264_);

    @Dynamic @Invoker @Nullable ChunkHolder invokeCc_UpdateChunkScheduling(long cloPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);

    @Dynamic @Invoker CompletableFuture<Either<List<CloAccess>, ChunkHolder.ChunkLoadingFailure>> invokeCc_GetChunkRangeFuture(ChunkHolder chunkHolder, int range, IntFunction<ChunkStatus> statusGetter);
}
