package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import java.util.concurrent.CompletableFuture;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkHolder.class)
public interface ChunkHolderTestAccess {
    @Dynamic @Invoker CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> invokeCc_GetOrScheduleFuture(ChunkStatus status, ChunkMap map);
}
