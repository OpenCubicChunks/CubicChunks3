package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

// FIXME should be in forge sourceset once tests run against forge
@Dasm(GlobalSet.class)
@Mixin(ChunkHolder.class)
public class MixinChunkHolder_Forge {
    // Field added by Forge
    @AddFieldToSets(sets = ChunkToCloSet.class, owner = @Ref(ChunkHolder.class), field = @FieldSig(name = "currentlyLoading", type = @Ref(LevelChunk.class)))
    LevelClo cc_currentlyLoading;

    // getter/setter as a workaround to the field needing to be used as a LevelClo in some places and a LevelCube in others
    LevelCube cc_getCurrentlyLoading() {
        return cc_currentlyLoading instanceof LevelCube ? (LevelCube) cc_currentlyLoading : null;
    }

    void cc_setCurrentlyLoading(LevelCube clo) {
        cc_currentlyLoading = clo;
    }
}
