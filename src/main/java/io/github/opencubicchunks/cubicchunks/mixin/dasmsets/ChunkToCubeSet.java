package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCube;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderRegionCacheCubeInfo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.EmptyLevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ImposterProtoCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

/**
 * Should be used for DASM transforms that work with only Cubes (as opposed to working with both Chunks and Cubes)
 * <br/><br/>
 * Cube-related field and type redirects, and method redirects containing Cube-related types in the signature or return type should be added to this set.
 * <br/>
 * Other redirects may also be added to this set if they should only be applied in contexts working with only Cubes.
 * Redirects applicable in all contexts should be added to {@link GlobalSet}.
 */
@RedirectSet
public interface ChunkToCubeSet extends GlobalSet {
    @TypeRedirect(from = @Ref(ChunkPos.class), to = @Ref(CubePos.class))
    abstract class ChunkPos_to_CubePos_redirects {
        @FieldRedirect(@FieldSig(type = @Ref(long.class), name = "INVALID_CHUNK_POS"))
        static final long INVALID_CUBE_POS = Long.MAX_VALUE;

        @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "x"))
        native int getX();

        @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "z"))
        native int getZ();

        @MethodRedirect(@MethodSig("toLong()J"))
        native long asLong();

        // Dummy methods that throw errors; these should be manually redirected to the correct x,y,z methods using mixin.
        // (See: MixinCubePos)
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(int.class), @Ref(int.class) }))
        static native CubePos dummy_fromChunkCoords(int x, int z);

        @MethodRedirect(@MethodSig("asLong(II)J"))
        static native long dummy_chunkAsLong(int x, int z);
    }

    @TypeRedirect(from = @Ref(ChunkAccess.class), to = @Ref(CubeAccess.class))
    abstract class ChunkAccess_to_CubeAccess_redirects {
        @FieldRedirect(@FieldSig(type = @Ref(ChunkPos.class), name = "chunkPos")) protected CubePos cubePos;

        @MethodRedirect(@MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;")) public native CubePos cc_getCubePos();
    }

    @TypeRedirect(from = @Ref(LevelChunk.class), to = @Ref(LevelCube.class))
    abstract class LevelChunk_to_LevelCube_redirects {
        @FieldRedirect(@FieldSig(type = @Ref(ChunkPos.class), name = "chunkPos")) protected CubePos cubePos;

        @MethodRedirect(@MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;")) public native CubePos cc_getCubePos();
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

    @TypeRedirect(from = @Ref(EmptyLevelChunk.class), to = @Ref(EmptyLevelCube.class))
    abstract class EmptyLevelChunk_to_EmptyLevelCube_redirects { }

    // FIXME probably need to move to a client-only set
    @TypeRedirect(from = @Ref(RenderChunk.class), to = @Ref(RenderCube.class))
    abstract class RenderChunk_to_RenderCube_redirects { }

    @TypeRedirect(from = @Ref(string = "net.minecraft.client.renderer.chunk.RenderRegionCache$ChunkInfo"), to = @Ref(RenderRegionCacheCubeInfo.class))
    abstract class RenderRegionCache$ChunkInfo_to_RenderRegionCacheCubeInfo_redirects { }

    @TypeRedirect(
        from = @Ref(ClientChunkCache.Storage.class),
        to = @Ref(ClientCubeCache.Storage.class)
    )
    abstract class ClientChunkCache$Storage_to_ClientCubeCache$Storage_redirects { }

    @TypeRedirect(from = @Ref(LevelChunk.UnsavedListener.class), to = @Ref(LevelCube.UnsavedListener.class))
    interface LevelChunk$UnsavedListener_to_LevelCube$UnsavedListener_redirects { }

    // TODO move to a forge-specific sourceset
    // getter/setter as a workaround to forge adding a field that needs to be used as a LevelClo in some places and a LevelCube in others
    @IntraOwnerContainer(owner = @Ref(ChunkHolder.class))
    abstract class ChunkHolder_Forge_Jank_redirects {
        @FieldToMethodRedirect(value = @FieldSig(name = "currentlyLoading", type = @Ref(LevelChunk.class)), setter = "cc_setCurrentlyLoading")
        native LevelCube cc_getCurrentlyLoading();
    }
}
