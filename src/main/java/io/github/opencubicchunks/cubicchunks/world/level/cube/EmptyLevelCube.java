package io.github.opencubicchunks.cubicchunks.world.level.cube;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.CubeAccessAndDescendantsSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.EmptyLevelChunk;

// Whole class redirect
@TransformFromClass(value = @Ref(EmptyLevelChunk.class), sets = CubeAccessAndDescendantsSet.class)
public class EmptyLevelCube extends LevelCube {
    public EmptyLevelCube(Level pLevel, CloPos pPos, Holder<Biome> pBiome) {
        super(pLevel, pPos);
        throw new IllegalStateException("DASM failed to apply");
    }
}
