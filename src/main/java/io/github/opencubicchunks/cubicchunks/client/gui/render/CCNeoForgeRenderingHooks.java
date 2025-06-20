package io.github.opencubicchunks.cubicchunks.client.gui.render;

import io.github.opencubicchunks.cubicchunks.client.gui.render.pip.WorldLoadingCubeStatusesRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class CCNeoForgeRenderingHooks {
    private CCNeoForgeRenderingHooks() {}

    @SubscribeEvent
    public static void registerPipRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(WorldLoadingCubeStatusesRenderer::new);
    }
}
