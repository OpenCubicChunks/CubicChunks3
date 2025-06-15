package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.level.ChunkTracker;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkTracker.class)
public interface ChunkTrackerTestAccess {
    @Dynamic
    @Invoker(remap = false, value = "cc_onSetLevel")
    void invoke_cc_onSetLevel(long pos, int level);

    @Dynamic
    @Accessor(remap = false, value = "cc_existingCubesForCubeColumns")
    Long2ObjectMap<IntSet> get_cc_existingCubesForCubeColumns();
}
