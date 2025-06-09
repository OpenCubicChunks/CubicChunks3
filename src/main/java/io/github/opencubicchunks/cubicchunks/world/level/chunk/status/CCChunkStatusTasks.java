package io.github.opencubicchunks.cubicchunks.world.level.chunk.status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkInCubicContextSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStatusTasks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;

/**
 * Equivalent of {@link ChunkStatusTasks} for chunks in cubic worlds.
 * <p/>
 * See also: {@link CubeStatusTasks} for cube generation tasks.
 */
@Dasm(ChunkInCubicContextSet.class)
public class CCChunkStatusTasks {
    @AddTransformToSets(ChunkInCubicContextSet.class) @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = @MethodSig("isLighted(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"))
    private static native boolean isLighted(ChunkAccess chunk);

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("passThrough(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> passThrough(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return CompletableFuture.completedFuture(chunk);
    }

    // We skip chunk generation steps in cubic contexts by delegating to passThrough
    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))// We skip chunk generation steps in cubic contexts by delegating to passThrough
    public static CompletableFuture<ChunkAccess> generateStructureStarts(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("loadStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> loadStructureStarts(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateStructureReferences(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateStructureReferences(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateBiomes(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateBiomes(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateNoise(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateNoise(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateSurface(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateSurface(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateCarvers(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateCarvers(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateFeatures(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateFeatures(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("initializeLight(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> initializeLight(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("light(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> light(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateSpawn(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<ChunkAccess> generateSpawn(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    ) {
        return passThrough(worldGenContext, step, cache, chunk);
    }

    // We still want to upgrade ProtoChunks to LevelChunks normally
    @AddTransformToSets(ChunkInCubicContextSet.class) @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = @MethodSig("full(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static native CompletableFuture<ChunkAccess> full(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk
    );

    @AddMethodToSets(sets = ChunkInCubicContextSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("postLoadProtoChunk(Lnet/minecraft/server/level/ServerLevel;Ljava/util/List;)V"))
    private static void postLoadProtoChunk(ServerLevel level, List<CompoundTag> entityTags) {
        // Entities should be handled on the cube, not the chunk
    }
}
