package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GenerationChunkHolder.class)
public interface GenerationChunkHolderTestAccess {
    @Invoker void invokeUpdateHighestAllowedStatus(ChunkMap chunkMap);
}
