package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunkSection;

// TODO block entities - see ClientboundLevelChunkPacketData
public class CCClientboundLevelCubePacketData {
    private final byte[] buffer;

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

        for(LevelChunkSection levelchunksection : cube.getSections()) {
            i += levelchunksection.getSerializedSize();
        }

        return i;
    }

    public static void extractChunkData(FriendlyByteBuf pBuffer, LevelCube cube) {
        for (LevelChunkSection levelchunksection : cube.getSections()) {
            levelchunksection.write(pBuffer);
        }
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }
}
