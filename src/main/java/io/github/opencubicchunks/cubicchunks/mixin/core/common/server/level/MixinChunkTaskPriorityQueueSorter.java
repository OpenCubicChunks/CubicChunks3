package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;


import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTaskPriorityQueueSorter;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTaskPriorityQueueSorter.class)
public abstract class MixinChunkTaskPriorityQueueSorter implements CubicTaskPriorityQueueSorter {
    protected boolean cc_isCubic;

    @WrapOperation(method = "getDebugStatus", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private Stream<String> cc_replaceChunkPosInDebugStatus(Stream<Long> instance, Function<? super Long, ? extends String> function, Operation<Stream<String>> original) {
        if (!cc_isCubic) return original.call(instance, function);
        return instance.map(CloPos::fromLong).map(CloPos::toString);
    }

    @Inject(method = "onLevelChange", at = @At("HEAD"))
    private void cc_onChunkOnLevelChange(ChunkPos p_140616_, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_, CallbackInfo ci) {
        assert !cc_isCubic;
    }

    @Override
    @UsedFromASM
    @TransformFrom("onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V")
    public abstract <T> void onLevelChange(CloPos p_140616_, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_);
}
