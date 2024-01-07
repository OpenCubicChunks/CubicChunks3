package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.mixin.DasmRedirect;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.server.level.CubicDistanceManager;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@DasmRedirect()
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager implements CubicDistanceManager {
    protected boolean cc_isCubic;

    @Override
    @UsedFromASM
    @TransformFrom("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    public abstract <T> void addTicket(TicketType<T> p_140793_, CloPos p_140794_, int p_140795_, T p_140796_);

    @Override
    @UsedFromASM
    @TransformFrom("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    public abstract <T> void removeTicket(TicketType<T> p_140824_, CloPos p_140825_, int p_140826_, T p_140827_);

    @Override
    @UsedFromASM
    @TransformFrom("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    public abstract <T> void addRegionTicket(TicketType<T> p_140841_, CloPos p_140842_, int p_140843_, T p_140844_);

    @Override
    @UsedFromASM
    @TransformFrom("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V")
    public abstract <T> void addRegionTicket(TicketType<T> p_140841_, CloPos p_140842_, int p_140843_, T p_140844_, boolean forceTicks);

    @Override
    @UsedFromASM
    @TransformFrom("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V")
    public abstract <T> void removeRegionTicket(TicketType<T> p_140850_, CloPos p_140851_, int p_140852_, T p_140853_);

    @Override
    @UsedFromASM
    @TransformFrom("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V")
    public abstract <T> void removeRegionTicket(TicketType<T> p_140850_, CloPos p_140851_, int p_140852_, T p_140853_, boolean forceTicks);

    @UsedFromASM
    @TransformFrom("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)V")
    protected abstract void updateCubeForced(CloPos p_140800_, boolean p_140801_);

    @WrapOperation(method = "updateChunkForced", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/TicketType;FORCED:Lnet/minecraft/server/level/TicketType;"))
    private TicketType<?> cc_replaceTicketTypeOnUpdateChunkForced(Operation<TicketType<ChunkPos>> original) {
        if(!cc_isCubic) return original.call();
        return CubicTicketType.FORCED;
    }

    @WrapOperation(method = "addPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/TicketType;PLAYER:Lnet/minecraft/server/level/TicketType;"))
    private TicketType<?> cc_replaceTicketTypeOnAddPlayer(Operation<TicketType<ChunkPos>> original) {
        if(!cc_isCubic) return original.call();
        return CubicTicketType.PLAYER;
    }

    @WrapOperation(method = "removePlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/TicketType;PLAYER:Lnet/minecraft/server/level/TicketType;"))
    private TicketType<?> cc_replaceTicketTypeOnRemovePlayer(Operation<TicketType<ChunkPos>> original) {
        if(!cc_isCubic) return original.call();
        return CubicTicketType.PLAYER;
    }

    // TODO: Make mixins for dumpTickets if you're feeling ambitious (I'm not, and it is debug code, so it's not a priority)

}
