package io.github.opencubicchunks.cubicchunks.mixin.core.common.core;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SectionPos.class)
public abstract class MixinSectionPos {
    @Shadow public abstract ChunkPos chunk();

    public CloPos cc_cube() {
        return CloPos.section((SectionPos) (Object) this);
    }

    public CloPos cc_chunk() {
        return CloPos.chunk(this.chunk());
    }
}
