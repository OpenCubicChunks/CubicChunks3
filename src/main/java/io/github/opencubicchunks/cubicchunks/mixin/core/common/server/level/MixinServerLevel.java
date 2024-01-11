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

    @Inject(method = {"isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z"}, at = @At("HEAD"))
    private void cc_onIsNaturalSpawningAllowed(ChunkPos p_201917_, CallbackInfoReturnable<Boolean> cir){
        assert !cc_isCubic;
    }

    @Inject(method = {"invalidateCapabilities(Lnet/minecraft/world/level/ChunkPos;)V"}, at = {@At("HEAD")})
    private void cc_onInvalidateCapabilities(ChunkPos p_201917_, CallbackInfo ci){
        assert !cc_isCubic;
    }

    @Override
    @UsedFromASM
    @TransformFrom("invalidateCapabilities(Lnet/minecraft/world/level/ChunkPos;)V")
    public abstract void invalidateCapabilities(CloPos cloPos);

    @Override
    @UsedFromASM
    @TransformFrom("isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z")
    public abstract boolean isNaturalSpawningAllowed(CloPos cloPos);

    // tickCube - new function

    // setCubeForced - new function

    // saveDebugReport - mixins, debug only, low priority

    // isPositionEntityTicking - mixin

}