package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import static io.github.opencubicchunks.cc_core.utils.Utils.unsafeCast;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkStatus.class)
public class MixinChunkStatus {
    // TODO (P2) proper generation logic; this currently ignores everything and only handles promotion from ProtoClo to LevelClo
    public CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_generate(
        Executor exectutor,
        ServerLevel level,
        ChunkGenerator chunkGenerator,
        StructureTemplateManager structureTemplateManager,
        ThreadedLevelLightEngine lightEngine,
        Function<CloAccess, CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>>> task,
        List<CloAccess> cache
    ) {
        CloAccess chunkaccess = cache.get(cache.size() / 2);
        return ((Object) this == ChunkStatus.FULL ? task.apply(chunkaccess) : CompletableFuture.completedFuture(Either.left(chunkaccess)))
            .thenApply(
                p_281217_ -> {
                    p_281217_.ifLeft(p_290029_ -> {
                        if (p_290029_ instanceof ProtoClo protochunk && !protochunk.getStatus().isOrAfter((ChunkStatus) (Object) this)) {
                            protochunk.setStatus((ChunkStatus) (Object) this);
                        }
                    });

                    return unsafeCast(p_281217_);
                }
            );
    }
}
