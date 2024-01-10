package io.github.opencubicchunks.cubicchunks.test.world.level;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestLevel {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    @Test public void testGetChunkAndGetChunkAtVanilla() {
        // TODO
    }

    @Test public void testSetBlockVanilla() {
        // TODO
    }

    @Test public void testGetBlockStateVanilla() {
        // TODO
    }

    @Test public void testIsLoadedVanilla() {
        // TODO
    }

    @Test public void testLoadedAndEntityCanStandOnFaceVanilla() {
        // TODO
    }

    @Test public void testGetChunkForCollisionsVanilla() {
        // TODO
    }
}
