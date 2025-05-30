package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CCClientboundForgetLevelCloPacket(CloPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CubicChunks.MODID, "forget_clo");

    public CCClientboundForgetLevelCloPacket(FriendlyByteBuf buffer) {
        this(CloPos.fromLong(buffer.readLong()));
    }

    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(pos.asLong());
    }

    @Override public ResourceLocation id() {
        return ID;
    }

    public static class Handler implements IPlayPayloadHandler<CCClientboundForgetLevelCloPacket>{
        @Override public void handle(CCClientboundForgetLevelCloPacket payload, PlayPayloadContext context) {
            var clientChunkCache = ((ClientChunkCache) context.level().get().getChunkSource());
            context.workHandler().execute(() -> {
                // TODO P2: queueLightRemoval - look at vanilla packet handler
                if (payload.pos.isChunk()) {
                    clientChunkCache.drop(payload.pos.chunkPos());
                } else {
                    ((ClientCubeCache) clientChunkCache).cc_drop(payload.pos.cubePos());
                }
            });
        }
    }
}
