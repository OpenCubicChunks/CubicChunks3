package io.github.opencubicchunks.cubicchunks.mixin;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.dasm.api.Ref;
import io.github.opencubicchunks.dasm.api.redirect.DasmRedirectSet;
import io.github.opencubicchunks.dasm.api.redirect.MethodRedirect;
import io.github.opencubicchunks.dasm.api.redirect.TypeRedirect;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

@DasmRedirectSet
public interface GeneralSet {
    @TypeRedirect(from = @Ref(ChunkPos.class), to = @Ref(CloPos.class))
    abstract class ChunkPosToCloPosRedirects { }

    @TypeRedirect(from = @Ref(ChunkAccess.class), to = @Ref(CloAccess.class))
    abstract class ChunkAccessToCloAccessRedirects {
        @MethodRedirect("cc_getCloPos")
        public native ChunkPos getPos();
    }

    @TypeRedirect(from = @Ref(LevelChunk.class), to = @Ref(LevelClo.class))
    abstract class LevelChunkToLevelCloRedirects { }
}
