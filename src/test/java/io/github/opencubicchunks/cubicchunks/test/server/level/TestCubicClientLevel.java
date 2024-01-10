package io.github.opencubicchunks.cubicchunks.test.server.level;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;


/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.client.multiplayer.MixinClientLevel}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicClientLevel {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }
}
