package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.concurrent.CompletableFuture;

import com.mojang.datafixers.util.Either;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface ServerCubeCache extends CubeSource {
    CompletableFuture<Either<CubeAccess, ChunkHolder.ChunkLoadingFailure>> cc_getCubeFuture(
        int pX, int pY, int pZ, ChunkStatus pChunkStatus, boolean pLoad
    );

    void cc_blockChanged(BlockPos pos);
    void cc_onLightUpdate(LightLayer pType, SectionPos pPos);

    // Stored on this interface since we can't add inner records in mixins
    record CloAndHolder(LevelClo chunk, ChunkHolder holder) {}
}
