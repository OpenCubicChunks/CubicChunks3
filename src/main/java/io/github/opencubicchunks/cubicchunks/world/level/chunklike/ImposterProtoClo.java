package io.github.opencubicchunks.cubicchunks.world.level.chunklike;

import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;

public interface ImposterProtoClo extends ProtoClo {
    static ImposterProtoClo create(LevelClo wrapped, boolean allowWrites) {
        if (wrapped instanceof LevelCube cube) {
            return new ImposterProtoCube(cube, allowWrites);
        } else {
            return (ImposterProtoClo) new ImposterProtoChunk(((LevelChunk) wrapped), allowWrites);
        }
    }

    LevelClo cc_getWrappedClo();
}
