package io.github.opencubicchunks.cubicchunks.world.level.chunklike;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.ProtoCube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;

public interface ProtoClo extends CloAccess {
    static ProtoClo create(CloPos cloPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, @Nullable BlendingData blendingData) {
        if (cloPos.isCube()) {
            return new ProtoCube(cloPos.cubePos(), upgradeData, levelHeightAccessor, biomeRegistry, blendingData);
        } else {
            return (ProtoClo) new ProtoChunk(cloPos.chunkPos(), upgradeData, levelHeightAccessor, biomeRegistry, blendingData);
        }
    }

    static ProtoClo create(
        CloPos cloPos,
        UpgradeData upgradeData,
        @Nullable LevelChunkSection[] sections,
        ProtoChunkTicks<Block> blockTicks,
        ProtoChunkTicks<Fluid> liquidTicks,
        LevelHeightAccessor levelHeightAccessor,
        Registry<Biome> biomeRegistry,
        @Nullable BlendingData blendingData
    ) {
        if (cloPos.isCube()) {
            return new ProtoCube(cloPos.cubePos(), upgradeData, sections, blockTicks, liquidTicks, levelHeightAccessor, biomeRegistry, blendingData);
        } else {
            return (ProtoClo) new ProtoChunk(cloPos.chunkPos(), upgradeData, sections, blockTicks, liquidTicks, levelHeightAccessor, biomeRegistry, blendingData);
        }
    }

    Map<BlockPos, BlockEntity> getBlockEntities();

    void addEntity(CompoundTag tag);

    List<CompoundTag> getEntities();

    void setPersistedStatus(ChunkStatus status);

    Map<BlockPos, CompoundTag> getBlockEntityNbts();

    @Nullable
    CarvingMask getCarvingMask();

    CarvingMask getOrCreateCarvingMask();

    void setCarvingMask(CarvingMask carvingMask);

    void setLightEngine(LevelLightEngine lightEngine);

    void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen);

    LevelChunkTicks<Block> unpackBlockTicks();

    LevelChunkTicks<Fluid> unpackFluidTicks();

    // TODO statics
//    public static short packOffsetCoordinates(BlockPos p_63281_);
//
//    public static BlockPos unpackOffsetCoordinates(short p_63228_, int p_63229_, ChunkPos p_63230_);
}
