package io.github.opencubicchunks.cubicchunks.server.level;

import java.util.function.Consumer;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.level.ChunkPos;

@Dasm(ChunkToCloSet.class)
public interface CloTrackingView extends ChunkTrackingView {
    @AddFieldToSets(containers = ChunkToCloSet.ChunkTrackingView_to_CloTrackingView_redirects.class, field = @FieldSig(type = @Ref(ChunkTrackingView.class), name = "EMPTY"))
    CloTrackingView EMPTY = new CloTrackingView() {
        @Override public boolean cc_contains(int cubeX, int cubeY, int cubeZ, boolean searchAllChunks) {
            return false;
        }

        @Override public void cc_forEach(Consumer<CloPos> action) {}

        @Override public boolean contains(int x, int z, boolean searchAllChunks) {
            return false;
        }

        @Override public void forEach(Consumer<ChunkPos> action) {}
    };

    @AddMethodToSets(containers = ChunkToCloSet.ChunkTrackingView_to_CloTrackingView_redirects.class, method = @MethodSig("of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/server/level/ChunkTrackingView;"))
    static CloTrackingView cc_of(CloPos center, int viewDistanceCubes) {
        return new CloTrackingView.Positioned(center, viewDistanceCubes);
    }

    @AddMethodToSets(containers = ChunkToCloSet.ChunkTrackingView_to_CloTrackingView_redirects.class, method = @MethodSig("difference(Lnet/minecraft/server/level/ChunkTrackingView;Lnet/minecraft/server/level/ChunkTrackingView;Ljava/util/function/Consumer;Ljava/util/function/Consumer;)V"))
    static void cc_difference(
            CloTrackingView oldCloTrackingView, CloTrackingView newCloTrackingView, Consumer<CloPos> chunkDropper, Consumer<CloPos> chunkMarker
    ) {
        if (!oldCloTrackingView.equals(newCloTrackingView)) {
            if (oldCloTrackingView instanceof Positioned oldPositioned && newCloTrackingView instanceof Positioned newPositioned) {
                if (oldPositioned.cc_cubeIntersects(newPositioned)) {
                    int minCubeX = Math.min(oldPositioned.minX(), newPositioned.minX());
                    int minCubeY = Math.min(oldPositioned.minY(), newPositioned.minY());
                    int minCubeZ = Math.min(oldPositioned.minZ(), newPositioned.minZ());
                    int maxCubeX = Math.max(oldPositioned.maxX(), newPositioned.maxX());
                    int maxCubeY = Math.max(oldPositioned.maxY(), newPositioned.maxY());
                    int maxCubeZ = Math.max(oldPositioned.maxZ(), newPositioned.maxZ());

                    for (int cubeX = minCubeX; cubeX <= maxCubeX; ++cubeX) {
                        for (int cubeZ = minCubeZ; cubeZ <= maxCubeZ; ++cubeZ) {
                            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                    int chunkX = Coords.cubeToSection(cubeX, dx);
                                    int chunkZ = Coords.cubeToSection(cubeZ, dz);
                                    boolean oldHas = oldPositioned.contains(chunkX, chunkZ);
                                    boolean newHas = newPositioned.contains(chunkX, chunkZ);
                                    if (oldHas != newHas) {
                                        if (newHas) {
                                            chunkDropper.accept(CloPos.chunk(chunkX, chunkZ));
                                        } else {
                                            chunkMarker.accept(CloPos.chunk(chunkX, chunkZ));
                                        }
                                    }
                                }
                            }
                            for (int cubeY = minCubeY; cubeY <= maxCubeY; ++cubeY) {
                                boolean oldHas = oldPositioned.cc_contains(cubeX, cubeY, cubeZ);
                                boolean newHas = newPositioned.cc_contains(cubeX, cubeY, cubeZ);
                                if (oldHas != newHas) {
                                    if (newHas) {
                                        chunkDropper.accept(CloPos.cube(cubeX, cubeY, cubeZ));
                                    } else {
                                        chunkMarker.accept(CloPos.cube(cubeX, cubeY, cubeZ));
                                    }
                                }
                            }
                        }
                    }
                    return;
                } else if (oldPositioned.cc_chunkIntersects(newPositioned)) {
                    int minCubeX = Math.min(oldPositioned.minX(), newPositioned.minX());
                    int minCubeZ = Math.min(oldPositioned.minZ(), newPositioned.minZ());
                    int maxCubeX = Math.max(oldPositioned.maxX(), newPositioned.maxX());
                    int maxCubeZ = Math.max(oldPositioned.maxZ(), newPositioned.maxZ());

                    for (int cubeX = minCubeX; cubeX <= maxCubeX; ++cubeX) {
                        for (int cubeZ = minCubeZ; cubeZ <= maxCubeZ; ++cubeZ) {
                            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                    int chunkX = Coords.cubeToSection(cubeX, dx);
                                    int chunkZ = Coords.cubeToSection(cubeZ, dz);
                                    boolean oldHas = oldPositioned.contains(chunkX, chunkZ);
                                    boolean newHas = newPositioned.contains(chunkX, chunkZ);
                                    if (oldHas != newHas) {
                                        if (newHas) {
                                            chunkDropper.accept(CloPos.chunk(chunkX, chunkZ));
                                        } else {
                                            chunkMarker.accept(CloPos.chunk(chunkX, chunkZ));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    oldPositioned.cc_forEachCubesOnly(chunkMarker);
                    newPositioned.cc_forEachCubesOnly(chunkDropper);
                    return;
                }
            }

            oldCloTrackingView.cc_forEach(chunkMarker);
            newCloTrackingView.cc_forEach(chunkDropper);
        }
    }

    @AddMethodToSets(containers = ChunkToCloSet.ChunkTrackingView_to_CloTrackingView_redirects.class, method = @MethodSig("contains(Lnet/minecraft/world/level/ChunkPos;)Z"))
    default boolean cc_contains(CloPos cloPos) {
        if (cloPos.isCube()) {
            return this.cc_contains(cloPos.getX(), cloPos.getY(), cloPos.getZ());
        } else {
            return this.contains(cloPos.getX(), cloPos.getZ());
        }
    }

    default boolean cc_contains(int cubeX, int cubeY, int cubeZ) {
        return this.cc_contains(cubeX, cubeY, cubeZ, true);
    }

    boolean cc_contains(int cubeX, int cubeY, int cubeZ, boolean searchAllChunks);

    @AddMethodToSets(containers = ChunkToCloSet.ChunkTrackingView_to_CloTrackingView_redirects.class, method = @MethodSig("forEach(Ljava/util/function/Consumer;)V"))
    void cc_forEach(Consumer<CloPos> action);

    default boolean cc_isInViewDistance(int cubeX, int cubeY, int cubeZ) {
        return this.cc_contains(cubeX, cubeY, cubeZ, false);
    }

    static boolean cc_isInViewDistance(int centerCubeX, int centerCubeY, int centerCubeZ, int viewDistanceCubes, int cubeX, int cubeY, int cubeZ) {
        return cc_isWithinDistance(centerCubeX, centerCubeY, centerCubeZ, viewDistanceCubes, cubeX, cubeY, cubeZ, false);
    }

    static boolean cc_isWithinDistance(
            int centerCubeX, int centerCubeY, int centerCubeZ, int viewDistanceCubes, int cubeX, int cubeY, int cubeZ,
            boolean includeOuterChunksAdjacentToViewBorder
    ) {
        int i = includeOuterChunksAdjacentToViewBorder ? 2 : 1;
        int dx = Math.max(0, Math.abs(cubeX - centerCubeX) - i);
        int dy = Math.max(0, Math.abs(cubeY - centerCubeY) - i);
        int dz = Math.max(0, Math.abs(cubeZ - centerCubeZ) - i);
        return dx * dx + dy * dy + dz * dz < viewDistanceCubes * viewDistanceCubes;
    }

    static boolean cc_isWithinDistanceCubeColumn(
            int centerCubeX, int centerCubeZ, int viewDistanceCubes, int cubeX, int cubeZ, boolean includeOuterChunksAdjacentToViewBorder
    ) {
        return cc_isWithinDistance(centerCubeX, 0, centerCubeZ, viewDistanceCubes, cubeX, 0, cubeZ, includeOuterChunksAdjacentToViewBorder);
    }

    @Dasm(ChunkToCloSet.class)
    record Positioned(
            CloPos center,
            @AddMethodToSets(containers = ChunkToCloSet.ChunkTrackingView$Positioned_to_CloTrackingView$Positioned_redirects.class, method = @MethodSig("viewDistance()I")) int viewDistanceCubes
    ) implements CloTrackingView {
        int minX() {
            return this.center.getX() - this.viewDistanceCubes - 1;
        }

        int minY() {
            return this.center.getY() - this.viewDistanceCubes - 1;
        }

        int minZ() {
            return this.center.getZ() - this.viewDistanceCubes - 1;
        }

        int maxX() {
            return this.center.getX() + this.viewDistanceCubes + 1;
        }

        int maxY() {
            return this.center.getY() + this.viewDistanceCubes + 1;
        }

        int maxZ() {
            return this.center.getZ() + this.viewDistanceCubes + 1;
        }

        @Override public boolean contains(int chunkX, int chunkZ, boolean searchAllChunks) {
            return cc_isWithinDistanceCubeColumn(this.center.getX(), this.center.getZ(), this.viewDistanceCubes, Coords.sectionToCube(chunkX),
                    Coords.sectionToCube(chunkZ), searchAllChunks);
        }

        @Override public void forEach(Consumer<ChunkPos> action) {
            for (int x = this.minX(); x <= this.maxX(); ++x) {
                for (int z = this.minZ(); z <= this.maxZ(); ++z) {
                    if (this.cc_contains(x, center.getY(), z)) {
                        for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                            for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                action.accept(new ChunkPos(Coords.cubeToSection(x, dx), Coords.cubeToSection(z, dz)));
                            }
                        }
                    }
                }
            }
        }

        private boolean cc_cubeIntersects(CloTrackingView.Positioned other) {
            return this.minX() <= other.maxX() && this.maxX() >= other.minX() && this.minY() <= other.maxY() && this.maxY() >= other.minY()
                    && this.minZ() <= other.maxZ() && this.maxZ() >= other.minZ();
        }

        private boolean cc_chunkIntersects(CloTrackingView.Positioned other) {
            return this.minX() <= other.maxX() && this.maxX() >= other.minX() && this.minZ() <= other.maxZ() && this.maxZ() >= other.minZ();
        }

        @Override public boolean cc_contains(int cubeX, int cubeY, int cubeZ, boolean searchAllChunks) {
            return cc_isWithinDistance(this.center.getX(), this.center.getY(), this.center.getZ(), this.viewDistanceCubes, cubeX, cubeY, cubeZ,
                    searchAllChunks);
        }

        @Override public void cc_forEach(Consumer<CloPos> action) {
            for (int x = this.minX(); x <= this.maxX(); ++x) {
                for (int z = this.minZ(); z <= this.maxZ(); ++z) {
                    if (this.cc_contains(x, center.getY(), z)) {
                        for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                            for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                action.accept(CloPos.chunk(Coords.cubeToSection(x, dx), Coords.cubeToSection(z, dz)));
                            }
                        }
                    }
                    for (int y = this.minY(); y <= this.maxY(); ++y) {
                        if (this.cc_contains(x, y, z)) {
                            action.accept(CloPos.cube(x, y, z));
                        }
                    }
                }
            }
        }

        private void cc_forEachCubesOnly(Consumer<CloPos> action) {
            for (int x = this.minX(); x <= this.maxX(); ++x) {
                for (int z = this.minZ(); z <= this.maxZ(); ++z) {
                    for (int y = this.minY(); y <= this.maxY(); ++y) {
                        if (this.cc_contains(x, y, z)) {
                            action.accept(CloPos.cube(x, y, z));
                        }
                    }
                }
            }
        }
    }
}
