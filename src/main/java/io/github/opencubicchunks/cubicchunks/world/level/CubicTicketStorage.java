package io.github.opencubicchunks.cubicchunks.world.level;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;

public interface CubicTicketStorage {
    void cc_addTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    void cc_addTicket(Ticket ticket, CloPos cloPos);

    void cc_removeTicketWithRadius(TicketType ticketType, CloPos cloPos, int radius);

    void cc_removeTicket(Ticket ticket, CloPos cloPos);

    boolean cc_updateChunkForced(CloPos cloPos, boolean add);
}
