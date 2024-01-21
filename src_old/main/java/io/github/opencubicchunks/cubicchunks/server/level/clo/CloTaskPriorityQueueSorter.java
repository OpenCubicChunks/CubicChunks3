package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;

public class CloTaskPriorityQueueSorter implements AutoCloseable, CubeHolderLevelChangeListener {

    public CloTaskPriorityQueueSorter(List<ProcessorHandle<?>> taskExecutors, Executor executor, int maxTasks) {
        throw new Error("ASM didn't apply");
    }

    public static Message<Runnable> message(Runnable runnable, long pos, IntSupplier intSupplier) {
        throw new Error("ASM didn't apply");
    }

    public static Message<Runnable> message(ChunkHolder holder, Runnable runnable) {
        throw new Error("ASM didn't apply");
    }

    public static Release release(Runnable runnable, long pos, boolean flag) {
        throw new Error("ASM didn't apply");
    }

    public <T> ProcessorHandle<Message<T>> getProcessor(ProcessorHandle<T> processorHandle, boolean flag) {
        throw new Error("ASM didn't apply");
    }

    // func_219091_a, getReleaseProcessor
    public ProcessorHandle<Release> getReleaseProcessor(ProcessorHandle<Runnable> processorHandle) {
        throw new Error("ASM didn't apply");
    }

    // func_219066_a, onLevelChange
    @Override
    public void onCloLevelChange(CloPos pos, IntSupplier getLevel, int level, IntConsumer setLevel) {
        throw new Error("ASM didn't apply");
    }

    @Override public void close() {
        throw new Error("ASM didn't apply");
    }

    public static final class Message<T> {
        final Function<ProcessorHandle<Unit>, T> task;
        final long pos;
        final IntSupplier level;

        Message(Function<ProcessorHandle<Unit>, T> function, long l, IntSupplier intSupplier) {
            this.task = function;
            this.pos = l;
            this.level = intSupplier;
        }
    }

    public static final class Release {
        final Runnable task;
        final long pos;
        final boolean clearQueue;

        Release(Runnable runnable, long l, boolean bl) {
            this.task = runnable;
            this.pos = l;
            this.clearQueue = bl;
        }
    }
}