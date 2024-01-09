package io.github.opencubicchunks.cubicchunks.test.server.level;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinChunkTaskPriorityQueueSorter}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicTaskPriorityQueueSorter {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    @Disabled("This test is empty, since it is a mixin that is only affecting a debug method. We could test in the future if needed.")
    @Test public void testGetDebugStatus() {}
}