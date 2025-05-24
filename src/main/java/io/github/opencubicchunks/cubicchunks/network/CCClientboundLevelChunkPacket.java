package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class CCClientboundLevelChunkPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CubicChunks.MODID, "level_chunk");

    private final ChunkPos pos;

    public CCClientboundLevelChunkPacket(ChunkPos pos) {
        this.pos = pos;
    }

    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(pos.x);
        buffer.writeInt(pos.z);
    }

    @Override public ResourceLocation id() {
        return ID;
    }

    public static class Handler implements IPlayPayloadHandler<CCClientboundLevelChunkPacket>, FriendlyByteBuf.Reader<CCClientboundLevelChunkPacket> {

        @Override public CCClientboundLevelChunkPacket apply(FriendlyByteBuf friendlyByteBuf) {
            int x = friendlyByteBuf.readInt();
            int z = friendlyByteBuf.readInt();
            return new CCClientboundLevelChunkPacket(new ChunkPos(x, z));
        }

        @Override public void handle(CCClientboundLevelChunkPacket payload, PlayPayloadContext context) {
            int x = payload.pos.x;
            int z = payload.pos.z;
            ChunkPos chunkPos = new ChunkPos(x, z);

            // TODO P2 :: This will contain heightmap data and some other stuff
        }
    }
}
