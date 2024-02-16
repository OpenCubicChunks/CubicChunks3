package io.github.opencubicchunks.cubicchunks.world.level.chunklike;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.LevelChunkTicks;

public interface LevelClo extends CloAccess {
    static LevelClo create(Level level, CloPos pos) {
        if (pos.isCube()) {
            return new LevelCube(level, pos);
        } else {
            return (LevelClo) new LevelChunk(level, pos.chunkPos());
        }
    }
    static LevelClo create(Level level,
                           CloPos pos,
                           UpgradeData data,
                           LevelChunkTicks<Block> blockTicks,
                           LevelChunkTicks<Fluid> fluidTicks,
                           long inhabitedTime,
                           @Nullable LevelChunkSection[] sections,
                           @Nullable PostLoadProcessor postLoad,
                           @Nullable BlendingData blendingData) {
        if (pos.isCube()) {
            return new LevelCube(level, pos, data, blockTicks, fluidTicks, inhabitedTime, sections, postLoad, blendingData);
        } else {
            return (LevelClo) new LevelChunk(level, pos.chunkPos(), data, blockTicks, fluidTicks, inhabitedTime, sections, PostLoadProcessor.forChunk(postLoad), blendingData);
        }
    }
    static LevelClo create(ServerLevel level, ProtoClo clo, @Nullable PostLoadProcessor postLoad) {
        if (clo instanceof ProtoCube cube) {
            return new LevelCube(level, cube, postLoad);
        } else {
            return (LevelClo) new LevelChunk(level, ((ProtoChunk) clo), PostLoadProcessor.forChunk(postLoad));
        }
    }

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

    @FunctionalInterface
    interface PostLoadProcessor {
        void run(LevelClo pChunk);
        /**
         * vanilla expects a (LevelChunk -> void) but this PostLoadProcessor is a (LevelClo -> void), so we delegate
         */
        static @Nullable LevelChunk.PostLoadProcessor forChunk(@Nullable PostLoadProcessor postLoad) {
            return postLoad == null ? null : chunk -> postLoad.run((LevelClo) chunk);
        }

        static @Nullable LevelCube.PostLoadProcessor forCube(@Nullable PostLoadProcessor postLoad) {
            return postLoad == null ? null : postLoad::run;
        }
    }
}
