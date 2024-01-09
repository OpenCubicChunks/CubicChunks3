package io.github.opencubicchunks.cubicchunks.test.world.level.chunklike;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCloPos {
    // If using 16x16x16 cubes, we don't have enough bits to fit all coordinates from -30 million to 30 million.
    static int maxValidDistance = CubicConstants.DIAMETER_IN_SECTIONS == 1 ? 15001000 : 30001000;
    static int maxValidDistanceCube = maxValidDistance / CubicConstants.DIAMETER_IN_BLOCKS;

    private void constructionAndBasicMethods(int x, int y, int z) {
        var cube = CloPos.cube(x, y, z);
        assertEquals(x, cube.getX());
        assertEquals(y, cube.getY());
        assertEquals(z, cube.getZ());
        assertTrue(cube.isCube());
        assertFalse(cube.isChunk());
        assertThrows(UnsupportedOperationException.class, cube::chunkPos);

        var cube2 = CloPos.cube(CubePos.of(x, y, z));

        assertEquals(cube, cube2);

        assertEquals(CubePos.from(new BlockPos(x, y, z)), CloPos.cube(new BlockPos(x, y, z)).cubePos());

        var sectionPos = SectionPos.of(x, y, z);
        assertEquals(CubePos.from(sectionPos), CloPos.section(sectionPos).cubePos());

        var column = CloPos.chunk(x, z);
        assertEquals(x, column.getX());
        assertEquals(z, column.getZ());
        assertTrue(column.isChunk());
        assertFalse(column.isCube());
        assertThrows(UnsupportedOperationException.class, column::cubePos);
        assertThrows(UnsupportedOperationException.class, column::getY);

        var column2 = CloPos.chunk(new ChunkPos(x, z));

        assertEquals(column, column2);
        assertEquals(new ChunkPos(x, z), column.chunkPos());

        assertEquals(cube, CloPos.fromLong(cube.asLong()));
        assertEquals(column, CloPos.fromLong(column.asLong()));

        assertEquals(cube.asLong(), CloPos.asLong(x, y, z));
        assertEquals(column.asLong(), CloPos.asLong(x, z));
    }

    private void packedUnpackedParity(int x, int y, int z) {
        var cube = CloPos.cube(x, y, z);
        var cubeLong = CloPos.asLong(x, y, z);

        assertEquals(cube.getX(), CloPos.extractX(cubeLong));
        assertEquals(cube.getY(), CloPos.extractY(cubeLong));
        assertEquals(cube.getZ(), CloPos.extractZ(cubeLong));
        assertTrue(CloPos.isCube(cubeLong));
        assertFalse(CloPos.isChunk(cubeLong));
        var neighbors1 = new ArrayList<>();
        var neighbors2 = new ArrayList<>();
        cube.forEachNeighbor(neighbors1::add);
        CloPos.forEachNeighbor(cubeLong, (val) -> neighbors2.add(CloPos.fromLong(val)));
        assertEquals(neighbors1, neighbors2);

        var column = CloPos.chunk(x, z);
        var columnLong = CloPos.asLong(x, z);
        assertEquals(column.getX(), CloPos.extractX(columnLong));
        assertThrows(IllegalArgumentException.class, () -> CloPos.extractY(columnLong));
        assertEquals(column.getZ(), CloPos.extractZ(columnLong));
        assertTrue(CloPos.isChunk(columnLong));
        assertFalse(CloPos.isCube(columnLong));
        neighbors1.clear();
        neighbors2.clear();
        column.forEachNeighbor(neighbors1::add);
        CloPos.forEachNeighbor(columnLong, (val) -> neighbors2.add(CloPos.fromLong(val)));
        assertEquals(neighbors1, neighbors2);
    }

    // offsetX/Z should be between 0 and CubicConstants.DIAMETER_IN_SECTIONS
    private void correspondingPositions(int x, int y, int z, int offsetX, int offsetY, int offsetZ) {
        // Calling methods with cube pos
        var cube = CloPos.cube(x, y, z);
        var verticallyOffsetCube = CloPos.cube(x, y+offsetY, z);
        assertEquals(cube, cube.correspondingCubeCloPos(y));
        assertEquals(cube.cubePos(), cube.correspondingCubePos(y));
        assertEquals(verticallyOffsetCube, cube.correspondingCubeCloPos(y+offsetY));
        assertEquals(verticallyOffsetCube.cubePos(), cube.correspondingCubePos(y+offsetY));
        var expectedChunkPosZeroOffset = CloPos.chunk(Coords.cubeToSection(x, 0), Coords.cubeToSection(z, 0));
        assertEquals(expectedChunkPosZeroOffset, cube.correspondingChunkCloPos());
        assertEquals(expectedChunkPosZeroOffset.chunkPos(), cube.correspondingChunkPos());
        var expectedChunkPos = CloPos.chunk(Coords.cubeToSection(x, offsetX), Coords.cubeToSection(z, offsetZ));
        assertEquals(expectedChunkPos, cube.correspondingChunkCloPos(offsetX, offsetZ));
        assertEquals(expectedChunkPos.chunkPos(), cube.correspondingChunkPos(offsetX, offsetZ));
        // Calling methods with chunk pos
        var chunk = CloPos.chunk(x, z);
        assertEquals(chunk, chunk.correspondingChunkCloPos());
        assertEquals(chunk, chunk.correspondingChunkCloPos(offsetX, offsetZ));
        assertEquals(chunk.chunkPos(), chunk.correspondingChunkPos());
        assertEquals(chunk.chunkPos(), chunk.correspondingChunkPos(offsetX, offsetZ));
        var correspondingCube = CloPos.cube(Coords.sectionToCube(x), y, Coords.sectionToCube(z));
        assertEquals(correspondingCube, chunk.correspondingCubeCloPos(y));
        assertEquals(correspondingCube.cubePos(), chunk.correspondingCubePos(y));
    }

    private void longGetSet(int x, int y, int z, int offsetX, int offsetY, int offsetZ) {
        // Calling methods with cube pos
        var cube = CloPos.asLong(x, y, z);
        assertEquals(x+offsetX, CloPos.extractX(CloPos.setX(cube, x+offsetX)));
        assertEquals(y+offsetY, CloPos.extractY(CloPos.setY(cube, y+offsetY)));
        assertEquals(z+offsetZ, CloPos.extractZ(CloPos.setZ(cube, z+offsetZ)));
        assertEquals(CloPos.asLong(x+offsetX, y+offsetY, z+offsetZ), CloPos.setZ(CloPos.setY(CloPos.setX(cube, x+offsetX), y+offsetY), z+offsetZ));
        // Calling methods with chunk pos
        var chunk = CloPos.asLong(x, z);
        assertEquals(x+offsetX, CloPos.extractX(CloPos.setX(chunk, x+offsetX)));
        assertThrows(IllegalArgumentException.class, () -> CloPos.setY(chunk, y+offsetY));
        assertThrows(IllegalArgumentException.class, () -> CloPos.extractY(chunk));
        assertEquals(z+offsetZ, CloPos.extractZ(CloPos.setZ(chunk, z+offsetZ)));
        assertEquals(CloPos.asLong(x+offsetX, z+offsetZ), CloPos.setZ(CloPos.setX(chunk, x+offsetX), z+offsetZ));
    }

    @Test public void testConstructionAndBasicMethods() {
        var random = new Random(7777);
        for (int i = 0; i < 1000; i++) {
            int x = random.nextInt(10000)-5000;
            int y = random.nextInt(10000)-5000;
            int z = random.nextInt(10000)-5000;

            constructionAndBasicMethods(x, y, z);
        }
    }

    @Test public void testPackedUnpackedParity() {
        var random = new Random(7778);
        for (int i = 0; i < 1000; i++) {
            int x = random.nextInt(10000) - 5000;
            int y = random.nextInt(10000) - 5000;
            int z = random.nextInt(10000) - 5000;

            packedUnpackedParity(x, y, z);
        }
    }

    @Test public void testCorrespondingPositions() {
        var random = new Random(7780);
        for (int i = 0; i < 1000; i++) {
            int x = random.nextInt(10000) - 5000;
            int y = random.nextInt(10000) - 5000;
            int z = random.nextInt(10000) - 5000;
            int offsetX = random.nextInt(CubicConstants.DIAMETER_IN_SECTIONS);
            int offsetZ = random.nextInt(CubicConstants.DIAMETER_IN_SECTIONS);
            int offsetY = random.nextInt(10000) - 5000;
            correspondingPositions(x, y, z, offsetX, offsetY, offsetZ);
        }
    }

    @Test public void testLongGetSet() {
        var random = new Random(7781);
        for (int i = 0; i < 1000; i++) {
            int x = random.nextInt(10000) - 5000;
            int y = random.nextInt(10000) - 5000;
            int z = random.nextInt(10000) - 5000;
            int offsetX = random.nextInt(10000) - 5000;
            int offsetZ = random.nextInt(10000) - 5000;
            int offsetY = random.nextInt(10000) - 5000;
            longGetSet(x, y, z, offsetX, offsetY, offsetZ);
        }
    }

    @Test public void testHighMagnitudeCoordinates() {
        var random = new Random(7779);
        for (int i = 0; i < 1000; i++) {
            // on each axis we pick either a coordinate near the magnitude limit (either positive or negative), or zero
            int x = (maxValidDistanceCube - random.nextInt(10000)) * (random.nextInt(3) - 1);
            int y = (maxValidDistanceCube - random.nextInt(10000)) * (random.nextInt(3) - 1);
            int z = (maxValidDistanceCube - random.nextInt(10000)) * (random.nextInt(3) - 1);
            int offsetSectionX = random.nextInt(CubicConstants.DIAMETER_IN_SECTIONS);
            int offsetSectionZ = random.nextInt(CubicConstants.DIAMETER_IN_SECTIONS);
            int offsetX = random.nextInt(10000) - 5000;
            int offsetZ = random.nextInt(10000) - 5000;
            int offsetY = random.nextInt(10000) - 5000;

            constructionAndBasicMethods(x, y, z);
            packedUnpackedParity(x, y, z);
            correspondingPositions(x, y, z, offsetSectionX, offsetY, offsetSectionZ);
            longGetSet(x, y, z, offsetX, offsetY, offsetZ);
        }
    }

    @Test public void forEachNeighborCounts() {
        var pos = CloPos.cube(3, 4, 5);
        var cubesCols = new int[] { 0,  0 };
        pos.forEachNeighbor(p -> cubesCols[p.isCube() ? 0 : 1]++);
        assertEquals(26, cubesCols[0]);
        assertEquals(CubicConstants.CHUNK_COUNT, cubesCols[1]);
        cubesCols[0] = 0;
        cubesCols[1] = 0;
        CloPos.forEachNeighbor(pos.asLong(), p -> cubesCols[CloPos.isCube(p) ? 0 : 1]++);
        assertEquals(26, cubesCols[0]);
        assertEquals(CubicConstants.CHUNK_COUNT, cubesCols[1]);
    }

    @Test public void testLongMaxValueIsOutOfBounds() {
        var pos = CloPos.fromLong(Long.MAX_VALUE);
        if (pos.isCube()) {
            assertTrue(Math.max(
                Math.max(
                    Math.abs(pos.getX()*CubicConstants.DIAMETER_IN_BLOCKS),
                    Math.abs(pos.getY()*CubicConstants.DIAMETER_IN_BLOCKS)
                ),
                Math.abs(pos.getZ()*CubicConstants.DIAMETER_IN_BLOCKS)
            ) > maxValidDistance, "Long.MAX_VALUE should represent an out-of-bounds CloPos, since it is used to represent invalid CloPoses, but it represents " + pos);
        } else {
            assertTrue(Math.max(
                Math.abs(pos.getX())*16,
                Math.abs(pos.getZ())*16
            ) > maxValidDistance, "Long.MAX_VALUE should represent an out-of-bounds CloPos, since it is used to represent invalid CloPoses, but it represents " + pos);
        }
    }
}
