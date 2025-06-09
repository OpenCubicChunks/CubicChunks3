package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;


import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.world.level.CubicTicketStorage;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(ChunkToCloSet.class)
@Mixin(TicketStorage.class)
public class MixinTicketStorage implements CubicTicketStorage {
    // TODO (P2) codec nonsense for save/load

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(TicketStorage.class), value = @MethodSig("addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_addTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(TicketStorage.class), value = @MethodSig("addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_addTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(TicketStorage.class), value = @MethodSig("removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_removeTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(TicketStorage.class), value = @MethodSig("removeTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    public native void cc_removeTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(TicketStorage.class), value = @MethodSig("updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)Z"))
    public native boolean cc_updateChunkForced(CloPos cloPos, boolean add);

    // TODO move to neoforge-specific mixin
    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(owner = @Ref(TicketStorage.class), value = @MethodSig("shouldForceNaturalSpawning(Lnet/minecraft/world/level/ChunkPos;)Z"))
    public native boolean cc_shouldForceNaturalSpawning(CloPos cloPos);
}
