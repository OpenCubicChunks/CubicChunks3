package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.CubicChunkHolder;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CubicChunkProgressListener;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Generally should not be used directly for DASM transforms; prefer using {@link GeneralSet} instead.
 * <br/><br/>
 * Redirects should be added to this set rather than {@link GeneralSet}, except when they cause issues with other sets that inherit from {@link GlobalSet} - for example constructor to factory redirects on ChunkAccess subclasses.
 */
@RedirectSet
public interface GlobalSet extends ForgeSet {
    @TypeRedirect(from = @Ref(ChunkPos.class), to = @Ref(CloPos.class))
    abstract class ChunkPos_to_CloPos_redirects {
        @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "x"))
        native int getX();

        @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "z"))
        native int getZ();

        // Note that this relies on ChunkPos and CloPos encoding to longs in the same way
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(long.class) }))
        static native CloPos fromLong(long cloPos);

        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(int.class), @Ref(int.class) }))
        static native CloPos chunk(int x, int z);
    }

    @TypeRedirect(from = @Ref(ChunkHolder.LevelChangeListener.class), to = @Ref(CubicChunkHolder.LevelChangeListener.class))
    abstract class LevelChangeListenerChunkHolder_to_CubicChunkHolder_redirects { }

    @TypeRedirect(from = @Ref(ChunkHolder.PlayerProvider.class), to = @Ref(CubicChunkHolder.PlayerProvider.class))
    abstract class PlayerProviderChunkHolder_to_CubicChunkHolder_redirects { }

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
        public abstract CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_generate(
            Executor pExectutor,
            ServerLevel pLevel,
            ChunkGenerator pChunkGenerator,
            StructureTemplateManager pStructureTemplateManager,
            ThreadedLevelLightEngine pLightEngine,
            Function<CloAccess, CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>>> pTask,
            List<CloAccess> pCache
        );
    }

    @TypeRedirect(from = @Ref(ChunkProgressListener.class), to = @Ref(CubicChunkProgressListener.class))
    interface ChunkProgressListener_to_CubicChunkProgressListener_redirects {
        @MethodRedirect(@MethodSig("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V"))
        void cc_updateSpawnPos(CloPos pCenter);

        @MethodRedirect(@MethodSig("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkStatus;)V"))
        void cc_onStatusChange(CloPos pChunkPosition, @Nullable ChunkStatus pNewStatus);
    }

    @TypeRedirect(from = @Ref(ChunkTrackingView.class), to = @Ref(CloTrackingView.class))
    interface ChunkTrackingView_to_CloTrackingView_redirects { }

    @TypeRedirect(from = @Ref(ChunkTrackingView.Positioned.class), to = @Ref(CloTrackingView.Positioned.class))
    interface ChunkTrackingView$Positioned_to_CloTrackingView$Positioned_redirects { }
}
