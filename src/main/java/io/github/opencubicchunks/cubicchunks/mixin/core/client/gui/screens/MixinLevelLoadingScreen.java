package io.github.opencubicchunks.cubicchunks.mixin.core.client.gui.screens;

import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.gui.screens.CubicLevelLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class MixinLevelLoadingScreen extends Screen {
    protected MixinLevelLoadingScreen() {
        super(null);
    }

    @Inject(method = "renderChunks", at = @At("HEAD"), cancellable = true)
    private static void cc_onRenderChunks(
            GuiGraphics guiGraphics, StoringChunkProgressListener progressListener, int centerX, int centerY, int size, int spacing, CallbackInfo ci
    ) {
        // TODO probably should check if overworld will be cubic before `level` is instantiated,
        // otherwise there's a flash of the vanilla loading screen before we start rendering the CC one
        Level level = Minecraft.getInstance().getSingleplayerServer().overworld();
        if (level == null || !((CanBeCubic) level).cc_isCubic()) {
            return;
        }

        ci.cancel();
        CubicLevelLoadingScreen.doRender(guiGraphics, progressListener);
    }
}
