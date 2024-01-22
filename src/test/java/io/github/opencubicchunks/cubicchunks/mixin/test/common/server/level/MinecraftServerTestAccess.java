package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface MinecraftServerTestAccess {

    @Invoker(value = "prepareLevels")
    void invoke_prepareLevels(ChunkProgressListener chunkProgressListener);

    @Invoker(value = "setInitialSpawn")
    void invoke_setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean p_177899_, boolean p_177900_);
}
