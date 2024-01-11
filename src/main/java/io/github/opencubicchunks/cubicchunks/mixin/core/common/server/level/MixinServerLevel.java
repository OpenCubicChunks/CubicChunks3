package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;


import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.server.level.CubicServerLevel;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    // isNaturalSpawningAllowed - mixin? new function?

    // invalidateCapabilites - phase 3, neoforge api

    // tickCube - new function

    // setCubeForced - new function

    // saveDebugReport - mixins, debug only, low priority

    // isPositionEntityTicking - mixin

}