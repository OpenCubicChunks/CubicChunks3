package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer.chunk;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.exception.DasmFailedToApply;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The vanilla {@link SectionCopy} stores block data of a {@link LevelChunkSection} and is used for rendering that section.
 * We simply add a constructor that creates the {@code SectionCopy} from a cube instead of a chunk.
 */
@Dasm(value = ChunkToCubeSet.class, target = @Ref(SectionCopy.class))
@Mixin(SectionCopy.class)
public class MixinSectionCopy {
    // TODO is this field on the vanilla class actually used anywhere? based on snowblower output not having it, it seems like neoforge is adding it?
    @AddFieldToSets(containers = ChunkToCubeSet.SectionCopy_redirects.class, field = @FieldSig(type = @Ref(LevelChunk.class), name = "wrapped"))
    final LevelCube cc_wrapped;

    @AddTransformToSets(ChunkToCubeSet.SectionCopy_redirects.class)
    @TransformFromMethod(@MethodSig("<init>(Lnet/minecraft/world/level/chunk/LevelChunk;I)V"))
    public MixinSectionCopy(LevelCube wrapped, int sectionIndex) {
        throw new DasmFailedToApply();
    }
}
