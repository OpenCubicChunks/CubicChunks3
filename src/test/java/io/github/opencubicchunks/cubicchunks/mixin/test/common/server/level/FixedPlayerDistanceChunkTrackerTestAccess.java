package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DistanceManager.FixedPlayerDistanceChunkTracker.class)
public interface FixedPlayerDistanceChunkTrackerTestAccess {
    @Accessor(value = "chunks") Long2ByteMap get_chunks();
}
