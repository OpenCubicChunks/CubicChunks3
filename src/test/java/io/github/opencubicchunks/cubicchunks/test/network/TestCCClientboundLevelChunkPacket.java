package io.github.opencubicchunks.cubicchunks.test.network;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.assertDeepEquals;

import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelChunkPacket;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

public class TestCCClientboundLevelChunkPacket extends BaseTest {
    @Test
    public void serdeTest() {
        var packet = new CCClientboundLevelChunkPacket(new ChunkPos(2, 4));
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        CCClientboundLevelChunkPacket.STREAM_CODEC.encode(buf, packet);
        assertDeepEquals(CCClientboundLevelChunkPacket.STREAM_CODEC.decode(buf), packet);
    }
}
