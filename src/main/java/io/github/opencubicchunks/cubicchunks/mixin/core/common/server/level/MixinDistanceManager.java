package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GeneralSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link DistanceManager} contains the main ticket hashmap and stores all the chunks that are loaded.
 * A ticket inside {@link DistanceManager} means that something is either requested to be loaded or is already loaded and needs to stay loaded.
 * It informs {@link net.minecraft.server.level.ChunkMap} of what chunks it needs to generate/load/unload to satisfy the tickets.
 * <br><br>
 * This mixin mostly just replaces calls to ChunkPos with CloPos and TicketType with CubicTicketType.
 */
@Dasm(GeneralSet.class)
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager implements CubicDistanceManager, MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
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
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_addTicket(TicketType<T> type, CloPos pos, int level, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_removeTicket(TicketType<T> type, CloPos pos, int level, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_addRegionTicket(TicketType<T> type, CloPos pos, int distance, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public abstract <T> void cc_addRegionTicket(TicketType<T> type, CloPos pos, int distance, T value, boolean forceTicks);

    @Override
    @UsedFromASM
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_removeRegionTicket(TicketType<T> type, CloPos pos, int distance, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public abstract <T> void cc_removeRegionTicket(TicketType<T> type, CloPos pos, int distance, T value, boolean forceTicks);

    @UsedFromASM
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)V"))
    protected abstract void updateCubeForced(CloPos pos, boolean add);

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
    private <T> boolean cc_replaceTicketTypeOnAddPlayer(TickingTracker instance, TicketType<T> type, ChunkPos chunkPos, int ticketLevel, T key, SectionPos sectionPos) {
        if(!cc_isCubic) return true;
        CloPos cloPos = CloPos.section(sectionPos);
        ((CubicTickingTracker)instance).addTicket(CubicTicketType.PLAYER, cloPos, ticketLevel, cloPos);
        return false;
    }

    /**
     * This function replaces the removeTicket call with a cubic version instead.
     *
     * This requires replacing the TicketType with a CubicTicketType and the ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/TickingTracker;removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_replaceTicketTypeOnRemovePlayer(TickingTracker instance, TicketType<T> type, ChunkPos chunkPos, int ticketLevel, T key, SectionPos sectionPos) {
        if(!cc_isCubic) return true;
        CloPos cloPos = CloPos.section(sectionPos);
        ((CubicTickingTracker)instance).removeTicket(CubicTicketType.PLAYER, cloPos, ticketLevel, cloPos);
        return false;
    }

    /**
     * The original function expects chunkPos.toLong(), but we need to replace it with cloPos.toLong() instead.
     */
    @WrapOperation(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    private long cc_replaceTicketTypeOnAddPlayer(ChunkPos chunkPos, Operation<Long> original, SectionPos sectionPos) {
        if(!cc_isCubic) return original.call(chunkPos);
        return CloPos.section(sectionPos).toLong();
    }

    /**
     * The original function expects chunkPos.toLong(), but we need to replace it with cloPos.toLong() instead.
     */
    @WrapOperation(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    private long cc_replaceTicketTypeOnRemovePlayer(ChunkPos chunkPos, Operation<Long> original, SectionPos sectionPos) {
        if(!cc_isCubic) return original.call(chunkPos);
        return CloPos.section(sectionPos).toLong();
    }

    /**
     * This function adds in the CC-specific ticket types to immutableset, since immutableset contains the list of tickets that cannot be removed
     * (they are all added under very specific circumstances and are removed at some point later). We can have that set contain both CC and
     * non-CC tickets and still function correctly.
     */
    @Redirect(method = "removeTicketsOnClosing", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableSet;" ))
    private ImmutableSet<Object> cc_addIrremovableTicketsToSet(Object e1, Object e2, Object e3) {
        return ImmutableSet.of(e1, e2, e3, CubicTicketType.UNKNOWN, CubicTicketType.LIGHT);
    }

    // TODO: Make mixins for dumpTickets if you're feeling ambitious (I'm not, and it is debug code, so it's not a priority)

}
