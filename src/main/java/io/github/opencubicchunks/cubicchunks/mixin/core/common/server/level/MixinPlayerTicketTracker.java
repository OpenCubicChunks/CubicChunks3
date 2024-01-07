package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.DistanceManagerAccess;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTaskPriorityQueueSorter;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DistanceManager.PlayerTicketTracker.class)
public abstract class MixinPlayerTicketTracker extends MixinFixedPlayerDistanceChunkTracker {
    @SuppressWarnings("target") @Shadow @Final DistanceManager this$0;
    @WrapOperation(method = "onLevelChange(JIZZ)V", at = @At(value = "NEW",
        target = "(Lnet/minecraft/server/level/TicketType;ILjava/lang/Object;)Lnet/minecraft/server/level/Ticket;"))
    private Ticket<?> cc_onTicketConstruct(TicketType<?> ttype, int a, Object pos, Operation<Ticket> original) {
        if (!cc_isCubic)
            return original.call(ttype, a, pos);
        return original.call(ttype, a, CloPos.fromLong(((ChunkPos) pos).toLong()));
    }
    @WrapWithCondition(method = "runAllUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTaskPriorityQueueSorter;onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
    private boolean cc_onRunAllUpdates(ChunkTaskPriorityQueueSorter instance, ChunkPos p_140616_, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_) {
        if(!cc_isCubic) return true;
        ((CubicTaskPriorityQueueSorter)((DistanceManagerAccess)this$0).ticketThrottler())
            .onLevelChange(CloPos.fromLong(p_140616_.toLong()), p_140617_, p_140618_, p_140619_);
        return false;
    }
}
