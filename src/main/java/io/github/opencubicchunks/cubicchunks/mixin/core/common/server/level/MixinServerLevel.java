package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;


import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.server.level.CubicServerLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements CubicServerLevel, MarkableAsCubic {
    protected boolean cc_isCubic;

    public MixinServerLevel() {
        super(null, null, null, null, null, false, false, 0, 0);
    }

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override
    public boolean cc_isCubic() {
        return cc_isCubic;
    }

    // TODO: phase 3 - isNaturalSpawningAllowed

    // TODO: phase 3 - invalidateCapabilites, neoforge api

    // TODO: phase 2 - tickCube - new function

    // TODO: phase 4 - setCubeForced - new function

    // TODO: saveDebugReport - mixins, debug only, low priority, if we really really really really need it

    // TODO: phase 2 - isPositionEntityTicking - mixin

}