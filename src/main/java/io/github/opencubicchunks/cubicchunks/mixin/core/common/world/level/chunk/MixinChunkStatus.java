package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk;

import static io.github.opencubicchunks.cc_core.utils.Utils.unsafeCast;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.api.annotations.transform.Visibility;
import io.github.opencubicchunks.cc_core.annotation.Public;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.Util;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Dasm(GlobalSet.class)
@Mixin(ChunkStatus.class)
public class MixinChunkStatus {
    @Shadow @Final private int range;

    @Shadow @Final private static List<ChunkStatus> STATUS_BY_RANGE;

    private static final List<ChunkStatus> STATUS_BY_RANGE_32 = ImmutableList.of(
        ChunkStatus.FULL,
        ChunkStatus.INITIALIZE_LIGHT,
        ChunkStatus.CARVERS,
        ChunkStatus.BIOMES,
        ChunkStatus.STRUCTURE_STARTS,
        ChunkStatus.STRUCTURE_STARTS,
        ChunkStatus.STRUCTURE_STARTS,
        ChunkStatus.STRUCTURE_STARTS
    );

    private static final List<ChunkStatus> STATUS_BY_RANGE_64 = ImmutableList.of(
        ChunkStatus.FULL,
        ChunkStatus.INITIALIZE_LIGHT,
        ChunkStatus.CARVERS,
        ChunkStatus.BIOMES,
        ChunkStatus.STRUCTURE_STARTS,
        ChunkStatus.STRUCTURE_STARTS
    );

    private static final List<ChunkStatus> STATUS_BY_RANGE_128 = ImmutableList.of(
        ChunkStatus.FULL,
        ChunkStatus.INITIALIZE_LIGHT,
        ChunkStatus.CARVERS,
        ChunkStatus.BIOMES,
        ChunkStatus.STRUCTURE_STARTS
    );

    @AddFieldToSets(sets = { GlobalSet.class }, owner = @Ref(ChunkStatus.class), field = @FieldSig(type = @Ref(List.class), name = "STATUS_BY_RANGE"))
    private static final List<ChunkStatus> CUBE_STATUS_BY_RANGE = getStatusByRange();

    @AddFieldToSets(sets = { GlobalSet.class }, owner = @Ref(ChunkStatus.class), field = @FieldSig(type = @Ref(IntList.class), name = "RANGE_BY_STATUS"))
    private static final IntList CUBE_RANGE_BY_STATUS = Util.make(new IntArrayList(ChunkStatus.getStatusList().size()), (rangeByStatus) -> {
        int range = 0;

        for (int status = ChunkStatus.getStatusList().size() - 1; status >= 0; --status) {
            while (range + 1 < STATUS_BY_RANGE.size() && status <= STATUS_BY_RANGE.get(range + 1).getIndex()) {
                ++range;
            }
            rangeByStatus.add(0, range);
        }
    });

    private static List<ChunkStatus> getStatusByRange() {
        int cubeDiameter = CubicConstants.DIAMETER_IN_SECTIONS;
        switch (cubeDiameter) {
            case 1:
                return STATUS_BY_RANGE; // same as vanilla
            case 2:
                return STATUS_BY_RANGE_32;
            case 4:
                return STATUS_BY_RANGE_64;
            case 8:
                return STATUS_BY_RANGE_128;
            default:
                throw new UnsupportedOperationException("Unsupported cube size " + cubeDiameter);
        }
    }

    // TODO (P2) proper generation logic; this currently ignores everything and only handles promotion from ProtoClo to LevelClo
    @AddMethodToSets(sets = { ChunkToCloSet.class }, owner = @Ref(ChunkStatus.class), method = @MethodSig("generate(Ljava/util/concurrent/Executor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Lnet/minecraft/server/level/ThreadedLevelLightEngine;Ljava/util/function/Function;Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>> cc_generate(
        Executor exectutor,
        ServerLevel level,
        ChunkGenerator chunkGenerator,
        StructureTemplateManager structureTemplateManager,
        ThreadedLevelLightEngine lightEngine,
        Function<CloAccess, CompletableFuture<Either<CloAccess, ChunkHolder.ChunkLoadingFailure>>> task,
        List<CloAccess> cache
    ) {
        CloAccess chunkaccess = cache.get(cache.size() / 2);
        return ((Object) this == ChunkStatus.FULL ? task.apply(chunkaccess) : CompletableFuture.completedFuture(Either.left(chunkaccess)))
            .thenApply(
                p_281217_ -> {
                    p_281217_.ifLeft(p_290029_ -> {
                        if (p_290029_ instanceof ProtoClo protochunk && !protochunk.getStatus().isOrAfter((ChunkStatus) (Object) this)) {
                            protochunk.setStatus((ChunkStatus) (Object) this);
                        }
                    });

                    return unsafeCast(p_281217_);
                }
            );
    }

    // TODO should things in here actually be in GlobalSet? not sure if Chunks should also have lowered range in cubic worlds
    //      if we make it ChunkToCubeSet then it makes things slightly awkward since MixinChunkMap uses ChunkToCloSet normally
    @AddMethodToSets(sets = { GlobalSet.class }, owner = @Ref(ChunkStatus.class), method = @MethodSig("getRange()I"))
    public int cc_getRange() {
        // TODO does this actually give the correct value? This is what was used in CC2
        return Coords.sectionToCubeCeil(this.range);
    }

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(visibility = Visibility.PRIVATE, value = @MethodSig("getStatusAroundFullChunk(I)Lnet/minecraft/world/level/chunk/ChunkStatus;"))
    @Public private static native ChunkStatus cc_getStatusAroundFullCube(int radius);

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(visibility = Visibility.PRIVATE, value = @MethodSig("maxDistance()I"))
    @Public private static native int cc_maxDistance();

    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(visibility = Visibility.PRIVATE, value = @MethodSig("getDistance(Lnet/minecraft/world/level/chunk/ChunkStatus;)I"))
    @Public private static native int cc_getDistance(ChunkStatus status);
}
