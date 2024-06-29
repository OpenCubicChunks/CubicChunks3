package io.github.opencubicchunks.cubicchunks.mixin.test.client.multiplayer;

import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientChunkCache;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkCache.class)
public interface ClientChunkCacheTestAccess {
    @Dynamic @Accessor("cc_cubeStorage") CubicClientChunkCache.Storage cubeStorage();
    @Dynamic @Accessor("cc_emptyCube") LevelCube emptyCube();
    @Accessor("storage") ClientChunkCache.Storage chunkStorage();
}
