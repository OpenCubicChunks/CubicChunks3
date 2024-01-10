package io.github.opencubicchunks.cubicchunks.test.server.level;


import static org.mockito.Mockito.mock;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * This test class is for testing TODO
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicServerLevel {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    @Test public void testChunkVanilla() {
        // TODO
    }

    @Test public void testSetDefaultSpawnPosVanilla() {
        // TODO
    }

    @Test public void testSetChunkForcedVanilla() {
        // TODO
    }
}
