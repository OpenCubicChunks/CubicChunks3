package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer.chunk;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.CubicRenderRegionCache;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCube;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderCubeRegion;
import io.github.opencubicchunks.cubicchunks.client.renderer.cube.RenderRegionCacheCubeInfo;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderRegionCache.class)
public abstract class MixinRenderRegionCache implements CubicRenderRegionCache {
    @Shadow private final Long2ObjectMap<RenderRegionCacheCubeInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

    // TODO can we possibly do this with DASM + mixin? probably not?
    @Override @Nullable public RenderCubeRegion cc_createRegion(Level level, SectionPos sectionPos, boolean nullForEmpty) {
        int centerCubeX = Coords.sectionToCube(sectionPos.getX());
        int centerCubeY = Coords.sectionToCube(sectionPos.getY());
        int centerCubeZ = Coords.sectionToCube(sectionPos.getZ());
        var centerCubeInfo = this.cc_getChunkInfo(level, centerCubeX, centerCubeY, centerCubeZ);
//        if (nullForEmpty && centerCubeInfo.cube().isSectionEmpty(sectionPos.y())) { // TODO need a proper isSectionEmpty on CubeAccess
//            return null;
//        }
        int cubeStartX = centerCubeX - 1;
        int cubeStartY = centerCubeY - 1;
        int cubeStartZ = centerCubeZ - 1;
        int cubeEndX = centerCubeX + 1;
        int cubeEndY = centerCubeY + 1;
        int cubeEndZ = centerCubeZ + 1;
        RenderCube[] renderCubes = new RenderCube[27];

        for(int cubeX = cubeStartX; cubeX <= cubeEndX; ++cubeX) {
            for(int cubeY = cubeStartY; cubeY <= cubeEndY; ++cubeY) {
                for(int cubeZ = cubeStartZ; cubeZ <= cubeEndZ; ++cubeZ) {
                    int cubeIndex = RenderCubeRegion.index(cubeStartX, cubeStartY, cubeStartZ, cubeX, cubeY, cubeZ);
                    var cubeInfo = cubeX == centerCubeX && cubeY == centerCubeY && cubeZ == centerCubeZ
                        ? centerCubeInfo
                        : this.cc_getChunkInfo(level, cubeX, cubeY, cubeZ);
                    renderCubes[cubeIndex] = cubeInfo.renderCube();
                }
            }
        }

        var modelDataManager = level.getModelDataManager().snapshotSectionRegion(cubeStartX, cubeStartY, cubeStartZ, cubeEndX, cubeEndY, cubeEndZ);
        return new RenderCubeRegion(level, cubeStartX, cubeStartY, cubeStartZ, renderCubes, modelDataManager);
    }

    private RenderRegionCacheCubeInfo cc_getChunkInfo(Level level, int cubeX, int cubeY, int cubeZ) {
        return this.chunkInfoCache
            .computeIfAbsent(
                CubePos.asLong(cubeX, cubeY, cubeZ),
                cubePosLong -> new RenderRegionCacheCubeInfo(((CubicLevel) level).cc_getCube(CubePos.extractX(cubePosLong), CubePos.extractY(cubePosLong), CubePos.extractZ(cubePosLong)))
            );
    }
}
