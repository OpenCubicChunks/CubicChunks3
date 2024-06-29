package io.github.opencubicchunks.cubicchunks.test.network;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.assertDeepEquals;
import static io.github.opencubicchunks.cubicchunks.testutils.Misc.generateRandomLevelCube;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelCubeWithLightPacket;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCCClientboundLevelCubeWithLightPacket extends BaseTest {
    @Test
    public void serdeTest() {
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);
        var pos1 = CloPos.cube(1, 2, 3);
        var pos2 = CloPos.cube(0, -2, 4);
        var cube1 = new LevelCube(clientLevelMock, pos1);
        var cube2 = generateRandomLevelCube(clientLevelMock, pos2, new Random(3333));

        var packet1 = new CCClientboundLevelCubeWithLightPacket(cube1);
        var packet2 = new CCClientboundLevelCubeWithLightPacket(cube2);

        var buf1 = new FriendlyByteBuf(Unpooled.buffer());
        var buf2 = new FriendlyByteBuf(Unpooled.buffer());

        packet1.write(buf1);
        packet2.write(buf2);

        assertDeepEquals(new CCClientboundLevelCubeWithLightPacket(buf1), packet1);
        assertDeepEquals(new CCClientboundLevelCubeWithLightPacket(buf2), packet2);
    }
}
