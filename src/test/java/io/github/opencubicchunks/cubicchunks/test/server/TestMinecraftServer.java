package io.github.opencubicchunks.cubicchunks.test.server;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMinecraftServer {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    @Test public void testSetInitialSpawnVanilla() {
        // TODO
    }

    @Test public void testPrepareLevelsVanilla() {
        // TODO
    }
}
