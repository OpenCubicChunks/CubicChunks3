package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.server.level.ChunkHolder;

/**
 * A future for loading a list of {@link CloAccess}es, that relies on being externally notified of CloAccesses being loaded, rather than depending on futures for each CloAccess.
 * <p>
 * The future completes once every CloAccess has been added.
 */
public class CloCollectorFuture extends CompletableFuture<List<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>>> {
    private final int size;

    private AtomicInteger added = new AtomicInteger();

    private final Either<CloAccess, ChunkHolder.ChunkLoadingFailure>[] results;

    public CloCollectorFuture(int size) {
        this.size = size;
        results = new Either[size];
    }

    public void add(int idx, Either<CloAccess, ChunkHolder.ChunkLoadingFailure> either, @Nullable Throwable error) {
        if (error != null) {
            completeExceptionally(error);
        } else {
            results[idx] = either;
            added.getAndIncrement();
        }

        if (added.get() >= size) {
            this.complete(Arrays.asList(results));
        }
    }
}
