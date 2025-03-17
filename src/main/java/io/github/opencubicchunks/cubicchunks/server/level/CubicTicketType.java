package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.Comparator;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.TicketTypeAccess;
import net.minecraft.server.level.TicketType;

public class CubicTicketType {
    public static final TicketType<CloPos> PLAYER = create("player", Comparator.comparingLong(CloPos::asLong));
    public static final TicketType<CloPos> FORCED = create("forced", Comparator.comparingLong(CloPos::asLong));
    public static final TicketType<CloPos> LIGHT = create("light", Comparator.comparingLong(CloPos::asLong));
    public static final TicketType<CloPos> UNKNOWN = create("unknown", Comparator.comparingLong(CloPos::asLong), 1);

    public static <T> TicketType<T> create(String nameIn, Comparator<T> comparator) {
        return TicketTypeAccess.cc_createNew(nameIn, comparator, 0L);
    }

    public static <T> TicketType<T> create(String nameIn, Comparator<T> comparator, int lifespanIn) {
        return TicketTypeAccess.cc_createNew(nameIn, comparator, lifespanIn);
    }
}
