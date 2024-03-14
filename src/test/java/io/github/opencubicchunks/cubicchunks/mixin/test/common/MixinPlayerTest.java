package io.github.opencubicchunks.cubicchunks.mixin.test.common;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayerTest {
    @Inject(method = "createAttributes", at = @At(value = "HEAD"), cancellable = true)
    private static void cubic_chunks_3$fixNeoForgeErrors(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(AttributeSupplier.builder());
    }
}
