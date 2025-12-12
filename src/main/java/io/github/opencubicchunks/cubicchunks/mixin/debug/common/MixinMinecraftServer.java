package io.github.opencubicchunks.cubicchunks.mixin.debug.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.BooleanSupplier;

import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.minecraft.server.MinecraftServer;
import one.profiler.AsyncProfiler;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Unique private static final boolean perf_doProfiling = System.getProperty("cubicchunks.debug.profiler", "false").equals("true");

    @Unique private static final Path perf_basePath = Path.of(System.getProperty("cubicchunks.debug.profiler.path", "profiler"));
    @Unique private static final int perf_interval = Integer.parseInt(System.getProperty("cubicchunks.debug.profiler.interval_ns", "1"));

    @Unique private static final AsyncProfiler perf_PROFILER = AsyncProfiler.getInstance();
    @Unique private static boolean perf_started = false;
    @Unique private static int perf_idx = 0;

    @Unique private static long perf_tickStartTime = 0;

    static {
        try {
            FileUtils.deleteDirectory(perf_basePath.toFile());
        } catch (IOException e) {
            CubicChunks.LOGGER.error("[tick-profiler] Failed to clear existing output directory {}", perf_basePath, e);
        }
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!perf_doProfiling) {
            return;
        }

        perf_stopProfiler();

        // Start a new profiler
        Path path = perf_basePath.resolve(perf_idx++ + ".jfr").toAbsolutePath();
        try {
            Files.createDirectories(path.getParent());
            perf_PROFILER.execute("start,jfr,interval=" + perf_interval + ",event=cpu,file=" + path.toAbsolutePath());
        } catch (IOException e) {
            CubicChunks.LOGGER.error("[tick-profiler] Failed to start async-profiler", e);
        }
        perf_started = true;
        perf_tickStartTime = System.nanoTime();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        perf_stopProfiler();
    }

    @Unique private static void perf_stopProfiler() {
        if (perf_started) {
            // Stop the currently running profiler
            try {
                perf_PROFILER.execute("stop");
            } catch (IOException e) {
                CubicChunks.LOGGER.error("[tick-profiler] Failed to stop async-profiler", e);
            }
            perf_started = false;

            // Rename the just stopped profiler output file to include its duration for easy sorting
            int previousIndex = perf_idx - 1;
            Duration duration = Duration.ofNanos(System.nanoTime() - perf_tickStartTime);
            try {
                Files.move(perf_basePath.resolve(previousIndex + ".jfr"), perf_basePath.resolve(duration.toMillis() + "_" + previousIndex + ".jfr"));
            } catch (IOException e) {
                CubicChunks.LOGGER.error("[tick-profiler] Failed to rename jfr file to include tick duration", e);
            }
        }
    }

}
