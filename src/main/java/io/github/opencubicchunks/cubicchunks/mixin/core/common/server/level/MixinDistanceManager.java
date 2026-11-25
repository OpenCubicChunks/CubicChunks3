package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.ThrottlingChunkTaskDispatcherAccess;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.world.level.CubicTicketStorage;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.LoadingChunkTracker;
import net.minecraft.server.level.SimulationChunkTracker;
import net.minecraft.server.level.ThrottlingChunkTaskDispatcher;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@link DistanceManager} contains the main ticket hashmap and stores all the chunks that are loaded.
 * A ticket inside {@link DistanceManager} means that something is either requested to be loaded or is already loaded and needs to stay loaded.
 * It informs {@link ChunkMap} of what chunks it needs to generate/load/unload to satisfy the tickets.
 * <br>
 * <br>
 * This mixin mostly just replaces calls to ChunkPos with CloPos.
 */
@Dasm(value = ChunkToCloSet.class, target = @Ref(DistanceManager.class))
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager implements MarkableAsCubic {
    /**
     * Vanilla allows only 4 player tickets to be "in execution" at once; a slot is only freed once that position reaches entity ticking. Cubic worlds
     * have far more positions in view than vanilla (e.g. 13^3 cubes vs 25^2 chunks at view distance 12), and each cube depends on a 3D neighborhood,
     * so 4 slots leave the server and worldgen threads mostly idle while waiting. Positions are still taken nearest-first, so a larger limit doesn't
     * change load order.
     */
    private static final int CC_MAX_PLAYER_TICKETS_IN_EXECUTION = 64;

    protected boolean cc_isCubic;

    @Shadow @Final private LoadingChunkTracker loadingChunkTracker;
    @Shadow @Final private SimulationChunkTracker simulationChunkTracker;
    @Shadow @Final private DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter;
    @Shadow @Final private DistanceManager.PlayerTicketTracker playerTicketManager;
    @Shadow @Final ThrottlingChunkTaskDispatcher ticketDispatcher;

    @Override public void cc_setCubic() {
        cc_isCubic = true;
        ((ThrottlingChunkTaskDispatcherAccess) this.ticketDispatcher).cc_setMaxChunksInExecution(CC_MAX_PLAYER_TICKETS_IN_EXECUTION);
        ((MarkableAsCubic) this.loadingChunkTracker).cc_setCubic();
        ((MarkableAsCubic) this.simulationChunkTracker).cc_setCubic();
        ((MarkableAsCubic) this.naturalSpawnChunkCounter).cc_setCubic();
        ((MarkableAsCubic) this.playerTicketManager).cc_setCubic();
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
    }

    /**
     * This function replaces the addTicket call with a cubic version instead.
     * This requires replacing the ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/TicketStorage;addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    private boolean cc_replaceTicketTypeOnAddPlayer(TicketStorage instance, Ticket ticket, ChunkPos chunkPos, SectionPos sectionPos) {
        if (!cc_isCubic) {
            return true;
        }
        CloPos cloPos = CloPos.section(sectionPos);
        ((CubicTicketStorage) instance).cc_addTicket(ticket, cloPos);
        return false;
    }

    /**
     * This function replaces the removeTicket call with a cubic version instead.
     * This requires replacing ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/TicketStorage;removeTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    private boolean cc_replaceTicketTypeOnRemovePlayer(TicketStorage instance, Ticket ticket, ChunkPos chunkPos, SectionPos sectionPos) {
        if (!cc_isCubic) {
            return true;
        }
        CloPos cloPos = CloPos.section(sectionPos);
        ((CubicTicketStorage) instance).cc_removeTicket(ticket, cloPos);
        return false;
    }

    /**
     * The original function expects chunkPos.toLong(), but we need to replace it with cloPos.toLong() instead.
     */
    @WrapOperation(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    private long cc_replaceTicketTypeOnAddPlayer(ChunkPos chunkPos, Operation<Long> original, SectionPos sectionPos) {
        if (!cc_isCubic) {
            return original.call(chunkPos);
        }
        return CloPos.section(sectionPos).toLong();
    }

    /**
     * The original function expects chunkPos.toLong(), but we need to replace it with cloPos.toLong() instead.
     */
    @WrapOperation(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    private long cc_replaceTicketTypeOnRemovePlayer(ChunkPos chunkPos, Operation<Long> original, SectionPos sectionPos) {
        if (!cc_isCubic) {
            return original.call(chunkPos);
        }
        return CloPos.section(sectionPos).toLong();
    }

    // TODO how does hasPlayersNearby work?
}
