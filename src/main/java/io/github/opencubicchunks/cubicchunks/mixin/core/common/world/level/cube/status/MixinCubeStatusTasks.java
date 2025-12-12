package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.cube.status;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.util.StaticCache3D;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CubeStatusTasks.class)
public class MixinCubeStatusTasks {
    @Dynamic @Redirect(method = "full", at = @At(value = "INVOKE", target = "Lio/github/opencubicchunks/cubicchunks/util/StaticCache3D;get(II)Ljava/lang/Object;"))
    private static Object onFullCube_cacheGet(StaticCache3D<?> instance, int x, int z, @Local(ordinal = 0) CubePos cubePos) {
        return instance.get(x, cubePos.getY(), z);
    }

    @Dynamic @Redirect(method = "dasm$redirect$lambda$full$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/status/WorldGenContext;unsavedListener()Lio/github/opencubicchunks/cubicchunks/world/level/cube/LevelCube$UnsavedListener;"))
    private static LevelCube.UnsavedListener onFullCube_worldGenContext_unsavedListener(WorldGenContext instance) {
        return cubePos -> {}; // TODO (P2) save/load: this is temporary until WorldGenContext actually has a proper redirect so we can get a cube
                              // unsaved listener
    }
}
