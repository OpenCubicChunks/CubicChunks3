package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTickingTracker;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link TickingTracker} is a class that manages a Long2ByteMap {@link TickingTracker#chunks} that contains the list of chunks that are ticking.
 * This is for the purposes of separating simulation distance from render distance.
 * <br><br>
 * {@link TickingTracker#chunks} is indexed by ChunkPos, and the value is the level of the chunk.
 * The meaning of the ChunkLevel is shown by {@link net.minecraft.server.level.ChunkLevel#byStatus(FullChunkStatus)}.
 * The ByteMap only contains values that are below FULL status (level 33), since those are the only ones that need ticking.
 * <br><br>
 * {@link TickingTracker} also contains a Long2ObjectOpenHashMap {@link TickingTracker#tickets} which contains the actual list of tickets.
 * The values from it get propagated to {@link TickingTracker#chunks} in {@link net.minecraft.server.level.ChunkTracker#computeLevelFromNeighbor}.
 * <br><br>
 * This mixin is used to convert {@link TickingTracker} to use {@link CloPos} instead of {@link ChunkPos}.
 */
@Dasm(ChunkToCloSet.class)
@Mixin(TickingTracker.class)
public abstract class MixinTickingTracker extends MixinChunkTracker implements CubicTickingTracker {
    @Inject(method = "setLevel", at = @At("HEAD"))
    private void cc_onSetLevel(long chunkPos, int level, CallbackInfo ci) {
        super.cc_onSetLevel(chunkPos, level);
    }

    @Inject(method = "getLevel(Lnet/minecraft/world/level/ChunkPos;)I", at = @At("HEAD"))
    private void cc_onChunkGetLevel(ChunkPos chunkPos, CallbackInfoReturnable<Integer> cir) {
        assert !cc_isCubic;
    }

    /**
     * This mixin reverts the creation of a {@link ChunkPos}, then reads the {@link CloPos} from the raw long.
     * We know if we are a Chunk or Cube based on {@link CloPos#CLO_Y_COLUMN_INDICATOR}, so there is no ambiguity here.
     */
    @WrapWithCondition(method = "replacePlayerTicketsLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/TickingTracker;addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_onReplacePlayerTicketsLevel(TickingTracker instance, TicketType type, ChunkPos chunkPos, int ticketLevel, T key) {
        if (!cc_isCubic) return true;
        // if isCubic then we expect tickets to be TicketType not TicketType
        var cloPos = CloPos.fromLong(chunkPos.toLong());
        this.cc_addTicket((TicketType) type, cloPos, ticketLevel, cloPos);
        return false;
    }

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_addTicket(TicketType type, CloPos cloPos, int ticketLevel, T key);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_removeTicket(TicketType type, CloPos cloPos, int ticketLevel, T key);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("getLevel(Lnet/minecraft/world/level/ChunkPos;)I"))
    public abstract int cc_getLevel(CloPos cloPos);
}
