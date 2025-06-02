package io.github.opencubicchunks.cubicchunks.mixin.core.common.server;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.SpawnPlaceFinder;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow public abstract ServerLevel overworld();

    @Shadow public abstract GameRules getGameRules();

    // setInitialSpawn
    // We replace the ChunkPos spawn position with a CubePos spawn position and reuse it later to get the world position.
    @Inject(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V"))
    private static void cc_replaceChunkPosInSetInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean generateBonusChest, boolean debug, CallbackInfo ci, @Share(
        "cubePos") LocalRef<CubePos> cubePosLocalRef) {
        if (((CanBeCubic) serverLevel).cc_isCubic()) {
            CubePos cubePos = new CubePos(serverLevel.getChunkSource().randomState().sampler().findSpawnPosition());
            cubePosLocalRef.set(cubePos);
        }
    }

    @WrapOperation(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;getWorldPosition()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos cc_replaceGetWorldPositionInSetInitialSpawn(ChunkPos chunkPos, Operation<BlockPos> original, ServerLevel serverLevel, @Share(
        "cubePos") LocalRef<CubePos> cubePosLocalRef) {
        if (((CanBeCubic) serverLevel).cc_isCubic()) {
            return cubePosLocalRef.get().asChunkPos().getWorldPosition();
        }
        return original.call(chunkPos);
    }

    /**
     * This mixin uses SpawnPlaceFinder (core CC2 code) in a similar fashion to the CC2 implementation.
     */
    @WrapOperation(method = "setInitialSpawn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
    private static int cc_replaceGetHeightWithSpawnPlaceFinder(ServerLevel serverLevel, Heightmap.Types heightmapType, int x, int z, Operation<Integer> original,
                                                               @Share("cubePos") LocalRef<CubePos> cubePosLocalRef) {
        if (((CanBeCubic) serverLevel).cc_isCubic()) {
            BlockPos topBlockBisect = SpawnPlaceFinder.getTopBlockBisect(serverLevel, cubePosLocalRef.get().asBlockPos(), false,
                pos -> serverLevel.getBlockState(pos).is(BlockTags.VALID_SPAWN),
                pos -> serverLevel.getBlockState(pos).getCollisionShape(serverLevel, pos).isEmpty());
            if (topBlockBisect != null) {
                return topBlockBisect.getY();
            } else {
                return serverLevel.getSeaLevel() + 1; // This is the default value in vanilla
            }
        }
        return original.call(serverLevel, heightmapType, x, z);
    }

    @WrapOperation(method = "prepareLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;square(I)I"))
    private int cc_onPrepareLevels_computeTickingGeneratedCount(int value, Operation<Integer> original) {
        if (!((CanBeCubic) overworld()).cc_isCubic()) {
            return original.call(value);
        }
        int cubeRadius = Coords.sectionToCubeCeil(this.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));
        int cubeDiameter = cubeRadius * 2 + 1;
        int chunkDiameter = cubeDiameter * CubicConstants.DIAMETER_IN_SECTIONS;
        return cubeDiameter * cubeDiameter * cubeDiameter + chunkDiameter * chunkDiameter;
    }

    // Temporary hack to let us unload a world without saving
    // TODO (P2): saving
    @Redirect(method = "stopServer", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private boolean cc_onStopServer_chunkMapHasWork(Stream instance, Predicate<?> predicate) {
        return false;
    }

    // TODO P2 :: Forced cubes will need to be implemented here as well; but this includes saving logic so P2
}
