package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.lighting;

import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelLightEngine.class)
public class MixinLevelLightEngine {
    @Shadow @Final protected LevelHeightAccessor levelHeightAccessor;

    // TODO (P2) lighting

    @Inject(method = "lightOnInSection", at = @At("HEAD"), cancellable = true)
    private void cc_onLightOnInSection(SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
        if (((CanBeCubic) this.levelHeightAccessor).cc_isCubic()) cir.setReturnValue(true);
    }

    @Inject(method = "getRawBrightness", at = @At("HEAD"), cancellable = true)
    private void cc_onGetRawBrightness(BlockPos blockPos, int amount, CallbackInfoReturnable<Integer> cir) {
        if (((CanBeCubic) this.levelHeightAccessor).cc_isCubic()) cir.setReturnValue(15);
    }
}
