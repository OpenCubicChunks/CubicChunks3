package io.github.opencubicchunks.cubicchunks.mixin.core.common.chunk;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cc_core.world.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.ProtoCube;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.chunk.ChunkStatus$SimpleGenerationTask")
public interface MixinChunkStatusSimpleGenerationTask {

    @Shadow void doWork(ChunkStatus status, ServerLevel level, ChunkGenerator generator, List<ChunkAccess> neighbors, ChunkAccess chunk);

    /**
     * @author Batrteks2x
     * @reason inject is not supported, SectionPrimer check
     */
    @Overwrite
    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
        ChunkStatus status, Executor executor, ServerLevel level, ChunkGenerator generator, StructureManager structureManager, ThreadedLevelLightEngine threadedLevelLightEngine,
        Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> neighbors, ChunkAccess chunk, boolean bl) {

        if (!((CubicLevelHeightAccessor) chunk).isCubic()) {
            if (!chunk.getStatus().isOrAfter(status)) {
                this.doWork(status, level, generator, neighbors, chunk);
                if (chunk instanceof ProtoChunk) {
                    ((ProtoChunk) chunk).setStatus(status);
                }
            }
            return CompletableFuture.completedFuture(Either.left(chunk));
        }

        if (!chunk.getStatus().isOrAfter(status)) {
            this.doWork(status, level, generator, neighbors, chunk);
            if (chunk instanceof ProtoChunk) {
                ((ProtoChunk) chunk).setStatus(status);
            } else if (chunk instanceof ProtoCube) {
                ((ProtoCube) chunk).updateCubeStatus(status);
            }
        }
        return CompletableFuture.completedFuture(Either.left(chunk));
    }
}
