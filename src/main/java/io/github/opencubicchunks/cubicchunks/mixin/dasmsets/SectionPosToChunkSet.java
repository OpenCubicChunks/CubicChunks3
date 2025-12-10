package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import net.minecraft.core.SectionPos;

/**
 * Replaces {@link SectionPos}.chunk() in dasm-copied code in cases where a chunk position should still be returned, not a cube position.
 */
@RedirectSet
public interface SectionPosToChunkSet {
    @IntraOwnerContainer(@Ref(SectionPos.class))
    abstract class SectionPos_redirects {
        @MethodRedirect("chunk()Lnet/minecraft/world/level/ChunkPos;")
        public native CloPos cc_chunk();
    }
}
