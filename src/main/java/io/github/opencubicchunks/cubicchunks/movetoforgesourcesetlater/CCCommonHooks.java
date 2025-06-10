package io.github.opencubicchunks.cubicchunks.movetoforgesourcesetlater;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.GlobalSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.CommonHooks;

@Dasm(GlobalSet.class)
public class CCCommonHooks {
    @AddMethodToSets(sets = ChunkToCubeSet.class, owner = @Ref(CommonHooks.class), method = @MethodSig("onChunkUnload(Lnet/minecraft/world/entity/ai/village/poi/PoiManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)V"))
    public static void onCubeUnload(PoiManager poiManager, CubeAccess cubeAccess) {
        // TODO (P2) save/load: once PoiManager cubic methods are implemented, this method can be a dasm copy
    }

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(CommonHooks.class), method = @MethodSig("onChunkUnload(Lnet/minecraft/world/entity/ai/village/poi/PoiManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)V"))
    public static void onCloUnload(PoiManager poiManager, CloAccess cloAccess) {
        if (cloAccess instanceof CubeAccess cubeAccess) {
            onCubeUnload(poiManager, cubeAccess);
        } else {
            CommonHooks.onChunkUnload(poiManager, ((ChunkAccess) cloAccess));
        }
    }
}
