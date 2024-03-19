package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GeneralSet;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTaskPriorityQueueSorter;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Dasm(GeneralSet.class)
@Mixin(ChunkTaskPriorityQueueSorter.class)
public abstract class MixinChunkTaskPriorityQueueSorter implements CubicTaskPriorityQueueSorter, MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
    }

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        @MethodSig("message(Lnet/minecraft/server/level/ChunkHolder;Ljava/lang/Runnable;)Lnet/minecraft/server/level/ChunkTaskPriorityQueueSorter$Message;"))
    private static native ChunkTaskPriorityQueueSorter.Message<Runnable> cc_message(ChunkHolder pChunkHolder, Runnable pTask);

    @AddTransformToSets(GeneralSet.class) @TransformFromMethod(
        @MethodSig("message(Lnet/minecraft/server/level/ChunkHolder;Ljava/util/function/Function;)Lnet/minecraft/server/level/ChunkTaskPriorityQueueSorter$Message;"))
    private static native <T> ChunkTaskPriorityQueueSorter.Message<T> cc_message(ChunkHolder pChunkHolder, Function<ProcessorHandle<Unit>, T> pTask);

    /**
     * This is a method that is only used for debugging, so we don't currently test it.
     */
    @WrapOperation(method = "getDebugStatus", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private Stream<String> cc_replaceChunkPosInDebugStatus(Stream<Long> instance, Function<? super Long, ? extends String> function, Operation<Stream<String>> original) {
        if (!cc_isCubic) return original.call(instance, function);
        return instance.map(CloPos::fromLong).map(CloPos::toString);
    }

    @Inject(method = "onLevelChange", at = @At("HEAD"))
    private void cc_onChunkOnLevelChange(ChunkPos chunkPos, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_, CallbackInfo ci) {
        assert !cc_isCubic;
    }

    @Override
    @UsedFromASM
    @TransformFromMethod(@MethodSig("onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
    public abstract <T> void onLevelChange(CloPos cloPos, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_);
}
