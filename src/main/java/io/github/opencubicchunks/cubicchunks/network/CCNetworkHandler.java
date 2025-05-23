package io.github.opencubicchunks.cubicchunks.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CCNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        // Sets the current network version
        final IPayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.play(CCClientboundLevelCubeWithLightPacket.ID, new CCClientboundLevelCubeWithLightPacket.Handler(), new CCClientboundLevelCubeWithLightPacket.Handler());
    }
}
