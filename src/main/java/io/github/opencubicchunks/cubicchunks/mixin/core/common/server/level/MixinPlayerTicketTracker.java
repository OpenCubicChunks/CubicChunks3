package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.access.common.DistanceManagerAccess;
import io.github.opencubicchunks.cubicchunks.server.level.CloTaskDispatcher;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ThrottlingChunkTaskDispatcher;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DistanceManager.PlayerTicketTracker.class)
public abstract class MixinPlayerTicketTracker extends MixinFixedPlayerDistanceChunkTracker {
    @SuppressWarnings("target")
    @Shadow @Final DistanceManager this$0;

    @Shadow @Final private Long2IntMap queueLevels;

    /**
     * Vanilla propagates player ticket levels out to {@link ChunkMap#MAX_VIEW_DISTANCE} regardless of the actual view distance. In a cubic world
     * the vanilla distance would make every player move between cubes recompute levels for ~65^3 positions. We instead limit propagation to the
     * maximum view distance in cubes.
     */
    @Override public void cc_setCubic() {
        int maxDistance = Coords.sectionToCubeRenderDistance(ChunkMap.MAX_VIEW_DISTANCE);
        this.cc_setMaxDistance(maxDistance);
        this.queueLevels.defaultReturnValue(maxDistance + 2);
        super.cc_setCubic();
    }

    /**
     * This modifies the lambda inside Distance.this.ticketDispatcher.onLevelChange to use a CloPos instead of a ChunkPos.
     */
    @WrapWithCondition(method = "runAllUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ThrottlingChunkTaskDispatcher;onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"))
    private boolean cc_onRunAllUpdates(
            ThrottlingChunkTaskDispatcher instance, ChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer
    ) {
        if (!cc_isCubic) {
            return true;
        }
        ((CloTaskDispatcher) ((DistanceManagerAccess) this$0).cc_ticketDispatcher()).cc_onLevelChange(CloPos.fromLong(chunkPos.toLong()), intSupplier,
                i, intConsumer);
        return false;
    }
}
