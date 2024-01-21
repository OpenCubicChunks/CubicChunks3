package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ImposterProtoClo;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ImposterProtoChunk.class)
public abstract class MixinImposterProtoChunk implements ImposterProtoClo {
    @Override public LevelClo cc_getWrappedClo() {
        return (LevelClo) this.getWrapped();
    }

    @Shadow public abstract LevelChunk getWrapped();
}
