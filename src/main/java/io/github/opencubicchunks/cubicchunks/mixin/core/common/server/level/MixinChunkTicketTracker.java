package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DistanceManager.ChunkTicketTracker.class)
public abstract class MixinChunkTicketTracker extends MixinChunkTracker {
    @Inject(method = "setLevel", at = @At("HEAD"))
    private void cc_onSetLevel(long sectionPos, int level, CallbackInfo ci) {
        super.cc_onSetLevel(sectionPos, level);
    }
    // TODO if/when we replace ChunkHolder with a cubic equivalent we'll need mixins here
}
