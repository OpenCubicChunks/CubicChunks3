package io.github.opencubicchunks.cubicchunks.network;

import java.util.function.Consumer;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

// TODO (P2) the name is currently a lie; no light data :)
public class CCClientboundLevelCubeWithLightPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(CubicChunks.MODID, "level_cube_with_light");

    private final CubePos pos;
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

    public static class Handler implements IPlayPayloadHandler<CCClientboundLevelCubeWithLightPacket>, FriendlyByteBuf.Reader<CCClientboundLevelCubeWithLightPacket> {
        @Override
        public void handle(CCClientboundLevelCubeWithLightPacket payload, PlayPayloadContext context) {
            int x = payload.pos.getX();
            int y = payload.pos.getY();
            int z = payload.pos.getZ();
            this.updateLevelCube(context.level().get(), x, y, z, payload);
        }

        @Override
        public CCClientboundLevelCubeWithLightPacket apply(FriendlyByteBuf friendlyByteBuf)
        {
            return new CCClientboundLevelCubeWithLightPacket(friendlyByteBuf);
        }

        private void updateLevelCube(Level level, int x, int y, int z, CCClientboundLevelCubeWithLightPacket payload) {
            // TODO P2 :: The empty compound tag should become a heightmap
            CompoundTag heightmap = new CompoundTag();

            // TODO P2 :: No block entity tags consumer
            Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> entityTagConsumer = (a) -> {};

            ((ClientCubeCache)(level
                .getChunkSource()))
                .cc_replaceWithPacketData(
                    x, y, z, payload.chunkData.getReadBuffer(), heightmap, entityTagConsumer);

            // TODO P2 :: Vanilla does light updates at this point
        }
    }
}
