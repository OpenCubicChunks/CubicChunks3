package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import org.codehaus.plexus.util.CollectionUtils;
import org.junit.jupiter.api.Test;

public class TestCloTrackingView extends BaseTest {
    // TODO might be possible to test this class more thoroughly, but this should be sufficient for now - non-trivial methods have full coverage
    @Test public void basicTests() {
        var originPos = CloPos.cube(0, 0, 0);
        var originChunk = CloPos.chunk(0, 0);
        var origin = CloTrackingView.cc_of(originPos, 1);

        assertTrue(origin.cc_contains(originPos));
        assertTrue(origin.cc_contains(originChunk));
        assertTrue(origin.contains(originChunk.chunkPos()));

        var list1 = new ArrayList<>();
        var list2 = new ArrayList<>();

        CloTrackingView.cc_difference(CloTrackingView.EMPTY, origin, list2::add, list1::add);
        assertThat(list1).isEmpty();
        assertThat(list2).anyMatch(pos -> pos.equals(originPos));
        assertThat(list2).anyMatch(pos -> pos.equals(originChunk));

        list1.clear();
        list2.clear();

        var higherUpPos = CloPos.cube(0, 100, 0);
        var higherUp = CloTrackingView.cc_of(higherUpPos, 1);

        assertFalse(higherUp.cc_contains(originPos));
        assertTrue(higherUp.cc_contains(higherUpPos));
        assertTrue(higherUp.cc_contains(originChunk));

        CloTrackingView.cc_difference(origin, higherUp, list2::add, list1::add);

        assertThat(list1).anyMatch(pos -> pos.equals(originPos));
        assertThat(list2).anyMatch(pos -> pos.equals(higherUpPos));
        // origin chunk is still included; shouldn't be added or removed
        assertThat(list1).noneMatch(pos -> pos.equals(originChunk));
        assertThat(list2).noneMatch(pos -> pos.equals(originChunk));

        list1.clear();
        list2.clear();

        CloTrackingView.cc_difference(higherUp, CloTrackingView.EMPTY, list2::add, list1::add);
        assertThat(list2).isEmpty();
        assertThat(list1).anyMatch(pos -> pos.equals(higherUpPos));
        assertThat(list1).anyMatch(pos -> pos.equals(originChunk));

        list1.clear();
        list2.clear();

        var farAwayPos = CloPos.cube(-100, -100, -100);
        var farAway = CloTrackingView.cc_of(farAwayPos, 1);

        assertFalse(farAway.cc_contains(originPos));
        assertFalse(farAway.cc_contains(originChunk));
        assertTrue(farAway.cc_contains(farAwayPos));
        assertTrue(farAway.cc_contains(farAwayPos.correspondingChunkCloPos()));

        farAway.cc_forEach(list1::add);
        assertThat(list1).anyMatch(pos -> pos.equals(farAwayPos));
        assertThat(list1).anyMatch(pos -> pos.equals(farAwayPos.correspondingChunkCloPos()));

        farAway.forEach(list2::add);
        assertThat(list2).anyMatch(pos -> pos.equals(farAwayPos.correspondingChunkPos()));
    }

    private void checkDifference(CloTrackingView before, CloTrackingView after, Set<CloPos> beforePos, Set<CloPos> afterPos) {
        var removed = new ArrayList<CloPos>();
        var added = new ArrayList<CloPos>();
        CloTrackingView.cc_difference(before, after, added::add, removed::add);
        var eRemoved = CollectionUtils.subtract(beforePos, afterPos);
        var eAdded = CollectionUtils.subtract(afterPos, beforePos);
        assertThat(removed).allMatch(eRemoved::contains);
        assertThat(eRemoved).allMatch(removed::contains);
        assertThat(added).allMatch(eAdded::contains);
        assertThat(eAdded).allMatch(added::contains);
    }

    @Test public void testDifferenceRandomized() {
        var random = new Random(120829);

        for (int i = 0; i < 100; i++) {
            var pos1 = CloPos.cube(random.nextInt(10000)-5000, random.nextInt(10000)-5000, random.nextInt(10000)-5000);
            var pos2 = CloPos.cube(pos1.getX() + random.nextInt(20)-10, pos1.getY() + random.nextInt(20)-10, pos1.getZ() + random.nextInt(20)-10);
            int r1 = random.nextInt(0, 10);
            int r2 = random.nextInt(0, 10);
            var track1 = CloTrackingView.cc_of(pos1, r1);
            var track2 = CloTrackingView.cc_of(pos2, r2);
            var list = new ArrayList<CloPos>();
            track1.cc_forEach(list::add);
            var set1 = Set.copyOf(list);
            list.clear();
            track2.cc_forEach(list::add);
            var set2 = Set.copyOf(list);
            checkDifference(track1, track2, set1, set2);
        }
    }
}
