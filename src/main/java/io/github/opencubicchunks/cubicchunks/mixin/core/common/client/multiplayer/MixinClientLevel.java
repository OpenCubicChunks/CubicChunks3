package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.multiplayer;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.ClientCubeCache;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientLevel;
import io.github.opencubicchunks.cubicchunks.client.renderer.CubicLevelRenderer;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.MixinLevel;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends MixinLevel implements CubicClientLevel {
    @Shadow @Final private ClientChunkCache chunkSource;
    @Shadow @Final private LevelRenderer levelRenderer;

    @Override public boolean cc_hasCube(int cubeX, int cubeY, int cubeZ) {
        return true;
    }

    // TODO should eventually be DASM once BlockTintCache and entity storage are actually done in CC
    public void cc_onCubeLoaded(CubePos cubePos) {
//        this.tintCaches.forEach((p_194154_, p_194155_) -> p_194155_.invalidateForChunk(chunkPos.x, chunkPos.z));
//        this.entityStorage.startTicking(chunkPos);
        ((CubicLevelRenderer) this.levelRenderer).cc_onCubeLoaded(cubePos);
    }

    @Override public ClientCubeCache cc_getCubeSource() {
        return ((ClientCubeCache) this.chunkSource);
    }

    // TODO: comments below don't account for 1.20.4->1.21.5 changes; will need to check for other methods that need CC changes

    // unload
    // TODO: Phase 2 - this interacts with the lighting engine and will need to change to support cubes

    // onCubeLoaded
    // TODO: Phase 3 - we will need to interact with entityStorage correctly at a per-cube level
}
