package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheTestAccess {
    @Accessor
    ServerChunkCache.MainThreadExecutor getMainThreadProcessor();
}
