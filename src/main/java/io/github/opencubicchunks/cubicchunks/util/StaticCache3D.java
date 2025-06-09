package io.github.opencubicchunks.cubicchunks.util;

import java.util.Locale;
import java.util.function.Consumer;

// TODO move to cc core? - sharing between versions is a bit awkward if vanilla changes it though
// A lot of code duplication here but dasm would be awkward
// FIXME tests? - low priority since it gets hit plenty in integration tests

/**
 * 3D equivalent of vanilla's {@link net.minecraft.util.StaticCache2D}.
 */
public class StaticCache3D<T> {
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final Object[] cache;

    public static <T> StaticCache3D<T> create(int centerX, int centerY, int centerZ, int size, StaticCache3D.Initializer<T> initializer) {
        int minX = centerX - size;
        int minY = centerY - size;
        int minZ = centerZ - size;
        int diameter = 2 * size + 1;
        return new StaticCache3D<>(minX, minY, minZ, diameter, diameter, diameter, initializer);
    }

    private StaticCache3D(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ, StaticCache3D.Initializer<T> initializer) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.cache = new Object[this.sizeX * this.sizeY * this.sizeZ];

        for (int x = minX; x < minX + sizeX; x++) {
            for (int y = minY; y < minY + sizeY; y++) {
                for (int z = minZ; z < minZ + sizeZ; z++) {
                    this.cache[this.getIndex(x, y, z)] = initializer.get(x, y, z);
                }
            }
        }
    }

    public void forEach(Consumer<T> action) {
        for (Object object : this.cache) {
            action.accept((T)object);
        }
    }

    public T get(int x, int y, int z) {
        if (!this.contains(x, y, z)) {
            throw new IllegalArgumentException("Requested out of range value (" + x + "," + y + "," + z + ") from " + this);
        } else {
            return (T)this.cache[this.getIndex(x, y, z)];
        }
    }

    public boolean contains(int x, int y, int z) {
        int xIndex = x - this.minX;
        int yIndex = y - this.minY;
        int zIndex = z - this.minZ;
        return xIndex >= 0 && xIndex < this.sizeX && yIndex >= 0 && yIndex < this.sizeY && zIndex >= 0 && zIndex < this.sizeZ;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "StaticCache3D[%d, %d, %d, %d, %d, %d]", this.minX, this.minY, this.minZ, this.minX + this.sizeX, this.minY + this.sizeY, this.minZ + this.sizeZ);
    }

    private int getIndex(int x, int y, int z) {
        int xIndex = x - this.minX;
        int yIndex = y - this.minY;
        int zIndex = z - this.minZ;
        return (xIndex * this.sizeY + yIndex) * this.sizeZ + zIndex;
    }

    @FunctionalInterface
    public interface Initializer<T> {
        T get(int x, int y, int z);
    }
}
