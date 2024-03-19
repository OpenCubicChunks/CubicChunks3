package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ForgeSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

// FIXME should be in forge sourceset once tests run against forge
@Mixin(ChunkHolder.class)
public class MixinChunkHolder_Forge {
    // Field added by Forge
    @AddFieldToSets(sets = ForgeSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(name = "currentlyLoading", type = @Ref(LevelChunk.class)))
    LevelClo currentlyLoading;
}
