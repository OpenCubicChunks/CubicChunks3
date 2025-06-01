package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.server.level.CloHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.entity.CloStatusUpdateListener;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Contains redirects that are applied to all DASM transforms.
 * <br/><br/>
 * Redirects should only be added to this set if they are applicable in all contexts.
 */
@RedirectSet
public interface GlobalSet extends ForgeSet {
    @TypeRedirect(from = @Ref(ChunkHolder.LevelChangeListener.class), to = @Ref(CloHolder.LevelChangeListener.class))
    interface LevelChangeListenerChunkHolder_to_CloHolder_redirects {
        @MethodRedirect(@MethodSig("onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
        void cc_onLevelChange(CloPos cloPos, IntSupplier p_140120_, int p_140121_, IntConsumer p_140122_);
    }

    @TypeRedirect(from = @Ref(ChunkHolder.PlayerProvider.class), to = @Ref(CloHolder.PlayerProvider.class))
    interface PlayerProviderChunkHolder_to_CloHolder_redirects { }

    @InterOwnerContainer(owner = @Ref(TicketType.class), newOwner = @Ref(CubicTicketType.class))
    abstract class ChunkTicketType_to_CloTicketType_redirects {
        @FieldRedirect(@FieldSig(type = @Ref(TicketType.class), name = "PLAYER"))
        public static TicketType<CloPos> PLAYER;
        @FieldRedirect(@FieldSig(type = @Ref(TicketType.class), name = "FORCED"))
        public static TicketType<CloPos> FORCED;
        @FieldRedirect(@FieldSig(type = @Ref(TicketType.class), name = "LIGHT"))
        public static TicketType<CloPos> LIGHT;
        @FieldRedirect(@FieldSig(type = @Ref(TicketType.class), name = "UNKNOWN"))
        public static TicketType<CloPos> UNKNOWN;
    }

    @IntraOwnerContainer(owner = @Ref(ChunkStatus.class))
    abstract class ChunkStatus_redirects {
        @MethodRedirect(@MethodSig("generate(Ljava/util/concurrent/Executor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Lnet/minecraft/server/level/ThreadedLevelLightEngine;Ljava/util/function/Function;Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"))
        public abstract CompletableFuture<ChunkResult<CloAccess>> cc_generate(
            Executor exectutor,
            ServerLevel level,
            ChunkGenerator chunkGenerator,
            StructureTemplateManager structureTemplateManager,
            ThreadedLevelLightEngine lightEngine,
            Function<CloAccess, CompletableFuture<ChunkResult<CloAccess>>> task,
            List<CloAccess> cache
        );
    }

    @TypeRedirect(from = @Ref(ChunkProgressListener.class), to = @Ref(CloProgressListener.class))
    interface ChunkProgressListener_to_CloProgressListener_redirects {
        @MethodRedirect(@MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"))
        void cc_updateSpawnPos(CloPos center);

        @MethodRedirect(@MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V"))
        void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);
    }

    @TypeRedirect(from = @Ref(ChunkStatusUpdateListener.class), to = @Ref(CloStatusUpdateListener.class))
    interface ChunkStatusUpdateListener_to_CloStatusUpdateListener_redirects { }

    @TypeRedirect(from = @Ref(ChunkTrackingView.class), to = @Ref(CloTrackingView.class))
    interface ChunkTrackingView_to_CloTrackingView_redirects { }

    @TypeRedirect(from = @Ref(ChunkTrackingView.Positioned.class), to = @Ref(CloTrackingView.Positioned.class))
    abstract class ChunkTrackingView$Positioned_to_CloTrackingView$Positioned_redirects { }

    // TODO These need to be specified explicitly for now bc of inheritance jank
    @IntraOwnerContainer(owner = @Ref(ChunkMap.DistanceManager.class))
    abstract class ChunkMap$DistanceManager_redirects {
        @MethodRedirect(@MethodSig("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
        public abstract <T> void cc_addTicket(TicketType<T> type, CloPos pos, int level, T value);

        @MethodRedirect(@MethodSig("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
        public abstract <T> void cc_removeTicket(TicketType<T> type, CloPos pos, int level, T value);

        @MethodRedirect(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
        public abstract <T> void cc_addRegionTicket(TicketType<T> type, CloPos pos, int distance, T value);

        @MethodRedirect(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
        public abstract <T> void cc_addRegionTicket(TicketType<T> type, CloPos pos, int distance, T value, boolean forceTicks);

        @MethodRedirect(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
        public abstract <T> void cc_removeRegionTicket(TicketType<T> type, CloPos pos, int distance, T value);

        @MethodRedirect(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
        public abstract <T> void cc_removeRegionTicket(TicketType<T> type, CloPos pos, int distance, T value, boolean forceTicks);

        @MethodRedirect(@MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)V"))
        protected abstract void updateCubeForced(CloPos pos, boolean add);
    }
}
