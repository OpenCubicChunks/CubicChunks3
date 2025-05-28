package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class CCClientboundLevelChunkPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CubicChunks.MODID, "level_chunk");

    private final ChunkPos pos;

    public CCClientboundLevelChunkPacket(ChunkPos pos) {
        this.pos = pos;
    }

    public CCClientboundLevelChunkPacket(FriendlyByteBuf buffer) {
        this.pos = new ChunkPos(buffer.readInt(), buffer.readInt());
    }

    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(pos.x);
        buffer.writeInt(pos.z);
    }

    @Override public ResourceLocation id() {
        return ID;
    }

    public static class Handler implements IPlayPayloadHandler<CCClientboundLevelChunkPacket> {
        @Override public void handle(CCClientboundLevelChunkPacket payload, PlayPayloadContext context) {
            int x = payload.pos.x;
            int z = payload.pos.z;
            // TODO P2 :: This will contain heightmap data and some other stuff
            context.workHandler().execute(() -> updateLevelChunk(context.level().get(), x, z));
        }

        private void updateLevelChunk(Level level, int x, int z) {

        }
    }
}
