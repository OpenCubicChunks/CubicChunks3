package io.github.opencubicchunks.cubicchunks.test.server.level;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicTaskPriorityQueue {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    @Test public void testResortChunkTasks() {
        // TODO
    }
}
