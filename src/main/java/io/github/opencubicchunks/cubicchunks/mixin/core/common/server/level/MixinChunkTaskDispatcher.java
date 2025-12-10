package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.CloTaskDispatcher;
import io.github.opencubicchunks.cubicchunks.server.level.CubeHolder;
import net.minecraft.server.level.ChunkTaskDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(value = ChunkToCloSet.class, target = @Ref(ChunkTaskDispatcher.class))
@Mixin(ChunkTaskDispatcher.class)
public class MixinChunkTaskDispatcher implements CubeHolder.LevelChangeListener, CloTaskDispatcher {
    @AddTransformToSets(ChunkToCloSet.ChunkTaskDispatcher_redirects.class)
    @TransformFromMethod(owner = @Ref(ChunkTaskDispatcher.class),
        value = "onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V")
    @Override public native void cc_onLevelChange(CloPos cloPos, IntSupplier queueLevelGetter, int ticketLevel, IntConsumer queueLevelSetter);

    public void cc_onLevelChange(CubePos cubePos, IntSupplier queueLevelGetter, int ticketLevel, IntConsumer queueLevelSetter) {
        cc_onLevelChange(CloPos.cube(cubePos), queueLevelGetter, ticketLevel, queueLevelSetter);
    }
}
