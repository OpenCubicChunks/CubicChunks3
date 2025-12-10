package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.client.renderer.CubicLevelRenderer;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(value = ChunkToCubeSet.class, target = @Ref(LevelRenderer.class))
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements CubicLevelRenderer {
    @AddTransformToSets(ChunkToCubeSet.LevelRenderer_redirects.class)
    @TransformFromMethod("onChunkReadyToRender(Lnet/minecraft/world/level/ChunkPos;)V")
    public native void cc_onCubeReadyToRender(CubePos cubePos);
}
