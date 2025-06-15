package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Dasm(ChunkToCloSet.class)
@Mixin(ChunkTaskPriorityQueue.class)
public abstract class MixinChunkTaskPriorityQueue implements MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
    }

    @Inject(method = "resortChunkTasks", at = @At("HEAD"))
    private void cc_onResortChunkTasks(int queueLevel, ChunkPos chunkPos, int ticketLevel, CallbackInfo ci) {
        assert !cc_isCubic;
    }

    @UsedFromASM
    @AddTransformToSets(ChunkToCloSet.ChunkTaskPriorityQueue_redirects.class)
    @TransformFromMethod(@MethodSig("resortChunkTasks(ILnet/minecraft/world/level/ChunkPos;I)V"))
    public native void cc_resortCubicTasks(int queueLevel, CloPos cloPos, int ticketLevel);
}
