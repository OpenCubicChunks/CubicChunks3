package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.cube;

import io.github.opencubicchunks.cubicchunks.world.level.cube.EmptyLevelCube;
import org.spongepowered.asm.mixin.Mixin;

// Needed for DASM to apply
@Mixin(EmptyLevelCube.class)
public abstract class MixinEmptyLevelCube extends MixinLevelCube {
}
