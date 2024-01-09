package io.github.opencubicchunks.cubicchunks.test.server.level;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinPlayerRespawnLogic}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestPlayerRespawnLogic {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    // TODO: There are no tests here because the only way to test this is with integration tests.
}

