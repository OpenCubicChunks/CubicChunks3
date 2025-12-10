package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.world.level.CubicTicketStorage;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(value = ChunkToCloSet.class, target = @Ref(TicketStorage.class))
@Mixin(TicketStorage.class)
public class MixinTicketStorage implements CubicTicketStorage {
    // TODO (P2) codec nonsense for save/load

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V")
    public native void cc_addTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V")
    public native void cc_addTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V")
    public native void cc_removeTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "removeTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V")
    public native void cc_removeTicket(Ticket ticket, CloPos cloPos);

    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "updateChunkForced(Lnet/minecraft/world/level/ChunkPos;Z)Z")
    public native boolean cc_updateChunkForced(CloPos cloPos, boolean add);

    // TODO move to neoforge-specific mixin
    @AddTransformToSets(ChunkToCloSet.TicketStorage_redirects.class)
    @TransformFromMethod(owner = @Ref(TicketStorage.class), value = "shouldForceNaturalSpawning(Lnet/minecraft/world/level/ChunkPos;)Z")
    public native boolean cc_shouldForceNaturalSpawning(CloPos cloPos);
}
