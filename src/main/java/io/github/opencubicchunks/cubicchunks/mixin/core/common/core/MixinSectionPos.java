package io.github.opencubicchunks.cubicchunks.mixin.core.common.core;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SectionPos.class)
public abstract class MixinSectionPos {
    @Shadow public abstract ChunkPos chunk();

    /**
     * Replaces {@link SectionPos}.chunk() in dasm-copied code in cases where a cube position should be returned, rather than a chunk position.
     * @return The position of the cube containing this section, as a {@link CloPos}
     */
    public CloPos cc_cube() {
        return CloPos.section((SectionPos) (Object) this);
    }

    /**
     * Replaces {@link SectionPos}.chunk() in dasm-copied code in cases where a chunk position should still be returned, not a cube position.
     * @return The position of the chunk containing this section, as a {@link CloPos}
     */
    public CloPos cc_chunk() {
        return CloPos.chunk(this.chunk());
    }
}
