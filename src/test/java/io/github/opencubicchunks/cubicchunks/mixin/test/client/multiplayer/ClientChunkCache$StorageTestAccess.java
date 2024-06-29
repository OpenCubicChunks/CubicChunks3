package io.github.opencubicchunks.cubicchunks.mixin.test.client.multiplayer;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientChunkCache.Storage.class)
public interface ClientChunkCache$StorageTestAccess {
    @Invoker("inRange") boolean invokeInRange(int pX, int pZ);
}
