package io.github.opencubicchunks.cubicchunks.mixin;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

@RedirectSet
public interface CubeAccessAndDescendantsSet extends GeneralSet {
    @TypeRedirect(from = @Ref(ChunkAccess.class), to = @Ref(CubeAccess.class))
    abstract class ChunkAccessToCubeAccessRedirects {
        @FieldRedirect(@FieldSig(type = @Ref(ChunkPos.class), name = "chunkPos")) protected CloPos cloPos;

        @MethodRedirect(@MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;")) public native CloPos cc_getCloPos();
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
