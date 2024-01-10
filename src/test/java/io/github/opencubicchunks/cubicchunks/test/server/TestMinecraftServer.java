package io.github.opencubicchunks.cubicchunks.test.server;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMinecraftServer {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    @Test public void testSetInitialSpawnVanilla() {
        // TODO
    }

    @Test public void testPrepareLevelsVanilla() {
        // TODO
    }
}
