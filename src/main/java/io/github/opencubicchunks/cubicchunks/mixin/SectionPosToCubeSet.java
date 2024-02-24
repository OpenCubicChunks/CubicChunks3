package io.github.opencubicchunks.cubicchunks.mixin;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.SectionPos;

@RedirectSet
public interface SectionPosToCubeSet {
    @IntraOwnerContainer(owner = @Ref(SectionPos.class))
    abstract class SectionPos_redirects {
        @MethodRedirect(@MethodSig("chunk()Lnet/minecraft/world/level/ChunkPos;"))
        public native CloPos cc_cube();
    }
}
