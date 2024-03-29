package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.opencubicchunks.cc_core.world.SpawnPlaceFinder;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Verify why this priority is here
@Mixin(value = PlayerRespawnLogic.class, priority = 999)
public class MixinPlayerRespawnLogic {

    /**
     * This mixin uses SpawnPlaceFinder (core CC2 code) in a similar fashion to the CC2 implementation.
     */
    @Inject(method = "getOverworldRespawnPos", at = @At("HEAD"), cancellable = true)
    private static void getOverworldRespawnPos(ServerLevel level, int posX, int posZ, CallbackInfoReturnable<BlockPos> cir) {
        if (!((CanBeCubic) level).cc_isCubic()) {
            return;
        }
        cir.setReturnValue(SpawnPlaceFinder.getTopBlockBisect(level, new BlockPos(posX, 0, posZ), false,
            pos -> level.getBlockState(pos).is(BlockTags.VALID_SPAWN),
            pos -> level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()));
    }
}
