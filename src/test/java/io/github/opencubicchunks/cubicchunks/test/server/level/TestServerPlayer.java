package io.github.opencubicchunks.cubicchunks.test.server.level;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.setupServerLevel;
import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

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
        var serverPlayer = new ServerPlayer(mock(RETURNS_DEEP_STUBS), serverLevel, mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS));
        serverPlayer.connection = Mockito.mock();
        return serverPlayer;
    }

    @Test public void testTeleportToVanilla() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            ServerPlayer player = setupServerPlayer(serverLevelReference.value());

            // teleportTo takes the destination level to teleport to as a param, so we mock it
            ServerLevel serverLevelSpy = spy(serverLevelReference.value());
            ServerChunkCache serverChunkCacheMock = mock(ServerChunkCache.class);
            Mockito.when(serverLevelSpy.getChunkSource()).thenReturn(serverChunkCacheMock);

            player.teleportTo(serverLevelSpy, 0, 0, 0, mock(), 0, 0);

            // Verify that serverChunkCacheMock.addRegionTicket(...) is called once
            Mockito.verify(serverChunkCacheMock, Mockito.times(1))
                .addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(0, 0), 1, player.getId());
        }
    }
}
