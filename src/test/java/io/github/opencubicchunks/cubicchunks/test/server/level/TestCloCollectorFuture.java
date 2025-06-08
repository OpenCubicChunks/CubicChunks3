package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.opencubicchunks.cubicchunks.server.level.CloCollectorFuture;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.server.level.ChunkResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCloCollectorFuture extends BaseTest {
    @Test public void testFutureCompletion() throws Exception {
        for (int i = 0; i < 9; i++) { // Run nine times, putting the center cube at each index
            var future = new CloCollectorFuture(9);
            CloAccess centerCube = mock();
            CloAccess others = mock();
            for (int j = 0; j < 9; j++) {
                assertFalse(future.isDone(), "future shouldn't complete until all Clos are added");
                future.add(ChunkResult.of(j == i ? centerCube : others), null, j == i);
            }
            assertTrue(future.isDone(), "future should be complete once all Clos are added");
            assertSame(future.get().get(4).orElse(null), centerCube, "centerCube should be at center index");
            for (int j = 0; j < 9; j++) {
                if (j == 4) continue;
                assertNotSame(future.get().get(j).orElse(null), centerCube, "reference to centerCube should not be duplicated at any other index");
            }
        }
    }

    @Test public void testMultipleCenterCubes() {
        var future = new CloCollectorFuture(9);
        CloAccess cloAccess = mock();
        future.add(ChunkResult.of(cloAccess), null, true);
        assertThrows(IllegalStateException.class, () -> future.add(ChunkResult.of(cloAccess), null, true), "trying to set centerCube multiple times should throw an exception");
    }

    @Test public void testMissingCenterCube() {
        var future = new CloCollectorFuture(9);
        CloAccess cloAccess = mock();
        for (int i = 0; i < 8; i++) {
            future.add(ChunkResult.of(cloAccess), null, false);
        }
        assertThrows(IllegalStateException.class, () -> future.add(ChunkResult.of(cloAccess), null, false), "completing without having set centerCube should throw an exception");
    }

    @Test public void testFutureExceptionPropagation() {
        for (int i = 0; i < 9; i++) { // Run nine times, throwing an exception at each index
            var future = new CloCollectorFuture(9);
            var exception = new Exception("test exception");
            CloAccess cloAccess = mock();
            for (int j = 0; j < 9; j++) {
                future.add(ChunkResult.of(cloAccess), i == j ? exception : null, false);
            }
            assertTrue(future.isCompletedExceptionally(), "future should complete exceptionally upon receiving an Exception");
        }
    }
}
