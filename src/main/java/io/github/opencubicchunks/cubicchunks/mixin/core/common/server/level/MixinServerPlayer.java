package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.entity.MixinEntity;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.server.level.CCServerPlayer;
import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Dasm(ChunkToCloSet.class)
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends MixinEntity implements CCServerPlayer {
    @AddFieldToSets(containers = ChunkToCloSet.ServerPlayer_redirects.class, field = @FieldSig(type = @Ref(ChunkTrackingView.class), name = "chunkTrackingView"))
    private CloTrackingView cc_cloTrackingView = CloTrackingView.EMPTY;

    @AddTransformToSets(ChunkToCloSet.ServerPlayer_redirects.class)
    @TransformFromMethod(@MethodSig("getChunkTrackingView()Lnet/minecraft/server/level/ChunkTrackingView;"))
    public native CloTrackingView cc_getCloTrackingView();

    @AddTransformToSets(ChunkToCloSet.ServerPlayer_redirects.class)
    @TransformFromMethod(@MethodSig("setChunkTrackingView(Lnet/minecraft/server/level/ChunkTrackingView;)V"))
    public native void cc_setCloTrackingView(CloTrackingView chunkTrackingView);

    // TODO P3 :: findDimensionEntryPoint

    // TODO P3 :: changeDimension

    // FIXME (P2) teleportation code needs CC changes
}
