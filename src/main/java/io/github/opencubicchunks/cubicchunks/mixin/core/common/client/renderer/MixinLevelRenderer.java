package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.renderer.CubicViewArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
    @Shadow @Nullable private ClientLevel level;
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(method = "allChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(DD)V"))
    private void cc_onAllChanged_repositionCamera(ViewArea viewArea, double x, double z, Operation<Void> original, @Local Entity cameraEntity) {
        if (level == null || !((CanBeCubic) level).cc_isCubic()) {
            original.call(viewArea, x, z);
            return;
        }
        ((CubicViewArea) viewArea).cc_repositionCamera(cameraEntity.getX(), cameraEntity.getY(), cameraEntity.getZ());
    }

    @WrapOperation(method = "setupRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(DD)V"))
    private void cc_onSetupRender_repositionCamera(ViewArea viewArea, double x, double z, Operation<Void> original) {
        if (level == null || !((CanBeCubic) level).cc_isCubic()) {
            original.call(viewArea, x, z);
            return;
        }
        ((CubicViewArea) viewArea).cc_repositionCamera(this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ());
    }

    // TODO onChunkLoaded


}
