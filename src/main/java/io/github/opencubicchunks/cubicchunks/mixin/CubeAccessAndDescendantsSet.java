package io.github.opencubicchunks.cubicchunks.mixin;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import io.github.opencubicchunks.dasm.api.Ref;
import io.github.opencubicchunks.dasm.api.redirect.DasmRedirectSet;
import io.github.opencubicchunks.dasm.api.redirect.FieldRedirect;
import io.github.opencubicchunks.dasm.api.redirect.MethodRedirect;
import io.github.opencubicchunks.dasm.api.redirect.TypeRedirect;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

@DasmRedirectSet
public interface CubeAccessAndDescendantsSet extends GeneralSet {
    @TypeRedirect(from = @Ref(ChunkAccess.class), to = @Ref(CubeAccess.class))
    abstract class ChunkAccessToCubeAccessRedirects {
        @FieldRedirect("cloPos") protected ChunkPos chunkPos;

        @MethodRedirect("cc_getCloPos") public native ChunkPos getPos();
    }

    @TypeRedirect(from = @Ref(LevelChunk.class), to = @Ref(LevelCube.class))
    abstract class LevelChunkToLevelCubeRedirects { }

    @TypeRedirect(
        from = @Ref(string = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity"),
        to = @Ref(string = "io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube$BoundTickingBlockEntity")
    )
    abstract class LevelChunk$BoundTickingBlockEntityToLevelCube$BoundTickingBlockEntityRedirects { }

    @TypeRedirect(
        from = @Ref(string = "net.minecraft.world.level.chunk.LevelChunk$PostLoadProcessor"),
        to = @Ref(string = "io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube$PostLoadProcessor")
    )
    abstract class LevelChunk$PostLoadProcessorToLevelCube$PostLoadProcessorRedirects { }

    @TypeRedirect(
        from = @Ref(string = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper"),
        to = @Ref(string = "io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube$RebindableTickingBlockEntityWrapper")
    )
    abstract class LevelChunk$RebindableTickingBlockEntityWrapperToLevelCube$RebindableTickingBlockEntityWrapperRedirects { }

    @TypeRedirect(from = @Ref(ProtoChunk.class), to = @Ref(ProtoCube.class))
    abstract class ProtoChunkToProtoCubeRedirects { }

    @TypeRedirect(from = @Ref(ImposterProtoChunk.class), to = @Ref(ImposterProtoCube.class))
    abstract class ImposterProtoChunkToImposterProtoCubeRedirects { }
}
