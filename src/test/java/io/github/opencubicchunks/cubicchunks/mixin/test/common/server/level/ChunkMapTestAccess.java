package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkMapTestAccess {
    @Accessor("visibleChunkMap") Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap();

    @Invoker @Nullable ChunkHolder invokeUpdateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);
}
