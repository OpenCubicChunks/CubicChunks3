package io.github.opencubicchunks.cubicchunks.mixin.test.common.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.mockito.Mockito;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerTest {
    @Shadow
    @SuppressWarnings("unused")
    public ServerGamePacketListenerImpl connection = Mockito.mock();
}
