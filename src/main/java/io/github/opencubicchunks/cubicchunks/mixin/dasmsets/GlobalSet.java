package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.entity.CloStatusUpdateListener;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Contains redirects that are applied to all DASM transforms. <br/> <br/> Redirects should only be added to this set if they are applicable in all
 * contexts.
 */
@RedirectSet
public interface GlobalSet extends ForgeSet {
    @IntraOwnerContainer(@Ref(ChunkStatus.class))
    abstract class ChunkStatus_redirects {
        @MethodRedirect(
            "generate(Ljava/util/concurrent/Executor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;"
                + "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;"
                + "Lnet/minecraft/server/level/ThreadedLevelLightEngine;Ljava/util/function/Function;Ljava/util/List;)"
                + "Ljava/util/concurrent/CompletableFuture;")
        public abstract CompletableFuture<ChunkResult<CloAccess>> cc_generate(
            Executor exectutor, ServerLevel level, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager,
            ThreadedLevelLightEngine lightEngine, Function<CloAccess, CompletableFuture<ChunkResult<CloAccess>>> task, List<CloAccess> cache
        );
    }

    @TypeRedirect(from = @Ref(ChunkProgressListener.class), to = @Ref(CloProgressListener.class))
    interface ChunkProgressListener_to_CloProgressListener_redirects {
        @MethodRedirect("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V")
        void cc_updateSpawnPos(CloPos center);

        @MethodRedirect("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V")
        void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
    }

    @TypeRedirect(from = @Ref(ChunkStatusUpdateListener.class), to = @Ref(CloStatusUpdateListener.class))
    interface ChunkStatusUpdateListener_to_CloStatusUpdateListener_redirects {
    }

    @TypeRedirect(from = @Ref(ChunkTrackingView.class), to = @Ref(CloTrackingView.class))
    interface ChunkTrackingView_to_CloTrackingView_redirects {
    }

    @TypeRedirect(from = @Ref(ChunkTrackingView.Positioned.class), to = @Ref(CloTrackingView.Positioned.class))
    abstract class ChunkTrackingView$Positioned_to_CloTrackingView$Positioned_redirects {
    }

    @IntraOwnerContainer(@Ref(StoringChunkProgressListener.class))
    class StoringChunkProgressListener_redirects {
    }

    @IntraOwnerContainer(@Ref(ChunkMap.class))
    class ChunkMap_redirects {
    }

    @IntraOwnerContainer(@Ref(ServerChunkCache.class))
    class ServerChunkCache_redirects {
    }
}
