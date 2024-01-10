package io.github.opencubicchunks.cubicchunks.mixin.core.common.server;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() { cc_isCubic = true;}

}
