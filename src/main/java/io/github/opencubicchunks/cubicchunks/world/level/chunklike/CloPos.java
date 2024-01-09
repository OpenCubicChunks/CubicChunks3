package io.github.opencubicchunks.cubicchunks.world.level.chunklike;

import io.github.opencubicchunks.cc_core.annotation.UsedFromASM;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

/**
 * A representation of the position of either a Chunk or a Cube.
 * <br><br>
 * When packed as a long, chunk positions are encoded the same as {@link ChunkPos#toLong}; cube positions are packed with 21 bits per axis. The parity of the top two bits of the long is
 * used to distinguish between chunks and cubes (if bit 0 XOR bit 1, it is a cube, otherwise it is a chunk).
 * <br>
 * Also note that for cubes the top two bits (the parity bit, and the top bit of the Z coordinate) are inverted, as otherwise {@link Long#MAX_VALUE} would be a valid position (-1, -1, -1).
 * <br>
 * Invalid CloPos long:          <br> <code> 0b01111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111 </code> <br>
 * Positive Z cube CloPos long:  <br> <code> 0b01ZZZZZZ ZZZZZZZZ ZZZZZZYY YYYYYYYY YYYYYYYY YYYXXXXX XXXXXXXX XXXXXXXX </code> <br>
 * Negative Z cube CloPos long:  <br> <code> 0b10ZZZZZZ ZZZZZZZZ ZZZZZZYY YYYYYYYY YYYYYYYY YYYXXXXX XXXXXXXX XXXXXXXX </code> <br>
 * Positive Z chunk CloPos long: <br> <code> 0b00ZZZZZZ ZZZZZZZZ ZZZZZZZZ ZZZZZZZZ XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX </code> <br>
 * Negative Z chunk CloPos long: <br> <code> 0b11ZZZZZZ ZZZZZZZZ ZZZZZZZZ ZZZZZZZZ XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX </code>
 */
public class CloPos {
    private static final int CLO_Y_COLUMN_INDICATOR = Integer.MAX_VALUE;

    private static final long TOP_TWO_BITS_MASK = (0b11L << 62);
    /**
     * long representing an invalid CloPos. This is the same value as {@link ChunkPos#INVALID_CHUNK_POS}.
     */
    public static final long INVALID_CLO_POS = Long.MAX_VALUE;
    private final int x, y, z;

    private CloPos(CubePos cubePos) {
        if (cubePos.getY() == CLO_Y_COLUMN_INDICATOR) {
            throw new IllegalArgumentException("Invalid cube Y position");
        }
        this.x = cubePos.getX();
        this.y = cubePos.getY();
        this.z = cubePos.getZ();
    }

    private CloPos(ChunkPos columnPos) {
        this.x = columnPos.x;
        this.z = columnPos.z;
        this.y = CLO_Y_COLUMN_INDICATOR;
    }

    private CloPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static CloPos cube(int x, int y, int z) {
        if (y == CLO_Y_COLUMN_INDICATOR) {
            throw new IllegalArgumentException("Invalid cube Y position");
        }
        return new CloPos(x, y, z);
    }


    public static CloPos chunk(int x, int z) {
        return new CloPos(x, CLO_Y_COLUMN_INDICATOR, z);
    }

    /**
     * Create a CloPos representing a CubePos containing a given section
     */
    public static CloPos section(SectionPos section) {
        return cube(
            Coords.sectionToCube(section.x()),
            Coords.sectionToCube(section.y()),
            Coords.sectionToCube(section.z())
        );
    }

    public static CloPos cube(CubePos cubePos) {
        return new CloPos(cubePos);
    }

    public static CloPos chunk(ChunkPos pos) {
        return new CloPos(pos);
    }

    public static CloPos cube(BlockPos pos) {
        return cube(
            Coords.blockToCube(pos.getX()),
            Coords.blockToCube(pos.getY()),
            Coords.blockToCube(pos.getZ())
        );
    }

    public boolean isCube() {
        return this.y != CLO_Y_COLUMN_INDICATOR;
    }

    public boolean isChunk() {
        return this.y == CLO_Y_COLUMN_INDICATOR;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        if (!isCube()) {
            throw new UnsupportedOperationException("Calling getY() on column CloPos");
        }
        return y;
    }

    public int getZ() {
        return z;
    }

    public static boolean isCube(long cloPos) {
        return (((cloPos >> 1) ^ cloPos) & (1L<<62)) != 0;
    }

    public static boolean isChunk(long cloPos) {
        return (((cloPos >> 1) ^ cloPos) & (1L<<62)) == 0;
    }

    public static CloPos fromLong(long cloPos) {
        if (CloPos.isCube(cloPos)) {
            int x = extractCubeX(cloPos);
            int y = extractCubeY(cloPos);
            int z = extractCubeZ(cloPos);
            return CloPos.cube(x, y, z);
        }
        return CloPos.chunk(ChunkPos.getX(cloPos), ChunkPos.getZ(cloPos));
    }

    public long asLong() {
        if (isCube()) {
            return CloPos.asLong(x, y, z);
        } else {
            return CloPos.asLong(x, z);
        }
    }

    /**
     * Exists for DASM transforms for ChunkPos->CloPos
     */
    @UsedFromASM
    public long toLong() {
        return asLong();
    }

    public static long asLong(int x, int y, int z) {
        long i = 0L;
        i |= ((long) x & (1 << 21) - 1);
        i |= ((long) y & (1 << 21) - 1) << 21;
        i |= ((long) z & (1 << 21) - 1) << 42;
        // If 2nd bit isn't set, set 1st bit, since cubes are marked by starting with 0b01 or 0b10
        if (i < (1L << 62)) i |= (1L << 63);
        // invert the top two bits for storage (as explained in the class javadoc)
        i ^= TOP_TWO_BITS_MASK;
        return i;
    }

    public static long asLong(int x, int z) {
        return ChunkPos.asLong(x, z);
    }

    private static int extractCubeX(long packed) {
        return (int) (packed << 43 >> 43);
    }

    private static int extractCubeY(long packed) {
        return (int) (packed << 22 >> 43);
    }

    private static int extractCubeZ(long packed) {
        // re-invert the top two bits, since they were inverted for storage
        packed ^= TOP_TWO_BITS_MASK;
        return (int) (packed << 1 >> 43);
    }

    public static int extractX(long packed) {
        if (isCube(packed)) return extractCubeX(packed);
        return ChunkPos.getX(packed);
    }

    public static int extractY(long packed) {
        if (isChunk(packed)) {
            throw new IllegalArgumentException("Column CloPos doesn't have a Y coordinate!");
        }
        return extractCubeY(packed);
    }

    public static int extractZ(long packed) {
        if (isCube(packed)) return extractCubeZ(packed);
        return ChunkPos.getZ(packed);
    }

    public static long setX(long packed, int x) {
        if (isChunk(packed)) {
            return ChunkPos.asLong(x, ChunkPos.getZ(packed));
        }
        //noinspection PointlessBitwiseExpression: yes intellij, it *is* equivalent to "-(1L << 21)" but it's also not as obvious
        packed &= ~((1L << 21) - 1);
        return packed | (x & (1L << 21) - 1);
    }

    public static long setY(long packed, int y) {
        if (isChunk(packed)) {
            throw new IllegalArgumentException("Column CloPos doesn't have a Y coordinate!");
        }
        packed &= ~(((1L << 21) - 1) << 21);
        return packed | ((y & (1L << 21) - 1) << 21);
    }

    public static long setZ(long packed, int z) {
        if (isChunk(packed)) {
            return ChunkPos.asLong(ChunkPos.getX(packed), z);
        }
        // mask is one bit larger (22 instead of 21) to also reset the parity bit
        packed &= ~(((1L << 22) - 1) << 42);
        packed |= ((z & (1L << 21) - 1) << 42);
        // If 2nd bit isn't set, set 1st bit, since cubes are marked by starting with 0b01 or 0b10
        if (packed < (1L << 62)) packed |= (1L << 63);
        // invert the top two bits for storage (as explained in the class javadoc)
        return packed ^ TOP_TWO_BITS_MASK;
    }

    public CubePos cubePos() {
        if (isChunk()) {
            throw new UnsupportedOperationException("Calling getY() on chunk CloPos");
        }
        return CubePos.of(this.x, this.y, this.z);
    }

    public ChunkPos chunkPos() {
        if (isCube()) {
            throw new UnsupportedOperationException("Calling getY() on cube CloPos");
        }
        return new ChunkPos(this.x, this.z);
    }

    public CloPos correspondingCubeCloPos(int y) {
        if (isCube()) {
            return CloPos.cube(this.x, y, this.z);
        } else {
            return CloPos.cube(Coords.sectionToCube(this.x), y, Coords.sectionToCube(this.z));
        }
    }

    public CubePos correspondingCubePos(int y) {
        if (isCube()) {
            return CubePos.of(this.x, y, this.z);
        } else {
            return CubePos.of(Coords.sectionToCube(this.x), y, Coords.sectionToCube(this.z));
        }
    }

    public ChunkPos correspondingChunkPos() {
        return correspondingChunkPos(0, 0);
    }

    public ChunkPos correspondingChunkPos(int localX, int localZ) {
        if (isCube()) {
            return new ChunkPos(Coords.cubeToSection(this.x, localX), Coords.cubeToSection(this.z, localZ));
        } else {
            return new ChunkPos(this.x, this.z);
        }
    }

    public CloPos correspondingChunkCloPos() {
        return correspondingChunkCloPos(0, 0);
    }

    public CloPos correspondingChunkCloPos(int localX, int localZ) {
        if (isCube()) {
            return CloPos.chunk(Coords.cubeToSection(this.x, localX), Coords.cubeToSection(this.z, localZ));
        } else {
            return CloPos.chunk(this.x, this.z);
        }
    }

    /**
     * Iterate over neighbors of this position. Chunks are treated as neighbors of cubes, but not the reverse (there is a mono-directional edge from cubes to chunks)
     */
    public void forEachNeighbor(Consumer<? super CloPos> consumer) {
        if (isCube()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0 || dz != 0)
                            consumer.accept(CloPos.cube(this.x + dx, this.y + dy, this.z + dz));
                    }
                }
            }
            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                    consumer.accept(correspondingChunkCloPos(dx, dz));
                }
            }
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0)
                        consumer.accept(CloPos.chunk(this.x + dx, this.z + dz));
                }
            }
        }
    }
    /**
     * Iterate over neighbors of the given position. Chunks are treated as neighbors of cubes, but not the reverse (there is a mono-directional edge from cubes to chunks)
     */
    public static void forEachNeighbor(long packed, LongConsumer consumer) {
        if (isCube(packed)) {
            int x = extractCubeX(packed);
            int y = extractCubeY(packed);
            int z = extractCubeZ(packed);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0 || dz != 0)
                            consumer.accept(CloPos.asLong(x + dx, y + dy, z + dz));
                    }
                }
            }
            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                    consumer.accept(CloPos.asLong(Coords.cubeToSection(x, dx), Coords.cubeToSection(z, dz)));
                }
            }
        } else {
            int x = ChunkPos.getX(packed);
            int z = ChunkPos.getZ(packed);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0)
                        consumer.accept(CloPos.asLong(x + dx, z + dz));
                }
            }
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloPos cloPos = (CloPos) o;
        return x == cloPos.x && y == cloPos.y && z == cloPos.z;
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override public String toString() {
        if (isCube()) {
            return "CloPos{" +
                "cube" +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
        }
        return "CloPos{" +
            "column" +
            ", x=" + x +
            ", z=" + z +
            '}';
    }
}
