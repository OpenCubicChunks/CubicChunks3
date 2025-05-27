package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.Arrays;
import java.util.Collections;
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

    private AtomicInteger index = new AtomicInteger();

    private final Either<CloAccess, ChunkHolder.ChunkLoadingFailure>[] results;
    // Vanilla expects that the center chunk is in the middle of the list; this is not the case for cubes, so we manually swap the center cube to the middle
    private AtomicInteger indexToBeSwappedWithCenterIndex = new AtomicInteger(-1);

    public CloCollectorFuture(int size) {
        this.size = size;
        results = new Either[size];
    }

    public void add(Either<CloAccess, ChunkHolder.ChunkLoadingFailure> either, @Nullable Throwable error, boolean isCenterCube) {
        if (error != null) {
            completeExceptionally(error);
        } else {
            int i = index.getAndIncrement();
            if (isCenterCube) {
                int oldValue = indexToBeSwappedWithCenterIndex.getAndSet(i);
                if (oldValue != -1) {
                    throw new IllegalStateException("Tried to set center cube when center cube was already set");
                }
            }
            results[i] = either;
        }

        if (index.get() >= size) {
            done();
        }
    }

    private void done() {
        int i = indexToBeSwappedWithCenterIndex.get();
        if (i == -1) {
            throw new IllegalStateException("All Clos were received but no center cube was set");
        }
        int j = results.length / 2;
        var results = Arrays.asList(this.results);
        Collections.swap(results, i, j);
        this.complete(results);
    }
}
