package io.github.opencubicchunks.cubicchunks.mixin.core.common.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Inject(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V"))
    private static void cc_replaceChunkPosInSetInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean generateBonusChest, boolean debug, CallbackInfo ci, @Share(
        "cubePos") LocalRef<CubePos> cubePosLocalRef) {
        if(((CanBeCubic) serverLevel).cc_isCubic()) {
            CubePos cubePos = new CubePos(serverLevel.getChunkSource().randomState().sampler().findSpawnPosition());
            cubePosLocalRef.set(cubePos);
        }
    }

    @WrapOperation(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;getWorldPosition()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos cc_replaceGetWorldPositionInSetInitialSpawn(ChunkPos chunkPos, Operation<BlockPos> original, ServerLevel serverLevel, @Share(
        "cubePos") LocalRef<CubePos> cubePosLocalRef) {
        if(((CanBeCubic) serverLevel).cc_isCubic()) {
            return cubePosLocalRef.get().asChunkPos().getWorldPosition();
        }
        return null;
    }

    // change getheight to funny algorithm that barteks wrote

    // setInitialSpawn - mixin

    // prepareLevels - mixin

}
