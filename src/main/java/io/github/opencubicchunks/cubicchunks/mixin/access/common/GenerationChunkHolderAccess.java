package io.github.opencubicchunks.cubicchunks.mixin.access.common;

import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.GenerationChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GenerationChunkHolder.class)
public interface GenerationChunkHolderAccess {
    @Invoker("removeTask") void cc_invokeRemoveTask(ChunkGenerationTask task);
}
