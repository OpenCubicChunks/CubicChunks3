package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubicDistanceManager;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTickingTracker;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@link DistanceManager} contains the main ticket hashmap and stores all the chunks that are loaded.
 * A ticket inside {@link DistanceManager} means that something is either requested to be loaded or is already loaded and needs to stay loaded.
 * It informs {@link ChunkMap} of what chunks it needs to generate/load/unload to satisfy the tickets.
 * <br><br>
 * This mixin mostly just replaces calls to ChunkPos with CloPos.
 */
@Dasm(ChunkToCloSet.class)
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager implements CubicDistanceManager, MarkableAsCubic {
    protected boolean cc_isCubic;
    @Shadow @Final private DistanceManager.ChunkTicketTracker ticketTracker;
    @Shadow @Final private DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter;
    @Shadow @Final private TickingTracker tickingTicketsTracker;
    @Shadow @Final private DistanceManager.PlayerTicketTracker playerTicketManager;
    @Shadow @Final ChunkTaskPriorityQueueSorter ticketThrottler;

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
        ((MarkableAsCubic) this.ticketTracker).cc_setCubic();
        ((MarkableAsCubic) this.naturalSpawnChunkCounter).cc_setCubic();
        ((MarkableAsCubic) this.tickingTicketsTracker).cc_setCubic();
        ((MarkableAsCubic) this.playerTicketManager).cc_setCubic();
        ((MarkableAsCubic) this.ticketThrottler).cc_setCubic();
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
    }

    // TODO working with chunks in CC contexts still calls these methods; is this something we want to avoid?
//    @Inject(method = {"addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
//        "removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
//    "addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
//    "addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V",
//    "removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V",
//    "removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V",
//    "updateChunkForced"}, at = @At("HEAD"))
//    private void cc_onUseChunkPos(CallbackInfo ci){
//        assert !cc_isCubic;
//    }

    @Override
    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_addTicket(TicketType type, CloPos pos, int level, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_removeTicket(TicketType type, CloPos pos, int level, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_addRegionTicket(TicketType type, CloPos pos, int distance, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public abstract <T> void cc_addRegionTicket(TicketType type, CloPos pos, int distance, T value, boolean forceTicks);

    @Override
    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public abstract <T> void cc_removeRegionTicket(TicketType type, CloPos pos, int distance, T value);

    @Override
    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public abstract <T> void cc_removeRegionTicket(TicketType type, CloPos pos, int distance, T value, boolean forceTicks);

    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(@MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)V"))
    public abstract void cc_updateCubeForced(CloPos pos, boolean add);

    // CubePos equivalents that delegate to their corresponding CloPos method
    // For ticket types that hold a CloPos, we additionally must convert the ticket value.
    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public <T> void cc_addTicket(TicketType type, CubePos pos, int level, T value) {
        cc_addTicket(type, CloPos.cube(pos), level, value instanceof CubePos cube ? (T) CloPos.cube(cube) : value);
    }

    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public <T> void cc_removeTicket(TicketType type, CubePos pos, int level, T value) {
        cc_removeTicket(type, CloPos.cube(pos), level, value instanceof CubePos cube ? (T) CloPos.cube(cube) : value);
    }

    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public <T> void cc_addRegionTicket(TicketType type, CubePos pos, int distance, T value) {
        cc_addRegionTicket(type, CloPos.cube(pos), distance, value instanceof CubePos cube ? (T) CloPos.cube(cube) : value);
    }

    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public <T> void cc_addRegionTicket(TicketType type, CubePos pos, int distance, T value, boolean forceTicks) {
        cc_addRegionTicket(type, CloPos.cube(pos), distance, value instanceof CubePos cube ? (T) CloPos.cube(cube) : value, forceTicks);
    }

    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public <T> void cc_removeRegionTicket(TicketType type, CubePos pos, int distance, T value) {
        cc_removeRegionTicket(type, CloPos.cube(pos), distance, value instanceof CubePos cube ? (T) CloPos.cube(cube) : value);
    }

    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;Z)V"))
    public <T> void cc_removeRegionTicket(TicketType type, CubePos pos, int distance, T value, boolean forceTicks) {
        cc_removeRegionTicket(type, CloPos.cube(pos), distance, value instanceof CubePos cube ? (T) CloPos.cube(cube) : value, forceTicks);
    }

    @UsedFromASM
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(DistanceManager.class), method = @MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)V"))
    public void cc_updateCubeForced(CubePos pos, boolean add) {
        cc_updateCubeForced(CloPos.cube(pos), add);
    }


    /**
     * This function replaces the addTicket call with a cubic version instead.
     *
     * This requires replacing the ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/TickingTracker;addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_replaceTicketTypeOnAddPlayer(TickingTracker instance, TicketType type, ChunkPos chunkPos, int ticketLevel, T key, SectionPos sectionPos) {
        if(!cc_isCubic) return true;
        CloPos cloPos = CloPos.section(sectionPos);
        ((CubicTickingTracker)instance).cc_addTicket(type, cloPos, ticketLevel, cloPos);
        return false;
    }

    /**
     * This function replaces the removeTicket call with a cubic version instead.
     *
     * This requires replacing ChunkPos with a CloPos.
     */
    @WrapWithCondition(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/TickingTracker;removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_replaceTicketTypeOnRemovePlayer(TickingTracker instance, TicketType type, ChunkPos chunkPos, int ticketLevel, T key, SectionPos sectionPos) {
        if(!cc_isCubic) return true;
        CloPos cloPos = CloPos.section(sectionPos);
        ((CubicTickingTracker)instance).cc_removeTicket(type, cloPos, ticketLevel, cloPos);
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

    @Override
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("runAllUpdates(Lnet/minecraft/server/level/ChunkMap;)Z"))
    public native boolean cc_runAllUpdates(ChunkMap chunkManager);

    // TODO: Make mixins for dumpTickets if you're feeling ambitious (I'm not, and it is debug code, so it's not a priority)

}
