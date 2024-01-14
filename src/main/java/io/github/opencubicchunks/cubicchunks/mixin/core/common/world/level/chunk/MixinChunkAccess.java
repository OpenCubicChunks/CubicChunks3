package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess implements CloAccess {
    private CloPos cc_cloPos;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ChunkPos chunkPos, UpgradeData p_187622_, LevelHeightAccessor p_187623_, Registry p_187624_, long p_187625_, LevelChunkSection[] p_187626_, BlendingData p_187627_,
                        CallbackInfo ci) {
        cc_cloPos = CloPos.chunk(chunkPos);
    }

    public CloPos cc_getCloPos() {
        return cc_cloPos;
    }
}
