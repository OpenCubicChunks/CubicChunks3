package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import java.util.concurrent.CompletableFuture;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkHolder.class)
public interface ChunkHolderTestAccess {
    @Dynamic @Invoker CompletableFuture<ChunkResult<CloAccess>> invokeCc_GetOrScheduleFuture(ChunkStatus status, ChunkMap map);
}
