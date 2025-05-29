package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer.chunk;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.CubicRenderRegionCache;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCube;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCubeRegion;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderRegionCacheCubeInfo;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderRegionCache.class)
public class MixinRenderRegionCache implements CubicRenderRegionCache {
    @Shadow private final Long2ObjectMap<RenderRegionCacheCubeInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

    // TODO can we possibly do this with DASM + mixin? probably not?
    @Override @Nullable public RenderCubeRegion cc_createRegion(Level level, BlockPos start, BlockPos end, int padding, boolean nullForEmpty) {
        var cubicLevel = ((CubicLevel) level);
        int cubeStartX = Coords.blockToCube(start.getX() - padding);
        int cubeStartY = Coords.blockToCube(start.getY() - padding);
        int cubeStartZ = Coords.blockToCube(start.getZ() - padding);
        int cubeEndX = Coords.blockToCube(end.getX() + padding);
        int cubeEndY = Coords.blockToCube(end.getY() + padding);
        int cubeEndZ = Coords.blockToCube(end.getZ() + padding);
        RenderRegionCacheCubeInfo[][][] arenderregioncache$chunkinfo = new RenderRegionCacheCubeInfo[cubeEndX - cubeStartX + 1][cubeEndY - cubeStartY + 1][cubeEndZ - cubeStartZ + 1];

        for(int cubeX = cubeStartX; cubeX <= cubeEndX; ++cubeX) {
            for(int cubeY = cubeStartY; cubeY <= cubeEndY; ++cubeY) {
                for(int cubeZ = cubeStartZ; cubeZ <= cubeEndZ; ++cubeZ) {
                    arenderregioncache$chunkinfo[cubeX - cubeStartX][cubeY - cubeStartY][cubeZ - cubeStartZ] = this.chunkInfoCache
                        .computeIfAbsent(
                            CubePos.asLong(cubeX, cubeY, cubeZ),
                            cubePosLong -> new RenderRegionCacheCubeInfo(cubicLevel.cc_getCube(CubePos.extractX(cubePosLong), CubePos.extractY(cubePosLong), CubePos.extractZ(cubePosLong)))
                        );
                }
            }
        }

        if (nullForEmpty && cc_isAllEmpty(start, end, cubeStartX, cubeStartY, cubeStartZ, arenderregioncache$chunkinfo)) {
            return null;
        } else {
            RenderCube[][][] arenderchunk = new RenderCube[cubeEndX - cubeStartX + 1][cubeEndY - cubeStartY + 1][cubeEndZ - cubeStartZ + 1];

            for(int x = cubeStartX; x <= cubeEndX; ++x) {
                for(int y = cubeStartY; y <= cubeEndY; ++y) {
                    for(int z = cubeStartZ; z <= cubeEndZ; ++z) {
                        arenderchunk[x - cubeStartX][y - cubeStartY][z - cubeStartZ] = arenderregioncache$chunkinfo[x - cubeStartX][y - cubeStartY][z - cubeStartZ].renderChunk();
                    }
                }
            }

            var maxSection = CubicConstants.DIAMETER_IN_SECTIONS - 1;

            var modelDataManager = level.getModelDataManager().snapshotSectionRegion(
                Coords.cubeToSection(cubeStartX, 0), Coords.cubeToSection(cubeStartY, 0), Coords.cubeToSection(cubeStartZ, 0),
                Coords.cubeToSection(cubeEndX, maxSection), Coords.cubeToSection(cubeEndY, maxSection), Coords.cubeToSection(cubeEndZ, maxSection)
            );
            return new RenderCubeRegion(level, cubeStartX, cubeStartY, cubeStartZ, arenderchunk, modelDataManager);
        }
    }

    private static boolean cc_isAllEmpty(BlockPos start, BlockPos end, int startX, int startY, int startZ, RenderRegionCacheCubeInfo[][][] infos) {
        return false;
        // TODO this seems to only be used as an optimization to avoid rendering empty sections so we probably don't need to implement it immediately
        //      instead of using vanilla logic, probably just want to individually check that every section in the bounds is empty
    }
}
