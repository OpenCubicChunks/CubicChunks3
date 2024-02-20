package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cubicchunks.mixin.access.common.TicketTypeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import java.util.Comparator;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;

public class CubicTicketType {
    public static final TicketType<CloPos> PLAYER = create("player", Comparator.comparingLong(CloPos::asLong));
    public static final TicketType<CloPos> FORCED = create("forced", Comparator.comparingLong(CloPos::asLong));
    public static final TicketType<CloPos> LIGHT = create("light", Comparator.comparingLong(CloPos::asLong));
    public static final TicketType<CloPos> UNKNOWN = create("unknown", Comparator.comparingLong(CloPos::asLong), 1);
    // TODO we don't actually need this; remove it and use TicketType.START
    public static final TicketType<Unit> START = create("start", (a, b) -> 0);

    public static <T> TicketType<T> create(String nameIn, Comparator<T> comparator) {
        return TicketTypeAccess.createNew(nameIn, comparator, 0L);
    }

    public static <T> TicketType<T> create(String nameIn, Comparator<T> comparator, int lifespanIn) {
        return TicketTypeAccess.createNew(nameIn, comparator, lifespanIn);
    }
}
