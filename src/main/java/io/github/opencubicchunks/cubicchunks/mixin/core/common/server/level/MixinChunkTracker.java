package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link ChunkTracker} is a class that determines the edges and as well as the values to be propagated to the edges in {@link DynamicGraphMinFixedPoint}.
 * It marks all chunks in a 1 chunk radius around the center as edges. Edge chunks have 1 level higher than the center chunk.
 * <br><br>
 * {@link DistanceManager.ChunkTicketTracker} would be the equivalent to {@link net.minecraft.server.level.TickingTracker}. Since that
 * implementation does not use {@link ChunkPos} directly, we do not need to mixin it unlike {@link net.minecraft.server.level.TickingTracker}.
 * <br><br>
 * This mixin replaces {@link ChunkPos} with {@link CloPos} and adds in logic to handle propagation for cubes.
 */
@Mixin(ChunkTracker.class)
public abstract class MixinChunkTracker extends DynamicGraphMinFixedPoint implements MarkableAsCubic {
    protected boolean cc_isCubic;
    private int cc_noChunkLevel;

    /** This is a list of all loaded cubes grouped by "big" column positions (32x32 columns) since cubes are 32x32x32.
     * It is needed for quick lookups to determine which cubes are next to a column when propagating level. */
    private Long2ObjectMap<IntSet> cc_existingCubesForCubeColumns;

    protected MixinChunkTracker() {
        super(0, 0, 0);
    }

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
        cc_existingCubesForCubeColumns = new Long2ObjectLinkedOpenHashMap<>();
        cc_noChunkLevel = levelCount-1;
    }

    @Redirect(method="*", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/ChunkPos;INVALID_CHUNK_POS:J"))
    private long cc_sentinelValue() {
        if (cc_isCubic)
            return CloPos.INVALID_CLO_POS;
        return ChunkPos.INVALID_CHUNK_POS;
    }

    @ModifyConstant(method = "computeLevelFromNeighbor", constant = @Constant(intValue = 1))
    private int cc_dontIncrementLevelOnCubeChunkEdge(int constant, @Local(ordinal = 0, argsOnly = true) long startPos, @Local(ordinal = 1, argsOnly = true) long endPos) {
        if (cc_isCubic && CloPos.isCube(startPos) && CloPos.isChunk(endPos)) return 0;
        return constant;
    }

    @Inject(method="checkNeighborsAfterUpdate", at=@At("HEAD"), cancellable = true)
    private void cc_onCheckNeighborsAfterUpdate(long pos, int level, boolean isDecreasing, CallbackInfo ci) {
        if (cc_isCubic) {
            ci.cancel();
            CloPos.forEachNeighbor(pos, n -> this.checkNeighbor(pos, n, level, isDecreasing));
        }
    }

    /**
     * This computes the propagation of the level from neighbor to neighbor.
     * <br><br>
     * We have to handle two cases: whether we are propagating to a column or a cube.
     * <br><br>
     * However, it is important to note that you cannot propagate from a column to a cube. This is because
     * if we did do that, then a single column could potentially load an infinite amount of cubes. So only
     * cubes can propagate to a column, or cubes propagating to cubes.
     */
    @Inject(method = "getComputedLevel", at=@At("HEAD"), cancellable = true)
    private void cc_onGetComputedLevel(long pos, long excludedSourcePos, int level, CallbackInfoReturnable<Integer> cir) {
        if (!cc_isCubic) return;
        if (CloPos.isChunk(pos)) {
            int out = level;

            int x = CloPos.extractX(pos);
            int z = CloPos.extractZ(pos);
            for (int x2 = -1; x2 <= 1; ++x2) {
                for (int z2 = -1; z2 <= 1; ++z2) {
                    long neighbor = CloPos.asLong(x + x2, z + z2);
                    if (neighbor == pos) {
                        neighbor = CloPos.INVALID_CLO_POS;
                    }

                    if (neighbor != excludedSourcePos) {
                        int k1 = this.computeLevelFromNeighbor(neighbor, pos, this.getLevel(neighbor));
                        if (out > k1) {
                            out = k1;
                        }

                        if (out == 0) {
                            cir.setReturnValue(out);
                            return;
                        }
                    }
                }
            }
            // This is propagating the level from neighboring cubes to this column.
            long cubeColumnKey = CloPos.asLong(Coords.sectionToCube(x), 0, Coords.sectionToCube(z));
            IntSet neighborCubeYSet = this.cc_existingCubesForCubeColumns.get(cubeColumnKey);
            if (neighborCubeYSet != null) {
                for (Integer cubeY : neighborCubeYSet) {
                    long neighbor = CloPos.setY(cubeColumnKey, cubeY);
                    assert neighbor != pos;
                    if (neighbor != excludedSourcePos) {
                        int k1 = this.computeLevelFromNeighbor(neighbor, pos, this.getLevel(neighbor));
                        if (out > k1) {
                            out = k1;
                        }

                        if (out == 0) {
                            cir.setReturnValue(out);
                            return;
                        }
                    }
                }
            }
            cir.setReturnValue(out);
        } else {
            int out = level;

            int x = CloPos.extractX(pos);
            int y = CloPos.extractY(pos);
            int z = CloPos.extractZ(pos);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        long neighbor = CloPos.asLong(x + dx, y + dy, z + dz);
                        if (neighbor == pos) {
                            neighbor = CloPos.INVALID_CLO_POS;
                        }

                        if (neighbor != excludedSourcePos) {
                            int k1 = this.computeLevelFromNeighbor(neighbor, pos, this.getLevel(neighbor));
                            if (out > k1) {
                                out = k1;
                            }

                            if (out == 0) {
                                cir.setReturnValue(out);
                                return;
                            }
                        }
                    }
                }
            }
            cir.setReturnValue(out);
        }
    }

    /**
     * This function adds in new cubes and sorts them into the cube column map.
     * <br><br>
     * It should be called from each onSetLevel implementation.
     * For CC, this should only be {@link MixinTickingTracker#cc_onSetLevel(long, int)} and {@link MixinChunkTracker#cc_onSetLevel(long, int)}.
     */
    protected void cc_onSetLevel(long pos, int level) {
        if (cc_isCubic && CloPos.isCube(pos)) {
            long key = CloPos.setY(pos, 0);
            IntSet cubes = cc_existingCubesForCubeColumns.get(key);
            if (level >= cc_noChunkLevel) {
                if (cubes != null) {
                    cubes.remove(CloPos.extractY(pos));
                    if (cubes.isEmpty()) {
                        cc_existingCubesForCubeColumns.remove(key);
                    }
                }
            } else {
                if (cubes == null) {
                    cc_existingCubesForCubeColumns.put(key, cubes = new IntOpenHashSet());
                }
                cubes.add(CloPos.extractY(pos));
            }
        }
    }
}
