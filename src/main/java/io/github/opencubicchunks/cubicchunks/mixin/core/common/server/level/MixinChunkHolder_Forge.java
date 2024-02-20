package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;

// FIXME should be in forge sourceset once tests run against forge
@Mixin(ChunkHolder.class)
public class MixinChunkHolder_Forge {
    // Field added by Forge
    LevelClo currentlyLoading;
}
