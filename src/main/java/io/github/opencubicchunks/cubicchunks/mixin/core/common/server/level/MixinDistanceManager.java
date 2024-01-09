package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.DasmRedirect;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.server.level.CubicDistanceManager;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTickingTracker;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link DistanceManager} contains the main ticket hashmap and stores all the chunks that are loaded.
 * A ticket inside {@link DistanceManager} means that something is either requested to be loaded or is already loaded and needs to stay loaded.
 * It informs {@link net.minecraft.server.level.ChunkMap} of what chunks it needs to generate/load/unload to satisfy the tickets.
 * <br><br>
 * This mixin mostly just replaces calls to ChunkPos with CloPos and TicketType with CubicTicketType.
 */
@DasmRedirect()
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager implements CubicDistanceManager, MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Inject(method = {"addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
        "removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
    "addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
    "addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V",
    "removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
    "removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V",
    "updateChunkForced"}, at = @At("HEAD"))
    private void cc_onUseChunkPos(CallbackInfo ci){
        assert !cc_isCubic;
    }

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

    /**
     * This function replaces a TicketType with a CubicTicketType.
     */
    @WrapOperation(method = "updateChunkForced", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/TicketType;FORCED:Lnet/minecraft/server/level/TicketType;"))
    private TicketType<?> cc_replaceTicketTypeOnUpdateChunkForced(Operation<TicketType<ChunkPos>> original) {
        if(!cc_isCubic) return original.call();
        return CubicTicketType.FORCED;
    }

    /**
     * This function replaces the addTicket call with a cubic version instead.
     *
     * This requires replacing the TicketType with a CubicTicketType and the ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/TickingTracker;addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_replaceTicketTypeOnAddPlayer(TickingTracker instance, TicketType<T> p_184155_, ChunkPos p_184156_, int p_184157_, T p_184158_, SectionPos p_140803) {
        if(!cc_isCubic) return true;
        CloPos cloPos = CloPos.section(p_140803);
        ((CubicTickingTracker)instance).addTicket(CubicTicketType.PLAYER, cloPos, p_184157_, cloPos);
        return false;
    }

    /**
     * This function replaces the removeTicket call with a cubic version instead.
     *
     * This requires replacing the TicketType with a CubicTicketType and the ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/TickingTracker;removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_replaceTicketTypeOnRemovePlayer(TickingTracker instance, TicketType<T> p_184169_, ChunkPos p_184170_, int p_184171_, T p_184172_, SectionPos p_140803) {
        if(!cc_isCubic) return true;
        CloPos cloPos = CloPos.section(p_140803);
        ((CubicTickingTracker)instance).removeTicket(CubicTicketType.PLAYER, cloPos, p_184171_, cloPos);
        return false;
    }

    /**
     * The original function expects chunkPos.toLong(), but we need to replace it with cloPos.toLong() instead.
     */
    @WrapOperation(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    private long cc_replaceTicketTypeOnAddPlayer(ChunkPos chunkPos, Operation<Long> original, SectionPos p_140803) {
        if(!cc_isCubic) return original.call(chunkPos);
        return CloPos.section(p_140803).toLong();
    }

    /**
     * The original function expects chunkPos.toLong(), but we need to replace it with cloPos.toLong() instead.
     */
    @WrapOperation(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    private long cc_replaceTicketTypeOnRemovePlayer(ChunkPos chunkPos, Operation<Long> original, SectionPos p_140803) {
        if(!cc_isCubic) return original.call(chunkPos);
        return CloPos.section(p_140803).toLong();
    }

    // TODO: Make mixins for dumpTickets if you're feeling ambitious (I'm not, and it is debug code, so it's not a priority)

}
