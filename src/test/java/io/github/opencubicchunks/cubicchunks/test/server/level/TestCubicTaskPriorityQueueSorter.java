package io.github.opencubicchunks.cubicchunks.test.server.level;

import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinChunkTaskPriorityQueueSorter}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicTaskPriorityQueueSorter extends BaseTest {
    @Disabled("This test is empty, since it is a mixin that is only affecting a debug method. We could test in the future if needed.")
    @Test public void testGetDebugStatus() {}
}