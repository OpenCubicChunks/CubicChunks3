package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelReader;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;

// Cube equivalents of LevelReader chunk methods are implemented as defaults in CubicLevelReader
@Mixin(LevelReader.class)
public interface MixinLevelReader extends CubicLevelReader {}
