package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// TODO (P2) the name is currently a lie; no light data :)
public class CCClientboundLevelCubeWithLightPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CubicChunks.MODID, "level_chunk");

    private CubePos pos;
    private final CCClientboundLevelCubePacketData chunkData;

    public CCClientboundLevelCubeWithLightPacket(LevelCube cube) {
        pos = cube.cc_getCloPos().cubePos();
        chunkData = new CCClientboundLevelCubePacketData(cube);
    }

    public CCClientboundLevelCubeWithLightPacket(final FriendlyByteBuf buffer) {
        pos = CubePos.of(buffer.readInt(), buffer.readInt(), buffer.readInt());
        chunkData = new CCClientboundLevelCubePacketData(buffer);
    }

    @Override public void write(final FriendlyByteBuf buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
        chunkData.write(buffer);
    }

    @Override public ResourceLocation id() {
        return ID;
    }

    public CCClientboundLevelCubePacketData getChunkData() {
        return chunkData;
    }
}
