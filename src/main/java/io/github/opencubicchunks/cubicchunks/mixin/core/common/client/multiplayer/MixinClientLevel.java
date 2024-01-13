package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.multiplayer;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public class MixinClientLevel implements CubicClientLevel, MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() { cc_isCubic = true;}

    // unload
    // TODO: Phase 2 - this interacts with the lighting engine and will need to change to support cubes

    // onCubeLoaded
    // TODO: Phase 3 - we will need to interact with entityStorage correctly at a per-cube level
}
