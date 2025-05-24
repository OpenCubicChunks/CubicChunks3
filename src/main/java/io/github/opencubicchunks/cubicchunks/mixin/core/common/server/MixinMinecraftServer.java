package io.github.opencubicchunks.cubicchunks.mixin.core.common.server;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.SpawnPlaceFinder;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow public abstract ServerLevel overworld();

    @Shadow public abstract boolean isRunning();

    @Shadow protected long nextTickTimeNanos;

    @Shadow protected abstract void waitUntilNextTick();


    // TODO P2 :: This value is dynamic in 1.21, we will need to revisit this
    private static final int VANILLA_DEFAULT_SPAWN_CHUNK_RADIUS = 11;

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

    // prepareLevels
    // This mixin is copied from CC2. It fills in a spawnRadiusRef that is used to determine how many cubes we need to generate for spawn to be ready.
    @WrapWithCondition(method = "prepareLevels", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerChunkCache;addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    private <T> boolean cc_replaceAddRegionTicketInPrepareLevels(ServerChunkCache serverChunkCache, TicketType<T> ticketType, ChunkPos chunkPos, int originalSpawnRadius, T unit,
                                                                 @Share("spawnRadius") LocalRef<Integer> spawnRadiusRef) {
        if (((CanBeCubic) serverChunkCache).cc_isCubic()) {
            int spawnRadius = Coords.sectionToCube(VANILLA_DEFAULT_SPAWN_CHUNK_RADIUS);
            spawnRadiusRef.set(spawnRadius);
            ((ServerCubeCache)serverChunkCache).cc_addRegionTicket(ticketType, CloPos.cube(overworld().getSharedSpawnPos()), spawnRadius, unit);
            return false;
        }

        return true;
    }

    @ModifyConstant(method = "prepareLevels", constant = @Constant(intValue = 441), require = 1)
    private int cc_modifyExpectedNumberOfTickingGenerated(int constant, @Share("spawnRadius") LocalRef<Integer> spawnRadiusRef) {
        if (((CanBeCubic) overworld()).cc_isCubic()) {
            // We need to calculate the number of cubes + chunks in the expected radius
            int spawnRadius = spawnRadiusRef.get();
            int spawnDiameterCubes = spawnRadius * 2 + 1;
            int cubesInRadius = spawnDiameterCubes * spawnDiameterCubes * spawnDiameterCubes;

            int spawnDiameterChunks = Coords.cubeToSection(spawnDiameterCubes, 0);
            int chunksInRadius = spawnDiameterChunks * spawnDiameterChunks;

            return cubesInRadius + chunksInRadius;
        }

        return constant;
    }

    // TODO P2 :: Forced cubes will need to be implemented here as well; but this includes saving logic so P2
}
