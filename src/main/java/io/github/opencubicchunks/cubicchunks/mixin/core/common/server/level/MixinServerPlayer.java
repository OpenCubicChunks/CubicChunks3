package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements MarkableAsCubic {
    protected boolean cc_isCubic;

    public MixinServerPlayer() {
        super(null, null, 0, null);
    }

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    /**
     * This mixin steals the x/y/z coordinates from a call to ChunkPos and replaces the ChunkPos in the addRegionTicketCall with a CloPos instead.
     */
    @WrapWithCondition(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level"
        + "/ServerChunkCache;addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public <T> boolean cc_wrapAddRegionTicket(ServerChunkCache instance, TicketType<T> type, ChunkPos pos, int distance, T value,
                                              @Local(ordinal = 0, argsOnly = true)double x,
                                              @Local(ordinal = 1, argsOnly = true)double y,
                                              @Local(ordinal = 2, argsOnly = true)double z) {
        if (!cc_isCubic) {
            return true;
        }
        // TODO: Add once Cubic ServerChunkCache exists
        //instance.addRegionTicket(type, CloPos.cube(BlockPos.containing(x, y, z)), 1, this.getId());
        return false;
    }

    // TODO: phase 3 - findDimensionEntryPoint

    // TODO: phase 3 - changeDimension
}
