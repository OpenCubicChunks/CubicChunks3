package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.multiplayer;

import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;

// Needed for DASM to apply
@Mixin(CubicClientChunkCache.Storage.class)
public class MixinCubicClientChunkCache$Storage {
}
