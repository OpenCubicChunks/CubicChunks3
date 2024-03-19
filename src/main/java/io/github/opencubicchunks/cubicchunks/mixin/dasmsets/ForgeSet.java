package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.movetoforgesourcesetlater.EventConstructorDelegates;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.event.level.ChunkEvent;

// TODO once redirect sets can be applied conditionally, this should be in the forge sourceset and GlobalSet should no longer extend it
@RedirectSet
public interface ForgeSet {
    @InterOwnerContainer(owner = @Ref(ChunkEvent.Load.class), newOwner = @Ref(EventConstructorDelegates.class))
    abstract class ChunkEvent$Load_delegateConstruction {
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(ChunkAccess.class), @Ref(boolean.class) }))
        static native ChunkEvent.Load create_ChunkEvent$Load(CloAccess cloAccess, boolean newChunk);
    }
}
