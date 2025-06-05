package io.github.opencubicchunks.cubicchunks.mixin.core.client.multiplayer;

import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import org.spongepowered.asm.mixin.Mixin;

// Needed for DASM to apply
@Mixin(ClientCubeCache.Storage.class)
public class MixinClientCubeCache$Storage {
}
