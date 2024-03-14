package io.github.opencubicchunks.cubicchunks.test.server.level;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.setupServerLevel;
import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinServerPlayer}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestServerPlayer {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    private ServerPlayer setupServerPlayer(ServerLevel serverLevel) {
        return new ServerPlayer(mock(RETURNS_DEEP_STUBS), serverLevel, mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS));
    }

    // TODO: Stub. This test needs a network connection to work.
    @Disabled @Test public void testTeleportToVanilla() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            ServerPlayer player = setupServerPlayer(serverLevelReference.value());
            player.teleportTo(player.serverLevel(), 0, 0, 0, mock(), 0, 0);
            assertTrue(player.serverLevel().getChunkSource().hasChunk(0, 0));
        }
    }
}
