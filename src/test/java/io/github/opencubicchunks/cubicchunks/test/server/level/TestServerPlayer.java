package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.RandomState;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;
import org.mockito.Mockito;

/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinServerPlayer}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestServerPlayer {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    private ServerPlayer setupServerPlayer() {
        Mockito.mockStatic(RandomState.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        return new ServerPlayer(mock(), new ServerLevel(mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), false, 0, mock(RETURNS_DEEP_STUBS), false, mock(RETURNS_DEEP_STUBS)), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS));
    }

    /**
     * This test is ignored for now. setupServerPlayer() needs to be correctly mocked first.
     */
    @Ignore @Test public void testTeleportToVanilla() {
        //ServerPlayer player = setupServerPlayer();
        //player.teleportTo(player.serverLevel(), 0, 0, 0, mock(), 0, 0);
        //assertTrue(player.serverLevel().getChunkSource().hasChunk(0, 0));
    }
}
