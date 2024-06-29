package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientChunkCache;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

// TODO rename this, it's used for anything that works with specifically cubes, not just descendants of CubeAccess
@RedirectSet
public interface CubeAccessAndDescendantsSet extends GlobalSet {
    @TypeRedirect(from = @Ref(ChunkAccess.class), to = @Ref(CubeAccess.class))
    abstract class ChunkAccess_to_CubeAccess_redirects {
        @FieldRedirect(@FieldSig(type = @Ref(ChunkPos.class), name = "chunkPos")) protected CloPos cloPos;

        @MethodRedirect(@MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;")) public native CloPos cc_getCloPos();
    }

    @TypeRedirect(from = @Ref(LevelChunk.class), to = @Ref(LevelCube.class))
    abstract class LevelChunk_to_LevelCube_redirects {
        @FieldRedirect(@FieldSig(type = @Ref(ChunkPos.class), name = "chunkPos")) protected CloPos cloPos;

        @MethodRedirect(@MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;")) public native CloPos cc_getCloPos();
    }

    @TypeRedirect(from = @Ref(LevelChunk.PostLoadProcessor.class), to = @Ref(LevelCube.PostLoadProcessor.class))
    interface LevelChunk$PostLoadProcessor_to_LevelCube$PostLoadProcessor_redirects { }

    @TypeRedirect(
        from = @Ref(string = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity"),
        to = @Ref(string = "io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube$BoundTickingBlockEntity")
    )
    abstract class LevelChunk$BoundTickingBlockEntity_to_LevelCube$BoundTickingBlockEntity_redirects { }

    @TypeRedirect(
        from = @Ref(string = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper"),
        to = @Ref(string = "io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube$RebindableTickingBlockEntityWrapper")
    )
    abstract class LevelChunk$RebindableTickingBlockEntityWrapper_to_LevelCube$RebindableTickingBlockEntityWrapper_redirects { }

    @TypeRedirect(from = @Ref(ProtoChunk.class), to = @Ref(ProtoCube.class))
    abstract class ProtoChunk_to_ProtoCube_redirects { }

    @TypeRedirect(from = @Ref(ImposterProtoChunk.class), to = @Ref(ImposterProtoCube.class))
    abstract class ImposterProtoChunk_to_ImposterProtoCube_redirects { }

    @TypeRedirect(
        from = @Ref(ClientChunkCache.Storage.class),
        to = @Ref(CubicClientChunkCache.Storage.class)
    )
    abstract class ClientChunkCache$Storage_to_CubicClientChunkCache$Storage_redirects { }
}
