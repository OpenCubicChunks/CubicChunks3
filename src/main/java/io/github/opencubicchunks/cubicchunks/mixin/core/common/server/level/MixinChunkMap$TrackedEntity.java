package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GeneralSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Dasm(GeneralSet.class)
@Mixin(ChunkMap.TrackedEntity.class)
public abstract class MixinChunkMap$TrackedEntity {
    @Shadow @Final Entity entity;

    // dasm + mixin
    @AddTransformToSets(GlobalSet.class) @TransformFromMethod(@MethodSig("updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public native void cc_updatePlayer(ServerPlayer pPlayer);

    @Dynamic @Redirect(method = "cc_dasm$cc_updatePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;isChunkTracked(Lnet/minecraft/server/level/ServerPlayer;II)Z"))
    private boolean cc_updatePlayer_isChunkTracked(ChunkMap instance, ServerPlayer pPlayer, int pX, int pZ) {
        // FIXME entity clo position once implemented
        return false; //((CubicChunkMap) instance).cc_isChunkTracked(pPlayer, this.entity.chunkPosition().x, 0, this.entity.chunkPosition().z);
    }
}
