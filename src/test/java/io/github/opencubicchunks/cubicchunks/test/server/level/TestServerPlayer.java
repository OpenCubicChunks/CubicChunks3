package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinServerPlayer}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestServerPlayer extends BaseTest {
    private ServerPlayer setupServerPlayer(ServerLevel serverLevel) {
        var serverPlayer = new ServerPlayer(mock(RETURNS_DEEP_STUBS), serverLevel, mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS));
        serverPlayer.connection = Mockito.mock();
        return serverPlayer;
    }
}
