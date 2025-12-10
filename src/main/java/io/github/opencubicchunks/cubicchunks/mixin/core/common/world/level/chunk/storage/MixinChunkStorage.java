package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.storage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(value = ChunkToCloSet.class, target = @Ref(ChunkStorage.class))
@Mixin(ChunkStorage.class)
public abstract class MixinChunkStorage {
    @AddMethodToSets(containers = ChunkToCloSet.ChunkStorage_redirects.class, method = "isOldChunkAround(Lnet/minecraft/world/level/ChunkPos;I)Z")
    public boolean cc_isOldChunkAround(CloPos pos, int radius) {
        return false; // TODO (P2) should be dasm'd once IOWorker is done
    }

    @AddMethodToSets(containers = ChunkToCloSet.ChunkStorage_redirects.class, method = "read(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;")
    public CompletableFuture<Optional<CompoundTag>> cc_read(CloPos cloPos) {
        // TODO (P2) loading - this method should be dasm'd
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @AddMethodToSets(containers = ChunkToCloSet.ChunkStorage_redirects.class, method = "write(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;")
    public void cc_write(CloPos cloPos, CompoundTag chunkData) {
        // TODO (P2) loading/unloading
    }

    // TODO (P2) chunkScanner() cubic version once IOWorker is done
}
