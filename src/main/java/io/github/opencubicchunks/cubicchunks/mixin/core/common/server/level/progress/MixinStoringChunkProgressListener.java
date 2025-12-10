package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.progress;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.server.level.progress.CloProgressListener;
import io.github.opencubicchunks.cubicchunks.server.level.progress.StoringCloProgressListener;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Dasm(value = ChunkToCloSet.class, target = @Ref(StoringChunkProgressListener.class))
@Mixin(StoringChunkProgressListener.class)
public abstract class MixinStoringChunkProgressListener implements CloProgressListener, StoringCloProgressListener {
    @Shadow @Final private Long2ObjectOpenHashMap<ChunkStatus> statuses;
    @Shadow @Final private int radius;
    @AddFieldToSets(containers = GlobalSet.StoringChunkProgressListener_redirects.class, field = "spawnPos:Lnet/minecraft/world/level/ChunkPos;")
    private CloPos cc_spawnPos;

    @AddTransformToSets(ChunkToCloSet.StoringChunkProgressListener_redirects.class)
    @TransformFromMethod("updateSpawnPos(Lnet/minecraft/world/level/ChunkPos;)V")
    @Override public native void cc_updateSpawnPos(CloPos center);

    @AddTransformToSets(ChunkToCloSet.StoringChunkProgressListener_redirects.class)
    @TransformFromMethod("onStatusChange(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V")
    @Override public native void cc_onStatusChange(CloPos chunkPosition, @Nullable ChunkStatus newStatus);

    @Override @Nullable public ChunkStatus cc_getStatus(int cubeX, int cubeY, int cubeZ) {
        int cubeRadius = Coords.sectionToCubeCeil(radius);
        var spawnPos = cc_spawnPos != null && cc_spawnPos.isCube() ? cc_spawnPos.cubePos() : CubePos.ZERO;
        return this.statuses.get(
            CubePos.asLong(cubeX + spawnPos.getX() - cubeRadius, cubeY + spawnPos.getY() - cubeRadius, cubeZ + spawnPos.getZ() - cubeRadius));
    }
}
