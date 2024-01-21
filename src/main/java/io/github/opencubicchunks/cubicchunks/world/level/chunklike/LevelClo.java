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
    FluidState getFluidState(int x, int y, int z);

    @Nullable
    BlockEntity getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType creationType);

    void addAndRegisterBlockEntity(BlockEntity blockEntity);

    boolean isTicking(BlockPos pos);

    void runPostLoad();

    boolean isEmpty();

    void replaceWithPacketData(
        FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> outputTagConsumer
    );

    void replaceBiomes(FriendlyByteBuf buffer);

    void setLoaded(boolean loaded);

    Level getLevel();

    Map<BlockPos, BlockEntity> getBlockEntities();

    void postProcessGeneration();

    void unpackTicks(long pos);

    void registerTickContainerInLevel(ServerLevel level);

    void unregisterTickContainerFromLevel(ServerLevel level);

    FullChunkStatus getFullStatus();

    void setFullStatus(Supplier<FullChunkStatus> fullStatus);

    void clearAllBlockEntities();

    void registerAllBlockEntitiesAfterLevelLoad();
}
