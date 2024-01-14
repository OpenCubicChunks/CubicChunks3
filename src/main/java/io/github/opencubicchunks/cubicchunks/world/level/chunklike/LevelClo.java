package io.github.opencubicchunks.cubicchunks.world.level.chunklike;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;

public interface LevelClo extends CloAccess {
    FluidState getFluidState(int p_62815_, int p_62816_, int p_62817_);

    @Nullable
    BlockEntity getBlockEntity(BlockPos p_62868_, LevelChunk.EntityCreationType p_62869_);

    void addAndRegisterBlockEntity(BlockEntity p_156391_);

    boolean isTicking(BlockPos p_156411_);

    void runPostLoad();

    boolean isEmpty();

    void replaceWithPacketData(
        FriendlyByteBuf p_187972_, CompoundTag p_187973_, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> p_187974_
    );

    void replaceBiomes(FriendlyByteBuf p_275574_);

    void setLoaded(boolean p_62914_);

    Level getLevel();

    Map<BlockPos, BlockEntity> getBlockEntities();

    void postProcessGeneration();

    void unpackTicks(long p_187986_);

    void registerTickContainerInLevel(ServerLevel p_187959_);

    void unregisterTickContainerFromLevel(ServerLevel p_187980_);

    FullChunkStatus getFullStatus();

    void setFullStatus(Supplier<FullChunkStatus> p_62880_);

    void clearAllBlockEntities();

    void registerAllBlockEntitiesAfterLevelLoad();
}
