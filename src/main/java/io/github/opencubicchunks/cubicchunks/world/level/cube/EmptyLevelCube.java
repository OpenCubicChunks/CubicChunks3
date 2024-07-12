package io.github.opencubicchunks.cubicchunks.world.level.cube;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.CubeAccessAndDescendantsSet;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.EmptyLevelChunk;

// Whole class redirect
@TransformFromClass(value = @Ref(EmptyLevelChunk.class), sets = CubeAccessAndDescendantsSet.class)
public class EmptyLevelCube extends LevelCube {
    public EmptyLevelCube(Level level, CloPos pos, Holder<Biome> biome) {
        super(level, pos);
        throw new IllegalStateException("DASM failed to apply");
    }
}
