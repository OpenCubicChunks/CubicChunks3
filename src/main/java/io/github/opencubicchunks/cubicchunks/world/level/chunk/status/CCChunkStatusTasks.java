package io.github.opencubicchunks.cubicchunks.world.level.chunk.status;

import java.util.concurrent.CompletableFuture;

import com.mojang.logging.LogUtils;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkInCubicContextSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStatusTasks;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

/**
 * Equivalent of {@link ChunkStatusTasks} for chunks in cubic worlds.
 * <p/>
 * See also: {@link CubeStatusTasks} for cube generation tasks.
 */
@Dasm(ChunkInCubicContextSet.class)
public class CCChunkStatusTasks {
    private CCChunkStatusTasks() {
    }

    @AddFieldToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class, field = "LOGGER:Lorg/slf4j/Logger;")
    private static final Logger LOGGER = LogUtils.getLogger();

    @AddTransformToSets(ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class)
    @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = "isLighted(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z")
    private static native boolean isLighted(ChunkAccess chunk);

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "passThrough(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> passThrough(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return CompletableFuture.completedFuture(chunk);
    }

    // We skip chunk generation steps in cubic contexts by delegating to passThrough
    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;"
            + "Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)"
            + "Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateStructureStarts(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "loadStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> loadStructureStarts(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateStructureReferences(Lnet/minecraft/world/level/chunk/status/WorldGenContext;"
            + "Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)"
            + "Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateStructureReferences(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateBiomes(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateBiomes(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateNoise(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateNoise(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateSurface(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateSurface(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateCarvers(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateCarvers(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateFeatures(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateFeatures(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "initializeLight(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> initializeLight(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "light(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> light(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "generateSpawn(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<ChunkAccess> generateSpawn(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    // We still want to upgrade ProtoChunks to LevelChunks normally
    @AddTransformToSets(ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class)
    @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class),
        value = "full(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static native CompletableFuture<ChunkAccess> full(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    );

    @AddMethodToSets(containers = ChunkInCubicContextSet.ChunkStatusTasks_to_CCChunkStatusTasks_redirects.class,
        method = "postLoadProtoChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/storage/ValueInput$ValueInputList;)V")
    private static void postLoadProtoChunk(ServerLevel level, ValueInput.ValueInputList entityTags) {
        // Entities should be handled on the cube, not the chunk
    }
}
