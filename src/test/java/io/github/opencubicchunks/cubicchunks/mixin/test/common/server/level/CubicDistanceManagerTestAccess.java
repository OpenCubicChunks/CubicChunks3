package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DistanceManager.class)
public interface CubicDistanceManagerTestAccess {
    @Invoker(value = "updateChunkForced")
    void invoke_updateChunkForced(ChunkPos p_140800_, boolean p_140801_);

    @Dynamic
    @Invoker(value = "updateCubeForced", remap = false)
    void invoke_updateCubeForced(CloPos p_140800_, boolean p_140801_);

    @Accessor(value = "playerTicketManager")
    DistanceManager.PlayerTicketTracker get_playerTicketManager();
}
