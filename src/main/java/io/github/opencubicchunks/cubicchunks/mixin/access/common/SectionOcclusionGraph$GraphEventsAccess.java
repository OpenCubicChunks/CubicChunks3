package io.github.opencubicchunks.cubicchunks.mixin.access.common;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionOcclusionGraph.GraphEvents.class)
public interface SectionOcclusionGraph$GraphEventsAccess {
    @Accessor("chunksWhichReceivedNeighbors") LongSet cc_chunksWhichReceivedNeighbors();
}
