package io.github.opencubicchunks.cubicchunks.network;

import static io.github.opencubicchunks.cubicchunks.network.MiscStreamCodecs.CLO_POS_STREAM_CODEC;

import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CCClientboundLevelChunkPacket(ChunkPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CCClientboundLevelChunkPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CubicChunks.MODID, "level_chunk"));

    public static final StreamCodec<ByteBuf, CCClientboundLevelChunkPacket> STREAM_CODEC = StreamCodec.composite(
        ChunkPos.STREAM_CODEC, CCClientboundLevelChunkPacket::pos,
        CCClientboundLevelChunkPacket::new
    );

    @Override public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<CCClientboundLevelChunkPacket> {
        @Override public void handle(CCClientboundLevelChunkPacket payload, IPayloadContext context) {
            int x = payload.pos.x;
            int z = payload.pos.z;
            // TODO P2 :: This will contain heightmap data and some other stuff
            updateLevelChunk(context.player().level(), x, z);
        }

        private void updateLevelChunk(Level level, int x, int z) {

        }
    }
}
