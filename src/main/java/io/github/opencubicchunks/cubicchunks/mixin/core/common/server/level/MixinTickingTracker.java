package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cubicchunks.mixin.DasmRedirect;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTickingTracker;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
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
@DasmRedirect()
@Mixin(TickingTracker.class)
public abstract class MixinTickingTracker extends MixinChunkTracker implements CubicTickingTracker {
    @Inject(method = "setLevel", at = @At("HEAD"))
    private void cc_onSetLevel(long pos, int level, CallbackInfo ci) {
        super.cc_onSetLevel(pos, level);
    }

    @Inject(method = "getLevel(Lnet/minecraft/world/level/ChunkPos;)I", at = @At("HEAD"))
    private void cc_onChunkGetLevel(ChunkPos p_184162_, CallbackInfoReturnable<Integer> cir) {
        assert !cc_isCubic;
    }

    /**
     * This mixin reverts the creation of a {@link ChunkPos}, then reads the {@link CloPos} from the raw long.
     * We know if we are a Chunk or Cube based on {@link CloPos#CLO_Y_COLUMN_INDICATOR}, so there is no ambiguity here.
     */
    @WrapWithCondition(method = "replacePlayerTicketsLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/TickingTracker;addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_onReplacePlayerTicketsLevel(TickingTracker instance, TicketType<T> ticketType, ChunkPos pos, int a, T b) {
        if (!cc_isCubic) return true;
        // if isCubic then we expect tickets to be TicketType<CloPos> not TicketType<ChunkPos>
        var cloPos = CloPos.fromLong(pos.toLong());
        this.addTicket((TicketType<CloPos>) ticketType, cloPos, a, cloPos);
        return false;
    }

    /**
     * We need to replace the reference to {@link TicketType#PLAYER} with {@link CubicTicketType#PLAYER} in {@link TickingTracker#replacePlayerTicketsLevel(int)}.
     */
    @WrapOperation(method = "replacePlayerTicketsLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/TicketType;PLAYER:Lnet/minecraft/server/level/TicketType;"))
    private TicketType<?> cc_replaceTicketType(Operation<TicketType<ChunkPos>> original) {
        if(!cc_isCubic) return original.call();
        return CubicTicketType.PLAYER;
    }

    @TransformFrom("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    public abstract <T> void addTicket(TicketType<T> p_184155_, CloPos p_184156_, int p_184157_, T p_184158_);

    @TransformFrom("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    public abstract <T> void removeTicket(TicketType<T> p_184169_, CloPos p_184170_, int p_184171_, T p_184172_);

    @TransformFrom("getLevel(Lnet/minecraft/world/level/ChunkPos;)I")
    public abstract int getLevel(CloPos p_184162_);
}
