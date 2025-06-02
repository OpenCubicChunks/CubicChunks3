package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MiscStreamCodecs {
    // TODO where should these go? can't go on CloPos/CubePos because they're in core
    public static final StreamCodec<ByteBuf, CloPos> CLO_POS_STREAM_CODEC = new StreamCodec<>() {
        public CloPos decode(ByteBuf buffer) {
            return CloPos.fromLong(buffer.readLong());
        }

        public void encode(ByteBuf buffer, CloPos cloPos) {
            buffer.writeLong(cloPos.asLong());
        }
    };

    public static final StreamCodec<ByteBuf, CubePos> CUBE_POS_STREAM_CODEC = new StreamCodec<>() {
        public CubePos decode(ByteBuf buffer) {
            return CubePos.from(buffer.readLong());
        }

        public void encode(ByteBuf buffer, CubePos cubePos) {
            buffer.writeLong(cubePos.asLong());
        }
    };
}
