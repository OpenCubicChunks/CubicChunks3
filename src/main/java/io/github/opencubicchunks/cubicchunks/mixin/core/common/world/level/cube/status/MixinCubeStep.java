package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.cube.status;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.status.CubeStep;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Needed for DASM to apply
@Mixin(CubeStep.class)
public class MixinCubeStep {
    @Dynamic
    @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onChunkGenerate(Lio/github/opencubicchunks/cc_core/api/CubePos;Lnet/minecraft/resources/ResourceKey;Ljava/lang/String;)Lnet/minecraft/util/profiling/jfr/callback/ProfiledDuration;"))
    private ProfiledDuration cc_onApply_profilerCall(JvmProfiler instance, CubePos cubePos, ResourceKey<Level> levelResourceKey, String s) {
        // TODO we just hack out the profiler stuff for now
        return null;
    }
}
