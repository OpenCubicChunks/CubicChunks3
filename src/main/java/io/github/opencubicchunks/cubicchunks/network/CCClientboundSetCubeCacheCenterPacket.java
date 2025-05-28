package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CCClientboundSetCubeCacheCenterPacket(CubePos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CubicChunks.MODID, "set_cube_cache_center");

    public CCClientboundSetCubeCacheCenterPacket(FriendlyByteBuf buffer) {
        this(CubePos.from(buffer.readLong()));
    }

    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(pos.asLong());
    }

    @Override public ResourceLocation id() {
        return ID;
    }

    public static class Handler implements IPlayPayloadHandler<CCClientboundSetCubeCacheCenterPacket> {
        @Override public void handle(CCClientboundSetCubeCacheCenterPacket payload, PlayPayloadContext context) {
            var clientChunkCache = ((ClientChunkCache) context.level().get().getChunkSource());
            context.workHandler().execute(() -> {
                ((ClientCubeCache) clientChunkCache).cc_updateViewCenter(payload.pos.getX(), payload.pos.getY(), payload.pos.getZ());
            });
        }
    }
}
