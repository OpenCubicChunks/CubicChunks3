package io.github.opencubicchunks.cubicchunks.movetoforgesourcesetlater;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.level.ChunkEvent;

// In DASM-copied code we redirect forge event construction to factory methods on this class, allowing for easier control (e.g. not firing events for cubic equivalents of vanilla things)
public class EventConstructorDelegates {
    public static Event create_ChunkEvent$Load(CloAccess cloAccess, boolean newChunk) {
        if (cloAccess instanceof ChunkAccess chunk) {
            return new ChunkEvent.Load(chunk, newChunk);
        } else {
            // Don't attempt to construct ChunkEvent$Load for cubes
            return new DummyEvent();
        }
    }

    public static Event create_ChunkEvent$Unload(CloAccess cloAccess) {
        if (cloAccess instanceof ChunkAccess chunk) {
            return new ChunkEvent.Unload(chunk);
        } else {
            // Don't attempt to construct ChunkEvent$Unload for cubes
            return new DummyEvent();
        }
    }

    // TODO (P4) we should eventually have CC equivalents for all events
    // We need this because NeoForge crashes if we pass a null event
    static class DummyEvent extends Event {}
}
