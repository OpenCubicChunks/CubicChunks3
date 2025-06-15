package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.opencubicchunks.cc_core.api.CubePos;
import net.minecraft.server.level.ServerPlayer;

public interface CubeHolder {
    @FunctionalInterface
    interface LevelChangeListener {
        void cc_onLevelChange(CubePos cubePos, IntSupplier queueLevelGetter, int ticketLevel, IntConsumer queueLevelSetter);
    }

    interface PlayerProvider {
        /**
         * Returns the players tracking the given cube.
         */
        List<ServerPlayer> cc_getPlayers(CubePos pos, boolean boundaryOnly);
    }
}
