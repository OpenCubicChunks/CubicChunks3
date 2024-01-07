package io.github.opencubicchunks.cubicchunks.mixin.access.common;

import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DistanceManager.class)
public interface DistanceManagerAccess {
    @Accessor("ticketThrottler") ChunkTaskPriorityQueueSorter ticketThrottler();
}
