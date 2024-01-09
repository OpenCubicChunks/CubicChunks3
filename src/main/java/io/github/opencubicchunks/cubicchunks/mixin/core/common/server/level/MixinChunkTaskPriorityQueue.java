package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTaskPriorityQueue.class)
public abstract class MixinChunkTaskPriorityQueue implements MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Inject(method = "resortChunkTasks", at = @At("HEAD"))
    private void cc_onResortChunkTasks(int p_140522_, ChunkPos p_140523_, int p_140524_, CallbackInfo ci) {
        assert !cc_isCubic;
    }

    @UsedFromASM
    @TransformFrom("resortChunkTasks(ILnet/minecraft/world/level/ChunkPos;I)V")
    protected abstract <T> void resortCubicTasks(int p_140522_, CloPos p_140523_, int p_140524_);
}
