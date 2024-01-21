package io.github.opencubicchunks.cubicchunks.test.world.level;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicLevel {
    @BeforeAll
    public static void setup() {
        setupTests();
    }
}
