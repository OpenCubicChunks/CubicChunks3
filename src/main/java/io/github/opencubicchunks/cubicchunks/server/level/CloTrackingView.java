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
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GeneralSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.level.ChunkPos;

@Dasm(GeneralSet.class)
public interface CloTrackingView extends ChunkTrackingView {
    @AddFieldToSets(sets = GlobalSet.class, owner = @Ref(ChunkTrackingView.class), field = @FieldSig(type = @Ref(ChunkTrackingView.class), name = "EMPTY"))
    CloTrackingView EMPTY = new CloTrackingView() {
        @Override public boolean cc_contains(int x, int y, int z, boolean pSearchAllChunks) {
            return false;
        }

        @Override public void cc_forEach(Consumer<CloPos> pAction) {
        }

        @Override
        public boolean contains(int x, int z, boolean pSearchAllChunks) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> pAction) {
        }
    };

    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ChunkTrackingView.class), method = @MethodSig("of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/server/level/ChunkTrackingView;"))
    static CloTrackingView cc_of(CloPos pCenter, int pViewDistance) {
        return new CloTrackingView.Positioned(pCenter, pViewDistance);
    }

    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ChunkTrackingView.class), method = @MethodSig("difference(Ljava/util/function/Consumer;Ljava/util/function/Consumer;)V"))
    static void cc_difference(CloTrackingView pOldCloTrackingView, CloTrackingView pNewCloTrackingView, Consumer<CloPos> pChunkDropper, Consumer<CloPos> pChunkMarker) {
        if (!pOldCloTrackingView.equals(pNewCloTrackingView)) {
            if (pOldCloTrackingView instanceof Positioned oldPositioned
                && pNewCloTrackingView instanceof Positioned newPositioned) {
                if (oldPositioned.cc_cubeIntersects(newPositioned)) {
                    int minX = Math.min(oldPositioned.minX(), newPositioned.minX());
                    int minY = Math.min(oldPositioned.minY(), newPositioned.minY());
                    int minZ = Math.min(oldPositioned.minZ(), newPositioned.minZ());
                    int maxX = Math.max(oldPositioned.maxX(), newPositioned.maxX());
                    int maxY = Math.max(oldPositioned.maxY(), newPositioned.maxY());
                    int maxZ = Math.max(oldPositioned.maxZ(), newPositioned.maxZ());

                    for(int x = minX; x <= maxX; ++x) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                    int chunkX = Coords.cubeToSection(x, dx);
                                    int chunkZ = Coords.cubeToSection(z, dz);
                                    boolean oldHas = oldPositioned.contains(chunkX, chunkZ);
                                    boolean newHas = newPositioned.contains(chunkX, chunkZ);
                                    if (oldHas != newHas) {
                                        if (newHas) {
                                            pChunkDropper.accept(CloPos.chunk(chunkX, chunkZ));
                                        } else {
                                            pChunkMarker.accept(CloPos.chunk(chunkX, chunkZ));
                                        }
                                    }
                                }
                            }
                            for (int y = minY; y <= maxY; ++y) {
                                boolean oldHas = oldPositioned.cc_contains(x, y, z);
                                boolean newHas = newPositioned.cc_contains(x, y, z);
                                if (oldHas != newHas) {
                                    if (newHas) {
                                        pChunkDropper.accept(CloPos.cube(x, y, z));
                                    } else {
                                        pChunkMarker.accept(CloPos.cube(x, y, z));
                                    }
                                }
                            }
                        }
                    }
                    return;
                } else if (oldPositioned.cc_chunkIntersects(newPositioned)) {
                    int minX = Math.min(oldPositioned.minX(), newPositioned.minX());
                    int minZ = Math.min(oldPositioned.minZ(), newPositioned.minZ());
                    int maxX = Math.max(oldPositioned.maxX(), newPositioned.maxX());
                    int maxZ = Math.max(oldPositioned.maxZ(), newPositioned.maxZ());

                    for(int x = minX; x <= maxX; ++x) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                    int chunkX = Coords.cubeToSection(x, dx);
                                    int chunkZ = Coords.cubeToSection(z, dz);
                                    boolean oldHas = oldPositioned.contains(chunkX, chunkZ);
                                    boolean newHas = newPositioned.contains(chunkX, chunkZ);
                                    if (oldHas != newHas) {
                                        if (newHas) {
                                            pChunkDropper.accept(CloPos.chunk(chunkX, chunkZ));
                                        } else {
                                            pChunkMarker.accept(CloPos.chunk(chunkX, chunkZ));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    oldPositioned.cc_forEachCubesOnly(pChunkMarker);
                    newPositioned.cc_forEachCubesOnly(pChunkDropper);
                    return;
                }
            }

            pOldCloTrackingView.cc_forEach(pChunkMarker);
            pNewCloTrackingView.cc_forEach(pChunkDropper);
        }
    }

    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ChunkTrackingView.class), method = @MethodSig("contains(Lnet/minecraft/world/level/ChunkPos;)Z"))
    default boolean cc_contains(CloPos cloPos) {
        if (cloPos.isCube()) {
            return this.cc_contains(cloPos.getX(), cloPos.getY(), cloPos.getZ());
        } else {
            return this.contains(cloPos.getX(), cloPos.getZ());
        }
    }

    default boolean cc_contains(int x, int y, int z) {
        return this.cc_contains(x, y, z, true);
    }

    boolean cc_contains(int x, int y, int z, boolean pSearchAllChunks);

    @AddMethodToSets(sets = GlobalSet.class, owner = @Ref(ChunkTrackingView.class), method = @MethodSig("forEach(Ljava/util/function/Consumer;)V"))
    void cc_forEach(Consumer<CloPos> pAction);

    default boolean cc_isInViewDistance(int x, int y, int z) {
        return this.cc_contains(x, y, z, false);
    }

    static boolean cc_isInViewDistance(int centerX, int centerY, int centerZ, int viewDistance, int x, int y, int z) {
        return cc_isWithinDistance(centerX, centerY, centerZ, viewDistance, x, y, z, false);
    }

    static boolean cc_isWithinDistance(int centerX, int centerY, int centerZ, int viewDistance, int x, int y, int z, boolean increaseRadiusByOne) {
        // Mojang does some weird jank, but it's almost identical to just increasing the view distance by 1 - so we do that instead
        if (increaseRadiusByOne) viewDistance++;
        int dx = Math.max(0, Math.abs(x - centerX) - 1);
        int dy = Math.max(0, Math.abs(y - centerY) - 1);
        int dz = Math.max(0, Math.abs(z - centerZ) - 1);
        return dx*dx + dy*dy + dz*dz < viewDistance * viewDistance;
    }

    static boolean cc_isWithinDistanceCubeColumn(int centerX, int centerZ, int viewDistance, int x, int z, boolean increaseRadiusByOne) {
        return cc_isWithinDistance(centerX, 0, centerZ, viewDistance, x, 0, z, increaseRadiusByOne);
    }

    record Positioned(CloPos center, int viewDistance) implements CloTrackingView {
        int minX() {
            return this.center.getX() - this.viewDistance - 1;
        }

        int minY() {
            return this.center.getY() - this.viewDistance - 1;
        }

        int minZ() {
            return this.center.getZ() - this.viewDistance - 1;
        }

        int maxX() {
            return this.center.getX() + this.viewDistance + 1;
        }

        int maxY() {
            return this.center.getY() + this.viewDistance + 1;
        }

        int maxZ() {
            return this.center.getZ() + this.viewDistance + 1;
        }

        @Override
        public boolean contains(int x, int z, boolean searchAllChunks) {
            return cc_isWithinDistanceCubeColumn(this.center.getX(), this.center.getZ(), this.viewDistance, Coords.sectionToCube(x), Coords.sectionToCube(z), searchAllChunks);
        }

        @Override
        public void forEach(Consumer<ChunkPos> pAction) {
            for(int x = this.minX(); x <= this.maxX(); ++x) {
                for(int z = this.minZ(); z <= this.maxZ(); ++z) {
                    if (this.cc_contains(x, center.getY(), z)) {
                        for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                            for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                pAction.accept(new ChunkPos(Coords.cubeToSection(x, dx), Coords.cubeToSection(z, dz)));
                            }
                        }
                    }
                }
            }
        }

        private boolean cc_cubeIntersects(CloTrackingView.Positioned pOther) {
            return this.minX() <= pOther.maxX() && this.maxX() >= pOther.minX()
                && this.minY() <= pOther.maxY() && this.maxY() >= pOther.minY()
                && this.minZ() <= pOther.maxZ() && this.maxZ() >= pOther.minZ();
        }

        private boolean cc_chunkIntersects(CloTrackingView.Positioned pOther) {
            return this.minX() <= pOther.maxX() && this.maxX() >= pOther.minX()
                && this.minZ() <= pOther.maxZ() && this.maxZ() >= pOther.minZ();
        }

        @Override public boolean cc_contains(int x, int y, int z, boolean searchAllChunks) {
            return cc_isWithinDistance(this.center.getX(), this.center.getY(), this.center.getZ(), this.viewDistance, x, y, z, searchAllChunks);
        }

        @Override public void cc_forEach(Consumer<CloPos> pAction) {
            for(int x = this.minX(); x <= this.maxX(); ++x) {
                for(int z = this.minZ(); z <= this.maxZ(); ++z) {
                    if (this.cc_contains(x, center.getY(), z)) {
                        for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                            for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                                pAction.accept(CloPos.chunk(Coords.cubeToSection(x, dx), Coords.cubeToSection(z, dz)));
                            }
                        }
                    }
                    for(int y = this.minY(); y <= this.maxY(); ++y) {
                        if (this.cc_contains(x, y, z)) {
                            pAction.accept(CloPos.cube(x, y, z));
                        }
                    }
                }
            }
        }

        private void cc_forEachCubesOnly(Consumer<CloPos> pAction) {
            for(int x = this.minX(); x <= this.maxX(); ++x) {
                for(int z = this.minZ(); z <= this.maxZ(); ++z) {
                    for(int y = this.minY(); y <= this.maxY(); ++y) {
                        if (this.cc_contains(x, y, z)) {
                            pAction.accept(CloPos.cube(x, y, z));
                        }
                    }
                }
            }
        }
    }
}
