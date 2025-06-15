package io.github.opencubicchunks.cubicchunks.network;

import java.util.Arrays;
import java.util.Objects;

import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.chunk.LevelChunkSection;

// TODO block entities - see ClientboundLevelChunkPacketData
public class CCClientboundLevelCubePacketData {
    private final byte[] buffer;

    public static final StreamCodec<FriendlyByteBuf, CCClientboundLevelCubePacketData> STREAM_CODEC = new StreamCodec<>() {
        public CCClientboundLevelCubePacketData decode(FriendlyByteBuf buffer) {
            return new CCClientboundLevelCubePacketData(buffer);
        }

        public void encode(FriendlyByteBuf buffer, CCClientboundLevelCubePacketData data) {
            data.write(buffer);
        }
    };

    public CCClientboundLevelCubePacketData(LevelCube cube) {
        buffer = new byte[calculateChunkSize(cube)];
        extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), cube);
    }

    public CCClientboundLevelCubePacketData(final FriendlyByteBuf buffer) {
        int i = buffer.readVarInt();
        if (i > 2097152) {
            throw new RuntimeException("Cube Packet trying to allocate too much memory on read.");
        } else {
            this.buffer = new byte[i];
            buffer.readBytes(this.buffer);
        }
    }

    public void write(final FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.buffer.length);
        buffer.writeBytes(this.buffer);
    }

    // TODO could maybe dasm copy these from ClientboundLevelChunkPacketData?
    private ByteBuf getWriteBuffer() {
        ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);
        bytebuf.writerIndex(0);
        return bytebuf;
    }

    private static int calculateChunkSize(LevelCube cube) {
        int i = 0;

        for (LevelChunkSection levelchunksection : cube.getSections()) {
            i += levelchunksection.getSerializedSize();
        }

        return i;
    }

    public static void extractChunkData(FriendlyByteBuf buffer, LevelCube cube) {
        for (LevelChunkSection levelchunksection : cube.getSections()) {
            levelchunksection.write(buffer);
        }
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }

    // Implement .equals for unit testing
    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        CCClientboundLevelCubePacketData that = (CCClientboundLevelCubePacketData) o;
        return Objects.deepEquals(buffer, that.buffer);
    }

    @Override public int hashCode() {
        return Arrays.hashCode(buffer);
    }
}
