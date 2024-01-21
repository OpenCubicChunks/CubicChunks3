package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk implements LevelClo {
}
