package io.github.opencubicchunks.cubicchunks.test.server.level;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;


/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.client.multiplayer.MixinClientLevel}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicClientLevel {
    @BeforeAll
    public static void setup() {
        setupTests();
    }
}
