package io.github.opencubicchunks.cubicchunks.test.network;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.assertDeepEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundSetCubeCacheCenterPacket;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.Misc;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCCClientboundSetCubeCacheCenterPacket extends BaseTest {
    @Test
    public void serdeTest() {
        var packet = new CCClientboundSetCubeCacheCenterPacket(CubePos.of(-5, 17, 0));
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        assertDeepEquals(new CCClientboundSetCubeCacheCenterPacket(buf), packet);
    }

    @Test
    public void handlerTest() {
        PlayPayloadContext payloadContextMock = mock();
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        ClientChunkCache clientChunkCacheMock = mock(ClientChunkCache.class);
        when(clientLevelMock.getChunkSource()).thenReturn(clientChunkCacheMock);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);

        when(payloadContextMock.level()).thenReturn(Optional.of(clientLevelMock));

        when(payloadContextMock.workHandler()).thenReturn(new Misc.DummyWorkHandler());

        var cubePos = CubePos.of(3, -2222, 42);

        var packet = new CCClientboundSetCubeCacheCenterPacket(cubePos);

        var handler = new CCClientboundSetCubeCacheCenterPacket.Handler();

        handler.handle(packet, payloadContextMock);

        ((ClientCubeCache) verify(clientChunkCacheMock, times(1))).cc_updateViewCenter(eq(cubePos.getX()), eq(cubePos.getY()), eq(cubePos.getZ()));

        verifyNoMoreInteractions(clientChunkCacheMock);
    }
}
