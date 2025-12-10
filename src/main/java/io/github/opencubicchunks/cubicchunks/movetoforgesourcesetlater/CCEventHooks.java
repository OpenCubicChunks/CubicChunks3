package io.github.opencubicchunks.cubicchunks.movetoforgesourcesetlater;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ForgeSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

@Dasm(ForgeSet.class)
public class CCEventHooks {
    private CCEventHooks() {
    }

    // TODO onCreateWorldSpawn?
    // TODO onTrySpawnPortal?

    @AddMethodToSets(containers = ForgeSet.EventHooks_to_CCEventHooks_redirects.class,
        method = "fireChunkTicketLevelUpdated(Lnet/minecraft/server/level/ServerLevel;JIILnet/minecraft/server/level/ChunkHolder;)V")
    public static void fireChunkTicketLevelUpdated(
        ServerLevel level, long cloPos, int oldTicketLevel, int newTicketLevel, @Nullable ChunkHolder chunkHolder
    ) {
        if (CloPos.isChunk(cloPos)) {
            EventHooks.fireChunkTicketLevelUpdated(level, cloPos, oldTicketLevel, newTicketLevel, chunkHolder);
        } else {
            // TODO (P4) CC event
        }
    }

    // TODO do we need a ChunkToCloForgeSet, etc? actually I guess if we only tell dasm about this class on forge then it's fine
    @AddMethodToSets(containers = ChunkToCloSet.EventHooks_to_CCEventHooks_redirects.class,
        method = "fireChunkWatch(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;"
            + "Lnet/minecraft/server/level/ServerLevel;)V")
    public static void fireChunkWatch(ServerPlayer entity, LevelClo clo, ServerLevel level) {
        if (clo instanceof LevelChunk chunk) {
            EventHooks.fireChunkWatch(entity, chunk, level);
        } else {
            // TODO (P4) CC event
        }
    }

    @AddMethodToSets(containers = ChunkToCloSet.EventHooks_to_CCEventHooks_redirects.class,
        method = "fireChunkSent(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;"
            + "Lnet/minecraft/server/level/ServerLevel;)V")
    public static void fireChunkSent(ServerPlayer entity, LevelClo clo, ServerLevel level) {
        if (clo instanceof LevelChunk chunk) {
            EventHooks.fireChunkSent(entity, chunk, level);
        } else {
            // TODO (P4) CC event
        }
    }

    @AddMethodToSets(containers = ChunkToCloSet.EventHooks_to_CCEventHooks_redirects.class,
        method = "fireChunkUnWatch(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;"
            + "Lnet/minecraft/server/level/ServerLevel;)V")
    public static void fireChunkUnWatch(ServerPlayer entity, CloPos cloPos, ServerLevel level) {
        if (cloPos.isChunk()) {
            EventHooks.fireChunkUnWatch(entity, cloPos.chunkPos(), level);
        } else {
            // TODO (P4) CC event
        }
    }
}
