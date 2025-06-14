package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.entity.MixinEntity;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.server.level.CCServerPlayer;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Dasm(ChunkToCloSet.class)
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends MixinEntity implements CCServerPlayer {
    @AddFieldToSets(containers = ChunkToCloSet.ServerPlayer_redirects.class, field = @FieldSig(type = @Ref(ChunkTrackingView.class), name = "chunkTrackingView"))
    private CloTrackingView cc_cloTrackingView = CloTrackingView.EMPTY;

    // TODO unnecessary once we have DASM redirect inheritance
    @AddMethodToSets(containers = ChunkToCubeSet.ServerPlayer_redirects.class, method = @MethodSig("chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    public CubePos cc_cubePosition() {
        return super.cc_cubePosition();
    }

    // TODO unnecessary once we have DASM redirect inheritance
    @AddMethodToSets(containers = ChunkToCloSet.ServerPlayer_redirects.class, method = @MethodSig("chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    public CloPos cc_cubePositionAsClo() {
        return super.cc_cubePositionAsClo();
    }

    @AddTransformToSets(ChunkToCloSet.ServerPlayer_redirects.class) @TransformFromMethod(@MethodSig("getChunkTrackingView()Lnet/minecraft/server/level/ChunkTrackingView;"))
    public native CloTrackingView cc_getCloTrackingView();

    @AddTransformToSets(ChunkToCloSet.ServerPlayer_redirects.class) @TransformFromMethod(@MethodSig("setChunkTrackingView(Lnet/minecraft/server/level/ChunkTrackingView;)V"))
    public native void cc_setCloTrackingView(CloTrackingView chunkTrackingView);

    // TODO P3 :: findDimensionEntryPoint

    // TODO P3 :: changeDimension

    // FIXME (P2) teleportation code needs CC changes
}
