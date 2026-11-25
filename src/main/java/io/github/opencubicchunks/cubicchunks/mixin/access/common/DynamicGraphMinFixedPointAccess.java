package io.github.opencubicchunks.cubicchunks.mixin.access.common;

import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import net.minecraft.world.level.lighting.LeveledPriorityQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DynamicGraphMinFixedPoint.class)
public interface DynamicGraphMinFixedPointAccess {
    @Mutable @Accessor("levelCount") void cc_setLevelCount(int levelCount);

    @Mutable @Accessor("priorityQueue") void cc_setPriorityQueue(LeveledPriorityQueue priorityQueue);
}
