package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;

// Needed to add the ProtoClo interface
@Mixin(ProtoChunk.class)
public abstract class MixinProtoChunk implements ProtoClo {
}
