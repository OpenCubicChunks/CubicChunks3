package io.github.opencubicchunks.cubicchunks.client.renderer.chunk;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.util.asm.FactoryFromConstructor;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.renderer.chunk.SectionCopy;

@Dasm(ChunkToCubeSet.class)
public interface CCSectionCopy {
    // Factory method for constructor that's added to SectionCopy by DASM
    @FactoryFromConstructor
    static SectionCopy cc_create(LevelCube wrapped, int sectionIndex) {
        throw new IllegalStateException("FactoryFromConstructor failed to apply");
    }
}
