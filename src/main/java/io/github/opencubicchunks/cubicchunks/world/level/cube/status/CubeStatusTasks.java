package io.github.opencubicchunks.cubicchunks.world.level.cube.status;

import java.util.concurrent.CompletableFuture;

import com.mojang.logging.LogUtils;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.status.CCChunkStatusTasks;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

/**
 * Equivalent of {@link ChunkStatusTasks} for cube generation.
 * <p/>
 * See also: {@link CCChunkStatusTasks} for chunk generation tasks in cubic worlds.
 */
@Dasm(ChunkToCubeSet.class)
public class CubeStatusTasks {
    private CubeStatusTasks() {}

    @AddFieldToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, field = "LOGGER:Lorg/slf4j/Logger;")
    private static final Logger LOGGER = LogUtils.getLogger();

    @AddTransformToSets(ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class)
    @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = "isLighted(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z")
    private static native boolean isLighted(CubeAccess cube);

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "passThrough(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> passThrough(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return CompletableFuture.completedFuture(cube);
    }

    // TODO (P3) we skip cube generation steps for now by delegating to passThrough
    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;"
            + "Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)"
            + "Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateStructureStarts(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "loadStructureStarts(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> loadStructureStarts(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateStructureReferences(Lnet/minecraft/world/level/chunk/status/WorldGenContext;"
            + "Lnet/minecraft/world/level/chunk/status/ChunkStep;Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)"
            + "Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateStructureReferences(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateBiomes(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateBiomes(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @SuppressWarnings("checkstyle:MagicNumber") // hardcoded terrain generation; the constants are arbitrary
    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateNoise(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateNoise(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        // Like vanilla's NoiseBasedChunkGenerator.fillFromNoise, fill off the worldgen executor: that executor runs one task at a time, so
        // generating there would serialize all terrain generation
        return CompletableFuture.supplyAsync(() -> {
            LevelChunkSection[] sections = cube.getSections();
            for (LevelChunkSection section : sections) {
                section.acquire();
            }
            try {
                fillSinusoidalTerrain(cube);
            } finally {
                for (LevelChunkSection section : sections) {
                    section.release();
                }
            }
            return cube;
        }, Util.backgroundExecutor().forName("cc_wgen_fill_noise"));
    }

    /**
     * Temporary basic sinusoidal terrain, so we can generate a simple test world.
     * <p/>
     * Writes directly to the cube's sections without locking (as vanilla's {@code doFill} does); the caller must have acquired them.
     */
    @SuppressWarnings("checkstyle:MagicNumber") // hardcoded terrain generation; the constants are arbitrary
    private static void fillSinusoidalTerrain(CubeAccess cube) {
        int amplitude = 20;
        var blockState = Blocks.SMOOTH_STONE.defaultBlockState();
        CubePos cubePos = cube.cc_getCubePos();
        int minY = cubePos.minCubeY();
        int maxY = Math.min(cubePos.maxCubeY(), CubicChunks.SUPERFLAT_HEIGHT + amplitude);
        int cubeX = cubePos.minCubeX();
        int cubeZ = cubePos.minCubeZ();
        for (int x = 0; x < CubicConstants.DIAMETER_IN_BLOCKS; x++) {
            for (int z = 0; z < CubicConstants.DIAMETER_IN_BLOCKS; z++) {
                long offset = Math.round((amplitude * (Math.sin((x + cubeX) / 8.0 + (z + cubeZ) / 21.0) + Math.cos((z + cubeZ) / 13.0))) / 2.0);
                // Solid wherever y + offset <= SUPERFLAT_HEIGHT
                int topY = (int) Math.min(maxY, CubicChunks.SUPERFLAT_HEIGHT - offset);
                for (int y = minY; y <= topY; y++) {
                    cube.getSection(Coords.blockToIndex(x, y, z)).setBlockState(SectionPos.sectionRelative(x), SectionPos.sectionRelative(y),
                            SectionPos.sectionRelative(z), blockState, false);
                }
            }
        }
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateSurface(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateSurface(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateCarvers(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateCarvers(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateFeatures(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateFeatures(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "initializeLight(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> initializeLight(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "light(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> light(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class, method = "generateSpawn(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static CompletableFuture<CubeAccess> generateSpawn(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    ) {
        return passThrough(worldGenContext, step, cache, cube);
    }

    // Upgrade ProtoCube to LevelCube
    // dasm + mixin
    @AddTransformToSets(ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class)
    @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = "full(Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/world/level/chunk/status/ChunkStep;"
            + "Lnet/minecraft/util/StaticCache2D;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;")
    public static native CompletableFuture<CubeAccess> full(
            WorldGenContext worldGenContext, CubeStep step, StaticCache3D<GenerationChunkHolder> cache, CubeAccess cube
    );

    @AddTransformToSets(ChunkToCubeSet.ChunkStatusTasks_to_CubeStatusTasks_redirects.class)
    @TransformFromMethod(owner = @Ref(ChunkStatusTasks.class), value = "postLoadProtoChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/storage/ValueInput$ValueInputList;)V")
    private static native void postLoadProtoCube(ServerLevel level, ValueInput.ValueInputList entityTags);
}
