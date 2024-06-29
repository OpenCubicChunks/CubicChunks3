package io.github.opencubicchunks.cubicchunks.test.client.multiplayer;

import static io.github.opencubicchunks.cc_core.utils.Coords.cubeToSection;
import static io.github.opencubicchunks.cubicchunks.testutils.Misc.assertDeepEquals;
import static io.github.opencubicchunks.cubicchunks.testutils.Misc.generateRandomLevelCube;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientChunkCache;
import io.github.opencubicchunks.cubicchunks.mixin.test.client.multiplayer.ClientChunkCache$StorageTestAccess;
import io.github.opencubicchunks.cubicchunks.mixin.test.client.multiplayer.ClientChunkCacheTestAccess;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelCubeWithLightPacket;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCubicClientChunkCache extends BaseTest {
    @Test public void basicTests() {
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);
        var clientChunkCache = ((CubicClientChunkCache) new ClientChunkCache(clientLevelMock, 10));
        var storage = ((ClientChunkCacheTestAccess) clientChunkCache).cubeStorage();
        var emptyCube = ((ClientChunkCacheTestAccess) clientChunkCache).emptyCube();

        var pos1 = CloPos.cube(1, 2, 3);
        var pos2 = CloPos.cube(-3, -4, 1);
        var pos3 = CloPos.cube(0, 0, 0);
        var index1 = storage.getIndex(pos1.getX(), pos1.getY(), pos1.getZ());
        var index2 = storage.getIndex(pos2.getX(), pos2.getY(), pos2.getZ());
        var index3 = storage.getIndex(pos3.getX(), pos3.getY(), pos3.getZ());

        var cube1 = new LevelCube(clientLevelMock, pos1);
        var cube4 = new LevelCube(clientLevelMock, pos1);

        var cube2 = new LevelCube(clientLevelMock, pos2);
        var cube3 = new LevelCube(clientLevelMock, pos3);

        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(0);
        // Getting unloaded cubes
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isNull();
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), true)).isSameAs(emptyCube);

        // Getting loaded cubes
        storage.replace(index1, cube1);
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(1);
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isSameAs(cube1);
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), true)).isSameAs(emptyCube);

        storage.replace(index2, cube2);
        storage.replace(index3, cube3);
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(3);
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isSameAs(cube1);
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), false)).isSameAs(cube2);
        assertThat(clientChunkCache.cc_getCube(pos3.getX(), pos3.getY(), pos3.getZ(), false)).isSameAs(cube3);

        // Replacing loaded cubes
        storage.replace(index1, cube4);
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(3);
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isSameAs(cube4);
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), false)).isSameAs(cube2);
        assertThat(clientChunkCache.cc_getCube(pos3.getX(), pos3.getY(), pos3.getZ(), false)).isSameAs(cube3);

        // Changing center and radius
        clientChunkCache.cc_updateViewCenter(1, 2, 3);
        clientChunkCache.cc_updateViewRadius(11);
        // this results in different indices and storage object
        storage = ((ClientChunkCacheTestAccess) clientChunkCache).cubeStorage();
        index1 = storage.getIndex(pos1.getX(), pos1.getY(), pos1.getZ());
        index2 = storage.getIndex(pos2.getX(), pos2.getY(), pos2.getZ());
        index3 = storage.getIndex(pos3.getX(), pos3.getY(), pos3.getZ());

        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(3);
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isSameAs(cube4);
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), false)).isSameAs(cube2);
        assertThat(clientChunkCache.cc_getCube(pos3.getX(), pos3.getY(), pos3.getZ(), false)).isSameAs(cube3);

        // Unloading cubes
        storage.replace(index3, null);
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(2);
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isSameAs(cube4);
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), false)).isSameAs(cube2);
        assertThat(clientChunkCache.cc_getCube(pos3.getX(), pos3.getY(), pos3.getZ(), false)).isNull();
        assertThat(clientChunkCache.cc_getCube(pos3.getX(), pos3.getY(), pos3.getZ(), true)).isSameAs(emptyCube);

        // Unloading cubes via cc_drop
        clientChunkCache.cc_drop(pos1.cubePos());
        clientChunkCache.cc_drop(pos2.cubePos());
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(0);
        assertThat(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false)).isNull();
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), true)).isSameAs(emptyCube);
    }

    // TODO should this one be under integration tests instead?
    // TODO disabled until we can do tests with registries (currently errors due to registries being mocked)
    @Disabled @Test
    public void receiveCubePacketTest() {
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);
        var clientChunkCache = ((CubicClientChunkCache) new ClientChunkCache(clientLevelMock, 10));
        var emptyCube = ((ClientChunkCacheTestAccess) clientChunkCache).emptyCube();

        var pos1 = CloPos.cube(-3, -2, -3);
        var pos2 = CloPos.cube(-3, -1, -3);
        var cube1 = new LevelCube(clientLevelMock, pos1);
        Random random = new Random(203);
        var cube2 = generateRandomLevelCube(clientLevelMock, pos2, random);
        var cube3 = generateRandomLevelCube(clientLevelMock, pos1, random);

        var packet1 = new CCClientboundLevelCubeWithLightPacket(cube1);
        var packet2 = new CCClientboundLevelCubeWithLightPacket(cube2);
        var packet3 = new CCClientboundLevelCubeWithLightPacket(cube3);

        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(0);
        // Load first cube
        // TODO (P2) don't pass in dummy args (tag and consumer) once heightmaps etc. are implemented
        clientChunkCache.cc_replaceWithPacketData(pos1.getX(), pos1.getY(), pos1.getZ(), packet1.getChunkData().getReadBuffer(), new CompoundTag(), (a)->{});
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(1);
        assertDeepEquals(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false), cube1);
        assertThat(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), true)).isSameAs(emptyCube);
        // Load second cube
        clientChunkCache.cc_replaceWithPacketData(pos2.getX(), pos2.getY(), pos2.getZ(), packet2.getChunkData().getReadBuffer(), new CompoundTag(), (a)->{});
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(2);
        assertDeepEquals(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false), cube1);
        assertDeepEquals(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), false), cube2);
        // Replace cube1 with cube3
        clientChunkCache.cc_replaceWithPacketData(pos1.getX(), pos1.getY(), pos1.getZ(), packet3.getChunkData().getReadBuffer(), new CompoundTag(), (a)->{});
        assertThat(clientChunkCache.cc_getLoadedCubeCount()).isEqualTo(2);
        assertDeepEquals(clientChunkCache.cc_getCube(pos1.getX(), pos1.getY(), pos1.getZ(), false), cube3);
        assertDeepEquals(clientChunkCache.cc_getCube(pos2.getX(), pos2.getY(), pos2.getZ(), false), cube2);
    }

    /**
     * Test that if a cube position is within the range of storage, then all adjacent chunk positions are also in range
     */
    @Test
    public void testCubeRangeContainedWithinChunkRange() {
        ClientLevel clientLevelMock = mock(Mockito.RETURNS_DEEP_STUBS);
        when(((CanBeCubic) clientLevelMock).cc_isCubic()).thenReturn(true);
        when(clientLevelMock.getHeight()).thenReturn(384);
        when(clientLevelMock.getSectionsCount()).thenReturn(24);
        var clientChunkCache = ((CubicClientChunkCache) new ClientChunkCache(clientLevelMock, 5));
        int centerX = 77;
        int centerZ = -33;
        clientChunkCache.cc_updateViewCenter(centerX, 0, centerZ);
        var cubeStorage = ((ClientChunkCacheTestAccess) clientChunkCache).cubeStorage();
        var chunkStorage = ((ClientChunkCacheTestAccess) clientChunkCache).chunkStorage();
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                if (cubeStorage.inRange(centerX + dx, 0, centerZ + dz)) {
                    for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                        for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
                            var isInRange = ((ClientChunkCache$StorageTestAccess) (Object) chunkStorage)
                                .invokeInRange(cubeToSection(centerX + dx, sectionX), cubeToSection(centerZ + dz, sectionZ));
                            assertTrue(isInRange);
                        }
                    }
                }
            }
        }
        // Test re-centering and updating view radius
        centerX = -123;
        centerZ = 0;
        clientChunkCache.cc_updateViewCenter(centerX, 0, centerZ);
        clientChunkCache.cc_updateViewRadius(7);
        cubeStorage = ((ClientChunkCacheTestAccess) clientChunkCache).cubeStorage();
        chunkStorage = ((ClientChunkCacheTestAccess) clientChunkCache).chunkStorage();
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                if (cubeStorage.inRange(centerX + dx, 0, centerZ + dz)) {
                    for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                        for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
                            assertTrue(((ClientChunkCache$StorageTestAccess) (Object) chunkStorage)
                                .invokeInRange(cubeToSection(centerX + dx, sectionX), cubeToSection(centerZ + dz, sectionZ)));
                        }
                    }
                }
            }
        }
        // Test re-centering without updating view radius after
        centerX = 0;
        centerZ = 300;
        clientChunkCache.cc_updateViewCenter(centerX, 0, centerZ);
        cubeStorage = ((ClientChunkCacheTestAccess) clientChunkCache).cubeStorage();
        chunkStorage = ((ClientChunkCacheTestAccess) clientChunkCache).chunkStorage();
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                if (cubeStorage.inRange(centerX + dx, 0, centerZ + dz)) {
                    for (int sectionX = 0; sectionX < CubicConstants.DIAMETER_IN_SECTIONS; sectionX++) {
                        for (int sectionZ = 0; sectionZ < CubicConstants.DIAMETER_IN_SECTIONS; sectionZ++) {
                            assertTrue(((ClientChunkCache$StorageTestAccess) (Object) chunkStorage)
                                .invokeInRange(cubeToSection(centerX + dx, sectionX), cubeToSection(centerZ + dz, sectionZ)));
                        }
                    }
                }
            }
        }
    }
}
