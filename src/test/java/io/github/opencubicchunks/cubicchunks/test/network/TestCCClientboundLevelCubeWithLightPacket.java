package io.github.opencubicchunks.cubicchunks.test.network;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.assertDeepEquals;
import static io.github.opencubicchunks.cubicchunks.testutils.Misc.generateRandomLevelCube;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelCubeWithLightPacket;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCCClientboundLevelCubeWithLightPacket extends BaseTest {

    @Test
    public void serdeTest() {
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);
        var pos1 = CubePos.of(1, 2, 3);
        var pos2 = CubePos.of(0, -2, 4);
        var cube1 = new LevelCube(clientLevelMock, pos1);
        var cube2 = generateRandomLevelCube(clientLevelMock, pos2, new Random(3333));

        var packet1 = new CCClientboundLevelCubeWithLightPacket(cube1);
        var packet2 = new CCClientboundLevelCubeWithLightPacket(cube2);

        var buf1 = new FriendlyByteBuf(Unpooled.buffer());
        var buf2 = new FriendlyByteBuf(Unpooled.buffer());

        CCClientboundLevelCubeWithLightPacket.STREAM_CODEC.encode(buf1, packet1);
        CCClientboundLevelCubeWithLightPacket.STREAM_CODEC.encode(buf2, packet2);

        assertDeepEquals(CCClientboundLevelCubeWithLightPacket.STREAM_CODEC.decode(buf1), packet1);
        assertDeepEquals(CCClientboundLevelCubeWithLightPacket.STREAM_CODEC.decode(buf2), packet2);
    }

    @Disabled // TODO disabled until we can apply client-side mixins in tests properly
    @Test
    public void handlerTest() {
        IPayloadContext payloadContextMock = mock(Mockito.RETURNS_DEEP_STUBS);
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        ClientChunkCache clientChunkCacheMock = mock(ClientChunkCache.class);
        when(clientLevelMock.getChunkSource()).thenReturn(clientChunkCacheMock);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);

        when(payloadContextMock.player().level()).thenReturn(clientLevelMock);

        var pos = CubePos.of(10, -2, 4);
        var cube = generateRandomLevelCube(clientLevelMock, pos, new Random(3333));

        var packet = new CCClientboundLevelCubeWithLightPacket(cube);

        var handler = new CCClientboundLevelCubeWithLightPacket.Handler();

        handler.handle(packet, payloadContextMock);

        ((ClientCubeCache) verify(clientChunkCacheMock, times(1))).cc_replaceWithPacketData(eq(pos.getX()), eq(pos.getY()), eq(pos.getZ()), any(),
                any(), any());
    }
}
