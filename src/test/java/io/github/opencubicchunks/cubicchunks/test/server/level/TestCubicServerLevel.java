package io.github.opencubicchunks.cubicchunks.test.server.level;


import static io.github.opencubicchunks.cubicchunks.testutils.Misc.setupServerLevel;
import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.withSettings;

import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.RandomState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;
import org.mockito.Mockito;

/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinServerLevel}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicServerLevel {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    private void unregisterMocks() {
        Mockito.mockStatic(RandomState.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
    }

    // TODO: Phase 3 - This needs a more rigorous test down the line when we actually care about entities
    @Test public void testVanillaSpawningAllowed() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            assertFalse(serverLevelReference.value().isNaturalSpawningAllowed(new ChunkPos(0, 0)));
        }
    }

    // TODO: Phase 3 - This is part of the neoforge API and will need a more rigorous test when we need to support their API
    @Test public void testInvalidateCapabilities() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            serverLevelReference.value().invalidateCapabilities(new ChunkPos(0, 0));
        }
    }

    // TODO: Stub.
    @Test public void testTickChunk() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            serverLevelReference.value().tickChunk(new LevelChunk(serverLevelReference.value(), new ChunkPos(0, 0)), 10);
        }
    }

    // TODO: Stub. This test hangs. Maybe due to ForcedChunksSavedData?
    @Test @Disabled public void testSetChunkForced() throws Exception{
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            serverLevelReference.value().setChunkForced(0, 0, true);
        }
    }

    // TODO: Stub.
    @Test public void testIsPositionEntityTicking() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            assertFalse(serverLevelReference.value().isPositionEntityTicking(BlockPos.ZERO));
        }
    }
}
