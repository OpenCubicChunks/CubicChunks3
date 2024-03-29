package io.github.opencubicchunks.cubicchunks.mixin.core.common.chunk;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.server.level.CubeHolderLevelChangeListener;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkHolder.LevelChangeListener.class)
public interface MixinChunkHolderLevelChangeListener extends CubeHolderLevelChangeListener {

    @Override default void onCubeLevelChange(CubePos pos, IntSupplier intSupplier, int i, IntConsumer consumer) {
    }
}