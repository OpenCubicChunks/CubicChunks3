package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;

@Dasm(ChunkToCubeSet.class)
public class CubeStatusTasks {
    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = @MethodSig("isLighted(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"))
    private static native boolean isLighted(CubeAccess cube);

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("passThrough(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> passThrough(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
//        return CompletableFuture.completedFuture(cube);
        return CompletableFuture.supplyAsync(() -> cube);
    }

    // TODO (P3) we skip cube generation steps for now by delegating to passThrough
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))// We skip chunk generation steps in cubic contexts by delegating to passThrough
    public static CompletableFuture<CubeAccess> generateStructureStarts(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("loadStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> loadStructureStarts(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateStructureReferences(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateStructureReferences(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateBiomes(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateBiomes(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateNoise(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateNoise(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        // Temporary basic sinusoidal terrain, so we can generate a simple test world
        int amplitude = 20;
        var blockPos = new BlockPos.MutableBlockPos();
        var blockState = Blocks.SMOOTH_STONE.defaultBlockState();
        int minY = cube.cc_getCubePos().minCubeY();
        int maxY = Math.min(cube.cc_getCubePos().maxCubeY(), CubicChunks.SUPERFLAT_HEIGHT + amplitude);
        int cubeX = cube.cc_getCubePos().minCubeX();
        int cubeZ = cube.cc_getCubePos().minCubeZ();
        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < CubicConstants.DIAMETER_IN_BLOCKS; x++) {
                for (int z = 0; z < CubicConstants.DIAMETER_IN_BLOCKS; z++) {
                    if (y + Math.round((amplitude*(Math.sin((x+cubeX)/8.0+(z+cubeZ)/21.0)+Math.cos((z+cubeZ)/13.0)))/2.0) <= CubicChunks.SUPERFLAT_HEIGHT)
                        cube.setBlockState(blockPos.set(x, y, z), blockState);
                }
            }
        }
        return CompletableFuture.completedFuture(cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateSurface(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateSurface(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateCarvers(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateCarvers(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateFeatures(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateFeatures(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("initializeLight(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> initializeLight(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("light(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> light(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(ChunkStatusTasks.class), method = @MethodSig("generateSpawn(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static CompletableFuture<CubeAccess> generateSpawn(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    // Upgrade ProtoCube to LevelCube
    // dasm + mixin
    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = @MethodSig("full(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;"))
    public static native CompletableFuture<CubeAccess> full(
        WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    );

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = @MethodSig("postLoadProtoChunk(Lnet/minecraft/server/level/ServerLevel;Ljava/util/List;)V"))
    private static native void postLoadProtoCube(ServerLevel level, List<CompoundTag> entityTags);
}
