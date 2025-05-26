package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.gui.screens.worldselection;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.config.CommonConfig;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SwitchGrid;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab", remap = false)
public class MixinCreateWorldScreen {

    @Unique private static final Component CUBIC_CHUNKS = Component.translatable("selectWorld.cubicChunks");

    @Unique private boolean cc_cubicChunks;

    public MixinCreateWorldScreen(boolean ccCubicChunks) {
        cc_cubicChunks = ccCubicChunks;
    }

    @Unique private boolean cc_isCubicChunks() {
        return cc_cubicChunks;
    }

    @Unique private void cc_setCubicChunks(boolean cubicChunks) {
        this.cc_cubicChunks = cubicChunks;
        CommonConfig config = CubicChunks.config();
        config.setGenerateNewWorldsAsCC(cubicChunks);
        config.markDirty();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screens/worldselection/SwitchGrid$Builder;addSwitch(Lnet/minecraft/network/chat/Component;Ljava/util/function/BooleanSupplier;Ljava/util/function/Consumer;)Lnet/minecraft/client/gui/screens/worldselection/SwitchGrid$SwitchBuilder;", ordinal = 1))
    private void cc_addCubicWorldGenButtonToCreateWorldScreen(CreateWorldScreen screen, CallbackInfo ci, @Local SwitchGrid.Builder builder) {
        cc_cubicChunks = CommonConfig.getConfig().shouldGenerateNewWorldsAsCC();
        builder.addSwitch(CUBIC_CHUNKS, this::cc_isCubicChunks, this::cc_setCubicChunks);
    }
}
