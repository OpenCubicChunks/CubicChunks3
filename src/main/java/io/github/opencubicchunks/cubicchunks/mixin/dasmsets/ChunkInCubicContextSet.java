package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.status.CCChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;

@RedirectSet
public interface ChunkInCubicContextSet extends GlobalSet {
    @InterOwnerContainer(from = @Ref(ChunkStatusTasks.class), to = @Ref(CCChunkStatusTasks.class))
    class ChunkStatusTasks_to_CCChunkStatusTasks_redirects {
    }
}
