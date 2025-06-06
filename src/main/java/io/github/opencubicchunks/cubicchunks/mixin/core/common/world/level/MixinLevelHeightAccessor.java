package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// FIXME temporary hack since cc_core still expects old method names
@Mixin(LevelHeightAccessor.class)
public interface MixinLevelHeightAccessor {
    @Shadow int getMinY();
    @Shadow int getMaxY();
    default int getMinBuildHeight() {
        return getMinY();
    }

    default int getMaxBuildHeight() {
        return getMaxY();
    }
}
