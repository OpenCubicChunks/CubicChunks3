package io.github.opencubicchunks.cubicchunks.testutils;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    @AfterEach
    public void afterEach() {
        // For some reason mockito is keeping around inline mocks when no reference to them exists, so we force it to clear them after each test.
        // This was creating a strange issue where running a subset of tests would OOME only when running under gradle, but not intellij.
        // Probably a mockito bug?
        Mockito.framework().clearInlineMocks();
    }
}
