package io.github.opencubicchunks.cubicchunks.movetoforgesourcesetlater;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class EventConstructorDelegates {
    public static ChunkEvent.Load create_ChunkEvent$Load(CloAccess cloAccess, boolean newChunk) {
        if (cloAccess instanceof ChunkAccess chunk) {
            return new ChunkEvent.Load(chunk, newChunk);
        } else {
            // Don't attempt to construct ChunkEvent$Load for cubes
            return null;
        }
    }
}
