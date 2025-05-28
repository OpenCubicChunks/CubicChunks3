package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;


// !!! Please minimize the amount of packets that we create until we move to 1.21 !!!
// This is because NeoForge's network is significantly different (and way better!) in 1.21 and beyond

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CCNetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        // Sets the current network version
        final IPayloadRegistrar registrar = event.registrar(CubicChunks.MODID);

        registrar.play(CCClientboundLevelCubeWithLightPacket.ID, CCClientboundLevelCubeWithLightPacket::new, new CCClientboundLevelCubeWithLightPacket.Handler());
        registrar.play(CCClientboundLevelChunkPacket.ID, CCClientboundLevelChunkPacket::new, new CCClientboundLevelChunkPacket.Handler());
        registrar.play(CCClientboundForgetLevelCloPacket.ID, CCClientboundForgetLevelCloPacket::new, new CCClientboundForgetLevelCloPacket.Handler());
        registrar.play(CCClientboundSetCubeCacheCenterPacket.ID, CCClientboundSetCubeCacheCenterPacket::new, new CCClientboundSetCubeCacheCenterPacket.Handler());
    }
}
