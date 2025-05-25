package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer.chunk;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCube;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCubeRegion;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderRegionCacheCubeInfo;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderRegionCache.class)
public class MixinRenderRegionCache {
    @Shadow private final Long2ObjectMap<RenderRegionCacheCubeInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

    // TODO can we possibly do this with DASM + mixin? probably not?
    @Nullable public RenderCubeRegion cc_createRegion(Level level, BlockPos start, BlockPos end, int padding, boolean nullForEmpty) {
        var cubicLevel = ((CubicLevel) level);
        int startX = SectionPos.blockToSectionCoord(start.getX() - padding);
        int startY = SectionPos.blockToSectionCoord(start.getY() - padding);
        int startZ = SectionPos.blockToSectionCoord(start.getZ() - padding);
        int endX = SectionPos.blockToSectionCoord(end.getX() + padding);
        int endY = SectionPos.blockToSectionCoord(end.getY() + padding);
        int endZ = SectionPos.blockToSectionCoord(end.getZ() + padding);
        RenderRegionCacheCubeInfo[][][] arenderregioncache$chunkinfo = new RenderRegionCacheCubeInfo[endX - startX + 1][endY - startY + 1][endZ - startZ + 1];

        for(int x = startX; x <= endX; ++x) {
            for(int y = startX; y <= endY; ++y) {
                for(int z = startZ; z <= endZ; ++z) {
                    arenderregioncache$chunkinfo[x - startX][y - startY][z - startZ] = this.chunkInfoCache
                        .computeIfAbsent(
                            CubePos.asLong(x, y, z),
                            cubePosLong -> new RenderRegionCacheCubeInfo(cubicLevel.cc_getCube(CubePos.extractX(cubePosLong), CubePos.extractY(cubePosLong), CubePos.extractZ(cubePosLong)))
                        );
                }
            }
        }

        if (nullForEmpty && cc_isAllEmpty(start, end, startX, startY, startZ, arenderregioncache$chunkinfo)) {
            return null;
        } else {
            RenderCube[][][] arenderchunk = new RenderCube[endX - startX + 1][endY - startY + 1][endZ - startZ + 1];

            for(int x = startX; x <= endX; ++x) {
                for(int y = startY; y <= endY; ++y) {
                    for(int z = startZ; z <= endZ; ++z) {
                        arenderchunk[x - startX][y - startY][z - startZ] = arenderregioncache$chunkinfo[x - startX][y - startY][z - startZ].renderChunk();
                    }
                }
            }

            var modelDataManager = level.getModelDataManager().snapshotSectionRegion(startX, startY, startZ, endX, endY, endZ);
            return new RenderCubeRegion(level, startX, startY, startZ, arenderchunk, modelDataManager);
        }
    }

    private static boolean cc_isAllEmpty(BlockPos start, BlockPos end, int startX, int startY, int startZ, RenderRegionCacheCubeInfo[][][] infos) {
        return false;
        // TODO this seems to only be used as an optimization to avoid rendering empty sections so we probably don't need to implement it immediately
        //      instead of using vanilla logic, probably just want to individually check that every section in the bounds is empty
    }
}
