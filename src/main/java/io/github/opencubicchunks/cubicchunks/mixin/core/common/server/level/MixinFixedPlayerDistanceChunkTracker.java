package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.opencubicchunks.cubicchunks.mixin.access.common.DynamicGraphMinFixedPointAccess;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.lighting.LeveledPriorityQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DistanceManager.FixedPlayerDistanceChunkTracker.class)
public abstract class MixinFixedPlayerDistanceChunkTracker extends MixinChunkTracker {
    // Matches the width passed to the DynamicGraphMinFixedPoint constructor by FixedPlayerDistanceChunkTracker.
    // TODO: Investigate performance trade-offs of a non-vanilla min-bound?
    private static final int CC_PRIORITY_QUEUE_EXPECTED_SIZE = 16;

    @Shadow @Final protected Long2ByteMap chunks;
    @Shadow @Final @Mutable protected int maxDistance;

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void cc_onSetLevel(long sectionPos, int level, CallbackInfo ci) {
        super.cc_onSetLevel(sectionPos, level);
    }

    /**
     * Changes the maximum distance levels are propagated to, updating all state that the vanilla constructor derives from it.
     * <p/>
     * Must be called before the tracker is used, and before {@link MixinChunkTracker#cc_setCubic()} (which depends on {@code levelCount}).
     */
    protected void cc_setMaxDistance(int maxDistance) {
        if (!this.chunks.isEmpty() || this.hasWork()) {
            throw new IllegalStateException("Cannot change the max distance of a FixedPlayerDistanceChunkTracker that is already in use");
        }
        int levelCount = maxDistance + 2;
        ((DynamicGraphMinFixedPointAccess) this).cc_setLevelCount(levelCount);
        ((DynamicGraphMinFixedPointAccess) this).cc_setPriorityQueue(new LeveledPriorityQueue(levelCount, CC_PRIORITY_QUEUE_EXPECTED_SIZE));
        this.maxDistance = maxDistance;
        this.chunks.defaultReturnValue((byte) levelCount);
    }
}
