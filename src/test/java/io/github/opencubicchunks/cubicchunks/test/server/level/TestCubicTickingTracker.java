package io.github.opencubicchunks.cubicchunks.test.server.level;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTickingTracker;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * This test class is for testing {@link TickingTracker} with {@link io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos} instead of {@link ChunkPos}.
 * <br><br>
 * We only test replacePlayerTicketsLevel since that is the only method that needs any bespoke functionality in {@link TickingTracker}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicTickingTracker {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    private TickingTracker setupTracker() {
        var tracker = new TickingTracker();
        ((MarkableAsCubic) tracker).cc_setCubic();
        return tracker;
    }

    @Test public void testReplaceSinglePlayerTicketLevel() {
        var tracker = setupTracker();
        var clopos = CloPos.cube(0, 0, 0);
        ((CubicTickingTracker)tracker).addTicket(CubicTicketType.PLAYER, clopos, 0, clopos);
        tracker.runAllUpdates();
        assertEquals(0, ((CubicTickingTracker)tracker).getLevel(clopos), "Adding ticket failed.");
        tracker.replacePlayerTicketsLevel(2);
        tracker.runAllUpdates();
        assertEquals(2, ((CubicTickingTracker)tracker).getLevel(clopos), "Replacing ticket failed.");
    }

    @Test public void testReplaceMultiplePlayerTicketsLevel() {
        var tracker = setupTracker();
        var clopos = CloPos.cube(0, 0, 0);
        ((CubicTickingTracker)tracker).addTicket(CubicTicketType.PLAYER, clopos, 0, clopos);
        tracker.runAllUpdates();
        assertEquals(0, ((CubicTickingTracker)tracker).getLevel(clopos), "Adding ticket failed.");
        ((CubicTickingTracker)tracker).addTicket(CubicTicketType.PLAYER, clopos, 0, clopos);
        tracker.runAllUpdates();
        assertEquals(0, ((CubicTickingTracker)tracker).getLevel(clopos), "Adding ticket failed.");
        tracker.replacePlayerTicketsLevel(2);
        tracker.runAllUpdates();
        assertEquals(2, ((CubicTickingTracker)tracker).getLevel(clopos), "Replacing ticket failed.");
    }

    @Test public void testReplaceSingleNonPlayerTicketLevel() {
        var tracker = setupTracker();
        var clopos = CloPos.cube(0, 0, 0);
        ((CubicTickingTracker)tracker).addTicket(CubicTicketType.UNKNOWN, clopos, 0, clopos);
        tracker.runAllUpdates();
        assertEquals(0, ((CubicTickingTracker)tracker).getLevel(clopos), "Adding ticket failed.");
        tracker.replacePlayerTicketsLevel(2);
        tracker.runAllUpdates();
        assertEquals(0, ((CubicTickingTracker)tracker).getLevel(clopos), "Replacing ticket failed.");
    }

    @Test public void testReplaceMixedTicketsLevel() {
        var tracker = setupTracker();
        var clopos = CloPos.cube(0, 0, 0);
        ((CubicTickingTracker)tracker).addTicket(CubicTicketType.PLAYER, clopos, 4, clopos);
        tracker.runAllUpdates();
        assertEquals(4, ((CubicTickingTracker)tracker).getLevel(clopos), "Adding ticket failed.");
        ((CubicTickingTracker)tracker).addTicket(CubicTicketType.UNKNOWN, clopos, 3, clopos);
        tracker.runAllUpdates();
        assertEquals(3, ((CubicTickingTracker)tracker).getLevel(clopos), "Adding ticket failed.");
        tracker.replacePlayerTicketsLevel(2);
        tracker.runAllUpdates();
        assertEquals(2, ((CubicTickingTracker)tracker).getLevel(clopos), "Replacing ticket failed.");
    }
}
