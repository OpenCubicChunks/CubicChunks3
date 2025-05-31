package io.github.opencubicchunks.cubicchunks.client.renderer.cube;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.renderer.chunk.RenderRegionCache;

// Cubic equivalent to RenderRegionCache$ChunkInfo, since we can't add inner classes with mixin
// Whole class redirect
@TransformFromClass(value = @Ref(string = "net.minecraft.client.renderer.chunk.RenderRegionCache$ChunkInfo"), sets = ChunkToCubeSet.class)
public class RenderRegionCacheCubeInfo {
    public RenderRegionCacheCubeInfo(LevelCube cube) {
        throw new IllegalStateException("DASM failed to apply");
    }

    @AddMethodToSets(owner = @Ref(string = "net.minecraft.client.renderer.chunk.RenderRegionCache$ChunkInfo"), sets = ChunkToCubeSet.class, method = @MethodSig("chunk()Lnet/minecraft/world/level/chunk/LevelChunk;"))
    public native LevelCube cube();

    @AddMethodToSets(owner = @Ref(string = "net.minecraft.client.renderer.chunk.RenderRegionCache$ChunkInfo"), sets = ChunkToCubeSet.class, method = @MethodSig("renderChunk()Lnet/minecraft/client/renderer/chunk/RenderChunk;"))
    public native RenderCube renderCube();
}
