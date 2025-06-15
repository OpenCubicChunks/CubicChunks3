package io.github.opencubicchunks.cubicchunks.server.level;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubePyramid;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStep;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Contract;

/**
 * Equivalent to {@link ChunkLevel} for cubes; has methods for determining {@link ChunkStatus}es in a radius around a fully loaded cube.
 */
@Dasm(GlobalSet.class)
public class CubeLevel {
    private CubeLevel() {}

    private static final int FULL_CHUNK_LEVEL = 33;
    private static final CubeStep FULL_CUBE_STEP = CubePyramid.CC_GENERATION_PYRAMID_CUBES.getStepTo(ChunkStatus.FULL);
    @AddFieldToSets(containers = ChunkToCubeSet.ChunkLevel_to_CubeLevel_redirects.class, field = @FieldSig(type = @Ref(int.class), name = "RADIUS_AROUND_FULL_CHUNK"))
    public static final int RADIUS_AROUND_FULL_CUBE = FULL_CUBE_STEP.accumulatedDependencies().getRadius();
    // TODO not sure if this one should actually be redirected? in some cases we want this MAX_LEVEL, in some cases we want the true MAX_LEVEL, which
    // is greater.
    public static final int MAX_LEVEL = FULL_CHUNK_LEVEL + RADIUS_AROUND_FULL_CUBE;

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkLevel_to_CubeLevel_redirects.class, method = @MethodSig("generationStatus(I)Lnet/minecraft/world/level/chunk/status/ChunkStatus;"))
    @Nullable public static ChunkStatus cubeGenerationStatus(int level) {
        return getStatusAroundFullCube(level - FULL_CHUNK_LEVEL, null);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkLevel_to_CubeLevel_redirects.class, method = @MethodSig("getStatusAroundFullChunk(ILnet/minecraft/world/level/chunk/status/ChunkStatus;)Lnet/minecraft/world/level/chunk/status/ChunkStatus;"))
    @Nullable @Contract("_,!null->!null;_,_->_")
    public static ChunkStatus getStatusAroundFullCube(int distance, @Nullable ChunkStatus chunkStatus) {
        if (distance > RADIUS_AROUND_FULL_CUBE) {
            return chunkStatus;
        } else {
            return distance <= 0 ? ChunkStatus.FULL : FULL_CUBE_STEP.accumulatedDependencies().get(distance);
        }
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkLevel_to_CubeLevel_redirects.class, method = @MethodSig("getStatusAroundFullChunk(I)Lnet/minecraft/world/level/chunk/status/ChunkStatus;"))
    public static ChunkStatus getStatusAroundFullCube(int distance) {
        return getStatusAroundFullCube(distance, ChunkStatus.EMPTY);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkLevel_to_CubeLevel_redirects.class, method = @MethodSig("byStatus(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)I"))
    public static int byCubeStatus(ChunkStatus status) {
        return FULL_CHUNK_LEVEL + FULL_CUBE_STEP.getAccumulatedRadiusOf(status);
    }

    @AddMethodToSets(containers = ChunkToCubeSet.ChunkLevel_to_CubeLevel_redirects.class, method = @MethodSig("isLoaded(I)Z"))
    public static boolean isLoadedCube(int level) {
        return level <= MAX_LEVEL;
    }
}
