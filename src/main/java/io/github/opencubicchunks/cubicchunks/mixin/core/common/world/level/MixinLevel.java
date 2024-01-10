package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public class MixinLevel implements CubicLevel, MarkableAsCubic {
    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() { cc_isCubic = true;}

    // getCubeAt - new function

    // getCube (LevelChunk) - new function

    // getCube (ChunkAccess) - new function

    // setBlock - mixin

    // markAndNotifyBlock - maybe?

    // getBlockState - mixin

    // getBlockEntity - mixin

    // setBlockEntity - mixin

    // removeBlockEntity - mixin

    // isLoaded - mixin

    // loadedAndEntityCanStandOnFace - mixin

    // getChunkForCollisions - mixin

    // blockEntityChanged - mixin

    // getCurrentDifficultyAt - mixin

    // TODO: Phase 3 low priority: Add a method to modify isOutsideSpawnableHeight to go from -30 mil to 30 mil, not -20 mil to 20 mil
}
