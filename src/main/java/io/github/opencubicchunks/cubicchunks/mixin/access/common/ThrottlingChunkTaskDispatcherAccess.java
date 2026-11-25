package io.github.opencubicchunks.cubicchunks.mixin.access.common;

import net.minecraft.server.level.ThrottlingChunkTaskDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThrottlingChunkTaskDispatcher.class)
public interface ThrottlingChunkTaskDispatcherAccess {
    @Mutable @Accessor("maxChunksInExecution") void cc_setMaxChunksInExecution(int maxChunksInExecution);
}
