package io.github.opencubicchunks.cubicchunks.mixin.core.common.movetoforgesourcesetlater;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Dasm(value = GlobalSet.class, target = @Ref(GenerationChunkHolder.class))
@Mixin(GenerationChunkHolder.class)
public class MixinGenerationChunkHolder_Forge {
    protected CubePos cc_cubePos;
    @Shadow public LevelChunk currentlyLoading;

    // Corresponds to field added by Forge
    @AddFieldToSets(containers = ChunkToCubeSet.GenerationChunkHolder_Forge_Jank_redirects.class, field = "currentlyLoading:Lnet/minecraft/world/level/chunk/LevelChunk;")
    public LevelCube cc_currentlyLoadingCube;

    public LevelClo cc_getCurrentlyLoading() {
        if (cc_cubePos != null) {
            return cc_currentlyLoadingCube;
        }
        return (LevelClo) currentlyLoading;
    }

    public void cc_setCurrentlyLoading(LevelClo clo) {
        if (cc_cubePos != null) {
            cc_currentlyLoadingCube = (LevelCube) clo;
        } else {
            currentlyLoading = (LevelChunk) clo;
        }
    }
}
