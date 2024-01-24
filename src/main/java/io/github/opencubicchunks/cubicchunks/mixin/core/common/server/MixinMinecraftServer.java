package io.github.opencubicchunks.cubicchunks.mixin.core.common.server;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.world.SpawnPlaceFinder;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow public abstract ServerLevel overworld();

    @Shadow public abstract boolean isRunning();

    @Shadow protected long nextTickTimeNanos;

    @Shadow protected abstract void waitUntilNextTick();

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
            int spawnRadius = (int) Math.ceil(10 * (16 / (float) CubicConstants.DIAMETER_IN_BLOCKS)); //vanilla is 10, 32: 5, 64: 3
            spawnRadiusRef.set(spawnRadius);
            // TODO: Fix this when ServerChunkCache exists
            //(CubicServerChunkCache)serverChunkCache.addRegionTicket(CubicTicketType.START, CloPos.cube(overworld().getSharedSpawnPos()), spawnRadius + 1, unit);
            return false;
        }
        return true;
    }

    @Inject(method = "prepareLevels", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;nextTickTimeNanos:J"))
    private void cc_waitUntilCubicGenerationComplete(CallbackInfo ci, @Share("spawnRadius") LocalRef<Integer> spawnRadiusRef) {
        if (((CanBeCubic) overworld().getChunkSource()).cc_isCubic()) {
            int d = spawnRadiusRef.get() * 2 + 1;
            // TODO: Fix this when ServerChunkCache exists
            //while (this.isRunning() && ((CubicServerChunkCache) overworld().getChunkSource()).getTickingGeneratedCubes() < d * d * d) {
            //    this.nextTickTimeNanos = Util.getMillis() + 10L;
            //    this.waitUntilNextTick();
            //}
        }
    }

    // TODO: forced cubes will need to be implemented for prepareLevels as well in the same way as above
}
