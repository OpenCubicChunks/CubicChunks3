package io.github.opencubicchunks.cubicchunks.server.level;

import net.minecraft.server.level.ServerPlayer;

public interface CubicChunkMap {
    boolean cc_isChunkTracked(ServerPlayer player, int x, int y, int z);
}
