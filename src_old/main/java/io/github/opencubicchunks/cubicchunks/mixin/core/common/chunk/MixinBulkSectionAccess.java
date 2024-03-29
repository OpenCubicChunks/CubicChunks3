package io.github.opencubicchunks.cubicchunks.mixin.core.common.chunk;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.levelgen.CubeWorldGenRegion;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BulkSectionAccess.class)
public class MixinBulkSectionAccess {

    @Shadow @Final private LevelAccessor level;

    @Shadow @Nullable private LevelChunkSection lastSection;

    @Shadow private long lastSectionKey;

    @Shadow @Final private Long2ObjectMap<LevelChunkSection> acquiredSections;

    @Inject(method = "getSection", at = @At("HEAD"), cancellable = true)
    private void returnCubeSection(BlockPos blockPos, CallbackInfoReturnable<LevelChunkSection> cir) {
        if (!((CubicLevelHeightAccessor) this.level).isCubic()) {
            return;
        }
        ChunkAccess cube = ((CubeWorldGenRegion) this.level).getCube(blockPos);
        int sectionIdx = Coords.blockToIndex(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if (sectionIdx >= 0 && sectionIdx < CubicConstants.SECTION_COUNT) {
            long sectionLong = SectionPos.asLong(blockPos);
            if (this.lastSection == null || this.lastSectionKey != sectionLong) {
                this.lastSection = this.acquiredSections.computeIfAbsent(sectionLong, (lx) -> {
                    LevelChunkSection section = cube.getSection(sectionIdx);
                    section.acquire();
                    return section;
                });
                this.lastSectionKey = sectionLong;
            }
            cir.setReturnValue(this.lastSection);
        }
    }
}
